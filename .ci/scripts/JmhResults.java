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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
 *   <li>{@code links} prints JMH Visualizer links and a table of scores and allocation, both compared with the
 *       last run and the last master run.
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
    private static final String BENCHMARK_PACKAGE = "xyz.dussim.feature.benchmark.";
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
            String lastKey = readLatest("any", suite);
            String masterKey = readLatest("master", suite);
            printComparison("last run", lastKey, key);
            printComparison("last master", masterKey, key);
            printTable(file, lastKey, masterKey);
        }
    }

    /**
     * Prints this run's scores and allocation next to their change against the last run and the last master run.
     * A problem here must not hide the links above or fail the step, so it is reported and skipped.
     */
    private static void printTable(String file, String lastKey, String masterKey) throws InterruptedException {
        try {
            Map<String, Result> current = parseResults(Files.readString(REPORTS.resolve(file)));
            Map<String, Result> last = baseline(lastKey);
            Map<String, Result> master = baseline(masterKey);

            List<String[]> rows = new ArrayList<>();
            rows.add(new String[] {"Benchmark", "Score", "Error", "Unit", "vs last run", "vs last master", "B/op", "vs last run", "vs last master"});
            for (Result result : current.values()) {
                boolean higherIsBetter = result.mode().equals("thrpt");
                Result lastResult = last.get(result.id());
                Result masterResult = master.get(result.id());
                rows.add(new String[] {
                    result.id(),
                    formatScore(result.score()),
                    formatScore(result.error()),
                    result.unit(),
                    scoreChange(result, lastResult, higherIsBetter),
                    scoreChange(result, masterResult, higherIsBetter),
                    Double.isNaN(result.alloc()) ? "-" : String.format(Locale.ROOT, "%,.0f", result.alloc()),
                    allocChange(result, lastResult),
                    allocChange(result, masterResult),
                });
            }

            System.out.println();
            System.out.println("Change is new against old; '~' means within the two runs' error, '-' means no baseline.");
            int[] widths = new int[rows.get(0).length];
            for (String[] row : rows) {
                for (int column = 0; column < row.length; column++) {
                    widths[column] = Math.max(widths[column], row[column].length());
                }
            }
            for (String[] row : rows) {
                StringBuilder line = new StringBuilder("  ");
                for (int column = 0; column < row.length; column++) {
                    // The benchmark name is left-aligned, every other column right-aligned.
                    String format = "%" + (column == 0 ? "-" : "") + widths[column] + "s";
                    line.append(String.format(format, row[column])).append(column == row.length - 1 ? "" : "  ");
                }
                System.out.println(line);
            }
        } catch (IOException | RuntimeException e) {
            System.out.println();
            System.out.println("No comparison table for " + file + ": " + e);
        }
    }

    /** The results stored under [key], or none when there is no baseline or it cannot be read. */
    private static Map<String, Result> baseline(String key) throws InterruptedException {
        if (key.isEmpty()) {
            return Map.of();
        }
        String body = get(key);
        return body.isEmpty() ? Map.of() : parseResults(body);
    }

    private static String scoreChange(Result current, Result previous, boolean higherIsBetter) {
        if (previous == null || !(previous.score() > 0) || Double.isNaN(current.score())) {
            return "-";
        }
        double difference = current.score() - previous.score();
        double noise = orZero(current.error()) + orZero(previous.error());
        String verdict = Math.abs(difference) <= noise ? "~" : (difference > 0) == higherIsBetter ? "better" : "worse";
        return String.format(Locale.ROOT, "%+.1f%% %s", difference / previous.score() * 100, verdict);
    }

    /** Allocation per operation is nearly exact, so any change of at least 0.1% counts. */
    private static String allocChange(Result current, Result previous) {
        if (previous == null || !(previous.alloc() > 0) || Double.isNaN(current.alloc())) {
            return "-";
        }
        double percent = (current.alloc() - previous.alloc()) / previous.alloc() * 100;
        String verdict = Math.abs(percent) < 0.1 ? "~" : percent < 0 ? "better" : "worse";
        return String.format(Locale.ROOT, "%+.1f%% %s", percent, verdict);
    }

    private static double orZero(double value) {
        return Double.isNaN(value) ? 0 : value;
    }

    private static String formatScore(double value) {
        if (Double.isNaN(value)) {
            return "-";
        }
        double magnitude = Math.abs(value);
        return String.format(Locale.ROOT, magnitude >= 1000 ? "%,.0f" : magnitude >= 10 ? "%.2f" : "%.3f", value);
    }

    /** One benchmark of a JMH result file; [alloc] is gc.alloc.rate.norm in B/op, NaN without the GC profiler. */
    private record Result(String id, String mode, double score, double error, String unit, double alloc) {
    }

    /** Results of a JMH JSON file by benchmark name (without the common package) and parameters. */
    private static Map<String, Result> parseResults(String json) {
        Map<String, Result> results = new LinkedHashMap<>();
        for (Object entry : (List<?>) new JsonReader(json).read()) {
            Map<?, ?> run = (Map<?, ?>) entry;
            String id = ((String) run.get("benchmark")).replace(BENCHMARK_PACKAGE, "");
            if (run.get("params") instanceof Map<?, ?> params && !params.isEmpty()) {
                id += params.entrySet().stream().map(param -> param.getKey() + "=" + param.getValue()).collect(Collectors.joining(", ", " [", "]"));
            }
            Map<?, ?> primary = (Map<?, ?>) run.get("primaryMetric");
            double alloc = Double.NaN;
            if (run.get("secondaryMetrics") instanceof Map<?, ?> secondary && secondary.get("gc.alloc.rate.norm") instanceof Map<?, ?> norm) {
                alloc = number(norm.get("score"));
            }
            results.put(id, new Result(id, (String) run.get("mode"), number(primary.get("score")), number(primary.get("scoreError")), (String) primary.get("scoreUnit"), alloc));
        }
        return results;
    }

    /** JMH writes a missing error as the string "NaN". */
    private static double number(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        try {
            return value instanceof String text ? Double.parseDouble(text) : Double.NaN;
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }

    /** Reads JSON into maps, lists, strings, doubles, booleans and null; enough for JMH result files. */
    private static final class JsonReader {
        private final String text;
        private int position;

        JsonReader(String text) {
            this.text = text;
        }

        Object read() {
            Object value = value();
            skipWhitespace();
            if (position != text.length()) {
                throw error("Unexpected content after the JSON value");
            }
            return value;
        }

        private Object value() {
            skipWhitespace();
            if (position >= text.length()) {
                throw error("Unexpected end of JSON");
            }
            char start = text.charAt(position);
            if (start == '{') {
                return object();
            }
            if (start == '[') {
                return array();
            }
            if (start == '"') {
                return string();
            }
            if (text.startsWith("true", position)) {
                position += 4;
                return Boolean.TRUE;
            }
            if (text.startsWith("false", position)) {
                position += 5;
                return Boolean.FALSE;
            }
            if (text.startsWith("null", position)) {
                position += 4;
                return null;
            }
            if (text.startsWith("NaN", position)) {
                position += 3;
                return Double.NaN;
            }
            int end = position;
            while (end < text.length() && "+-.eE0123456789".indexOf(text.charAt(end)) >= 0) {
                end++;
            }
            if (end == position) {
                throw error("Unexpected character '" + start + "'");
            }
            double number = Double.parseDouble(text.substring(position, end));
            position = end;
            return number;
        }

        private Map<String, Object> object() {
            Map<String, Object> object = new LinkedHashMap<>();
            position++;
            skipWhitespace();
            if (peek() == '}') {
                position++;
                return object;
            }
            while (true) {
                skipWhitespace();
                String name = string();
                skipWhitespace();
                expect(':');
                object.put(name, value());
                skipWhitespace();
                if (peek() == '}') {
                    position++;
                    return object;
                }
                expect(',');
            }
        }

        private List<Object> array() {
            List<Object> array = new ArrayList<>();
            position++;
            skipWhitespace();
            if (peek() == ']') {
                position++;
                return array;
            }
            while (true) {
                array.add(value());
                skipWhitespace();
                if (peek() == ']') {
                    position++;
                    return array;
                }
                expect(',');
            }
        }

        private String string() {
            expect('"');
            StringBuilder result = new StringBuilder();
            while (true) {
                char current = peek();
                position++;
                if (current == '"') {
                    return result.toString();
                }
                if (current != '\\') {
                    result.append(current);
                    continue;
                }
                char escaped = peek();
                position++;
                switch (escaped) {
                    case 'b' -> result.append('\b');
                    case 'f' -> result.append('\f');
                    case 'n' -> result.append('\n');
                    case 'r' -> result.append('\r');
                    case 't' -> result.append('\t');
                    case 'u' -> {
                        result.append((char) Integer.parseInt(text.substring(position, position + 4), 16));
                        position += 4;
                    }
                    default -> result.append(escaped);
                }
            }
        }

        private char peek() {
            if (position >= text.length()) {
                throw error("Unexpected end of JSON");
            }
            return text.charAt(position);
        }

        private void expect(char expected) {
            if (peek() != expected) {
                throw error("Expected '" + expected + "' but found '" + peek() + "'");
            }
            position++;
        }

        private void skipWhitespace() {
            while (position < text.length() && Character.isWhitespace(text.charAt(position))) {
                position++;
            }
        }

        private IllegalArgumentException error(String message) {
            return new IllegalArgumentException(message + " at offset " + position);
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
        return get(latestKey(scope, suite)).strip();
    }

    /** The public object stored under [key], or an empty string when it is missing or cannot be read. */
    private static String get(String key) throws InterruptedException {
        URI uri = URI.create("https://" + HOST + "/" + BUCKET + "/" + encodePath(key) + "?run=" + env("CI_PIPELINE_NUMBER"));
        HttpRequest request = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(30)).GET().build();
        try {
            HttpResponse<String> response = HTTP.send(request, BodyHandlers.ofString());
            return response.statusCode() / 100 == 2 ? response.body() : "";
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
