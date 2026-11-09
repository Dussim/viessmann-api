local lib = import "lib.libsonnet";

{
  name: "rerun_tests",
  steps: [
    lib.gradle(
      "jvmTest",
      ["gradle jvmTest --scan --rerun-tasks"],
      { event: "manual" },
    ),
  ],
}
