local lib = import "lib.libsonnet";

local s3Secrets = {
  S3_ACCESS_KEY: { from_secret: "S3_ACCESS_KEY" },
  S3_SECRET_KEY: { from_secret: "S3_SECRET_KEY" },
};

// Runs the single-file Java program in the build image, which already has a JDK.
local results(name, command) = {
  name: name,
  image: lib.buildImage,
  commands: ["java .ci/scripts/JmhResults.java " + command],
};

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
    lib.gradle("stableRegistryJmh", ["gradle stableRegistryJmh"]),
    lib.gradle("stableCycleJmh", ["gradle stableCycleJmh"]),
    results("uploadResults", "upload") + {
      when: { status: ["success", "failure"] },
      environment: s3Secrets,
    },
    results("visualizerLinks", "links") + {
      when: { status: ["success", "failure"] },
    },
    // Only runs after every benchmark step passed.
    results("updateLatest", "update-latest") + {
      environment: s3Secrets,
    },
  ],
}
