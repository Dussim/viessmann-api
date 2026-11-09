local lib = import "lib.libsonnet";

local when = [
  { event: "manual" },
  { event: "push", branch: "master" },
];

{
  name: "jmh",
  clone: lib.shallowClone,
  steps: [
    lib.gradle("jmh", ["gradle jmh"], when),
    lib.gradle("stableValidationJmh", ["gradle stableValidationJmh"], when),
    {
      name: "uploadResults",
      image: "woodpeckerci/plugin-s3:1.5.6",
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
      when: when,
    },
    {
      name: "visualizerLinks",
      image: lib.buildImage,
      commands: [
        "cd api/feature/benchmark/build/reports",
        "find . -name results.json -printf '%P\\n' | while read -r file; do echo \"https://jmh.morethan.io/?source=https://s3.dussim.xyz/jmh-results/${CI_PIPELINE_NUMBER}-${CI_COMMIT_SHA}/$${file}\"; done",
      ],
      when: when,
    },
  ],
}
