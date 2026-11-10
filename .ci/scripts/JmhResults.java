import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Publishes this CI run's JMH results. Runs as a single-file program in the build image:
 *
 * <pre>java .ci/scripts/JmhResults.java upload|links|update-latest</pre>
 *
 * <ul>
 *   <li>{@code upload} stores every results.json in the bucket under this run's key.
 *   <li>{@code links} prints JMH Visualizer links, compared with the last run and the last master run.
 *   <li>{@code update-latest} records this run's keys as the latest baselines.
 * </ul>
 *
 * Requests are signed here instead of by the AWS SDK: the SDK signs Accept-Encoding, which Cloudflare rewrites.
 */
public final class JmhResults {
    private static final Path REPORTS = Path.of("api/feature/benchmark/build/reports");
    private static final String HOST = "s3.dussim.xyz";
    private static final String BUCKET = "jmh-results";
    private static final String REGION = "us-east-1";
    // One host letter is percent-encoded so Crow's secret masking does not corrupt printed links.
    private static final String LINK_BUCKET_URL = "https://s3.%64ussim.xyz/" + BUCKET;
    private static final String VISUALIZER_URL = "https://jmh.morethan.io/";
    private static final String SEPARATOR = "=".repeat(64);
    private static final String UNSIGNED_PAYLOAD = "UNSIGNED-PAYLOAD";
    private static final String SIGNED_HEADERS = "host;x-amz-content-sha256;x-amz-date";
    private static final DateTimeFormatter AMZ_DATE = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");

    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(30)).build();

    private JmhResults() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException("Usage: java JmhResults.java upload|links|update-latest");
        }
        String runKey = env("CI_PIPELINE_NUMBER") + "-" + env("CI_COMMIT_SHA");
        List<String> files = resultFiles();
        switch (args[0]) {
            case "upload" -> upload(files, runKey);
            case "links" -> links(files, runKey);
            case "update-latest" -> updateLatest(files, runKey);
            default -> throw new IllegalArgumentException("Unknown command: " + args[0]);
        }
    }

    private static void upload(List<String> files, String runKey) throws Exception {
        for (String file : files) {
            put(runKey + "/" + file, "application/json", BodyPublishers.ofFile(REPORTS.resolve(file)));
            System.out.println("Uploaded " + file);
        }
    }

    private static void links(List<String> files, String runKey) throws Exception {
        for (String file : files) {
            String suite = suite(file);
            String key = runKey + "/" + file;
            System.out.println();
            System.out.println(SEPARATOR);
            System.out.println("  " + (suite.equals("jmh") ? "JMH benchmarks" : "Stable validation: " + suite));
            System.out.println(SEPARATOR);
            System.out.println("New results (" + runLabel(key) + "):");
            System.out.println("  " + VISUALIZER_URL + "?source=" + LINK_BUCKET_URL + "/" + key);
            printComparison("last run", readLatest("any", suite), key);
            printComparison("last master", readLatest("master", suite), key);
        }
    }

    private static void updateLatest(List<String> files, String runKey) throws Exception {
        boolean master = "master".equals(System.getenv("CI_COMMIT_BRANCH"));
        for (String file : files) {
            String suite = suite(file);
            String key = runKey + "/" + file;
            putLatest("any", suite, key);
            if (master) {
                putLatest("master", suite, key);
            }
            System.out.println("Recorded " + suite + " as latest");
        }
    }

    private static void printComparison(String title, String previousKey, String key) {
        if (!previousKey.isEmpty()) {
            System.out.println();
            System.out.println("New vs " + title + " (" + runLabel(previousKey) + "):");
            System.out.println("  " + VISUALIZER_URL + "?sources=" + LINK_BUCKET_URL + "/" + previousKey + "," + LINK_BUCKET_URL + "/" + key);
        }
    }

    /** Result files below the reports directory, as sorted '/'-separated relative paths. */
    private static List<String> resultFiles() throws IOException {
        try (Stream<Path> paths = Files.walk(REPORTS)) {
            return paths
                .filter(path -> path.getFileName().toString().equals("results.json"))
                .map(path -> REPORTS.relativize(path).toString().replace('\\', '/'))
                .sorted()
                .toList();
        }
    }

    /** "jmh/results.json" is suite "jmh"; "validation/small/<timestamp>/results.json" is suite "small". */
    private static String suite(String file) {
        String[] parts = file.split("/");
        if (parts.length >= 3 && parts[0].equals("validation")) {
            return parts[1];
        }
        int lastSlash = file.lastIndexOf('/');
        return lastSlash < 0 ? file : file.substring(0, lastSlash);
    }

    /** Keys look like "<pipeline number>-<commit sha>/<file>". */
    private static String runLabel(String key) {
        int dash = key.indexOf('-');
        if (dash < 0) {
            return key;
        }
        String sha = key.substring(dash + 1);
        return "run #" + key.substring(0, dash) + ", " + sha.substring(0, Math.min(7, sha.length()));
    }

    /** The recorded baseline key, or an empty string when there is none or it cannot be read. */
    private static String readLatest(String scope, String suite) throws InterruptedException {
        URI uri = URI.create("https://" + HOST + "/" + BUCKET + "/" + encodePath(latestKey(scope, suite)) + "?run=" + env("CI_PIPELINE_NUMBER"));
        HttpRequest request = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(30)).GET().build();
        try {
            HttpResponse<String> response = HTTP.send(request, BodyHandlers.ofString());
            return response.statusCode() / 100 == 2 ? response.body().strip() : "";
        } catch (IOException e) {
            return "";
        }
    }

    private static void putLatest(String scope, String suite, String key) throws Exception {
        put(latestKey(scope, suite), "text/plain", BodyPublishers.ofString(key));
    }

    private static String latestKey(String scope, String suite) {
        return "latest/" + scope + "/" + suite + ".key";
    }

    /** PUTs one object, signed with AWS Signature Version 4 and an unsigned payload. */
    private static void put(String key, String contentType, BodyPublisher body) throws Exception {
        String accessKey = env("S3_ACCESS_KEY");
        String secretKey = env("S3_SECRET_KEY");
        String amzDate = ZonedDateTime.now(ZoneOffset.UTC).format(AMZ_DATE);
        String date = amzDate.substring(0, 8);
        String path = "/" + BUCKET + "/" + encodePath(key);
        String scope = date + "/" + REGION + "/s3/aws4_request";

        String canonicalRequest = String.join(
            "\n",
            "PUT",
            path,
            "",
            "host:" + HOST,
            "x-amz-content-sha256:" + UNSIGNED_PAYLOAD,
            "x-amz-date:" + amzDate,
            "",
            SIGNED_HEADERS,
            UNSIGNED_PAYLOAD
        );
        String stringToSign = String.join("\n", "AWS4-HMAC-SHA256", amzDate, scope, sha256Hex(canonicalRequest));

        byte[] signingKey = ("AWS4" + secretKey).getBytes(StandardCharsets.UTF_8);
        for (String part : List.of(date, REGION, "s3", "aws4_request")) {
            signingKey = hmac(signingKey, part);
        }
        String signature = HexFormat.of().formatHex(hmac(signingKey, stringToSign));

        HttpRequest request = HttpRequest.newBuilder(URI.create("https://" + HOST + path))
            .timeout(Duration.ofSeconds(60))
            .header("Authorization", "AWS4-HMAC-SHA256 Credential=" + accessKey + "/" + scope + ", SignedHeaders=" + SIGNED_HEADERS + ", Signature=" + signature)
            .header("x-amz-content-sha256", UNSIGNED_PAYLOAD)
            .header("x-amz-date", amzDate)
            .header("Content-Type", contentType)
            .PUT(body)
            .build();
        HttpResponse<String> response = HTTP.send(request, BodyHandlers.ofString());
        if (response.statusCode() / 100 != 2) {
            throw new IOException("PUT " + key + " failed: HTTP " + response.statusCode() + "\n" + response.body());
        }
    }

    /** Percent-encodes each path segment the way Signature Version 4 expects (RFC 3986 unreserved characters stay). */
    private static String encodePath(String path) {
        return Arrays.stream(path.split("/"))
            .map(segment -> URLEncoder.encode(segment, StandardCharsets.UTF_8).replace("+", "%20").replace("*", "%2A").replace("%7E", "~"))
            .collect(Collectors.joining("/"));
    }

    private static String sha256Hex(String text) throws GeneralSecurityException {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8)));
    }

    private static byte[] hmac(byte[] key, String text) throws GeneralSecurityException {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, "HmacSHA256"));
        return mac.doFinal(text.getBytes(StandardCharsets.UTF_8));
    }

    private static String env(String name) {
        String value = System.getenv(name);
        if (value == null || value.isEmpty()) {
            throw new IllegalStateException("Missing environment variable " + name);
        }
        return value;
    }
}
