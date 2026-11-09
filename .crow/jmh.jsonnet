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
      image: "woodpeckerci/plugin-s3:1.5.6",
      when: { status: ["success", "failure"] },
      settings: {
        endpoint: "https://s3.dussim.xyz",
        bucket: "jmh-results",
        region: "us-east-1",
        path_style: true,
        source: "api/feature/benchmark/build/reports/**/results.json",
        strip_prefix: "api/feature/benchmark/build/reports/",
        target: "/${CI_PIPELINE_NUMBER}-${CI_COMMIT_SHA}",
        access_key: { from_secret: "S3_ACCESS_KEY" },
        secret_key: { from_secret: "S3_SECRET_KEY" },
      },
    },
    {
      name: "visualizerLinks",
      image: lib.buildImage,
      when: { status: ["success", "failure"] },
      commands: [
        "cd api/feature/benchmark/build/reports",
        "find . -name results.json -printf '%P\\n' | while read -r file; do echo \"https://jmh.morethan.io/?source=https://s3.%64ussim.xyz/jmh-results/${CI_PIPELINE_NUMBER}-${CI_COMMIT_SHA}/$${file}\"; done",
      ],
    },
  ],
}
