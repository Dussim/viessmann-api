local lib = import "lib.libsonnet";

{
  name: "jmh",
  when: [
    { event: "manual" },
    { event: "push", branch: "master" },
  ],
  clone: lib.shallowClone,
  steps: [
    lib.gradle("jmh", ["gradle jmh"]),
    lib.gradle("stableSmallValidationJmh", ["gradle stableSmallValidationJmh"]),
    lib.gradle("stableMediumValidationJmh", ["gradle stableMediumValidationJmh"]),
    lib.gradle("stableLargeValidationJmh", ["gradle stableLargeValidationJmh"]),
    {
      name: "uploadResults",
      // curl instead of woodpeckerci/plugin-s3: the AWS SDK signs Accept-Encoding, which Cloudflare rewrites.
      image: "curlimages/curl:8.23.0",
      when: { status: ["success", "failure"] },
      environment: {
        S3_ACCESS_KEY: { from_secret: "S3_ACCESS_KEY" },
        S3_SECRET_KEY: { from_secret: "S3_SECRET_KEY" },
      },
      commands: [
        "cd api/feature/benchmark/build/reports",
        "find . -name results.json | sed 's|^\\./||' | while read -r file; do curl --fail-with-body -sS --aws-sigv4 aws:amz:us-east-1:s3 --user \"$${S3_ACCESS_KEY}:$${S3_SECRET_KEY}\" -H 'x-amz-content-sha256: UNSIGNED-PAYLOAD' -H 'Content-Type: application/json' -T \"$${file}\" \"https://s3.dussim.xyz/jmh-results/${CI_PIPELINE_NUMBER}-${CI_COMMIT_SHA}/$${file}\" || exit 1; echo \"Uploaded $${file}\"; done",
      ],
    },
    {
      name: "visualizerLinks",
      image: "curlimages/curl:8.23.0",
      when: { status: ["success", "failure"] },
      commands: ["sh .crow/scripts/jmh-links.sh"],
    },
    {
      name: "updateLatest",
      image: "curlimages/curl:8.23.0",
      environment: {
        S3_ACCESS_KEY: { from_secret: "S3_ACCESS_KEY" },
        S3_SECRET_KEY: { from_secret: "S3_SECRET_KEY" },
      },
      commands: ["sh .crow/scripts/jmh-update-latest.sh"],
    },
  ],
}
