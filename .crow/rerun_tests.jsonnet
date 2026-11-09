local lib = import "lib.libsonnet";

{
  name: "rerun_tests",
  when: { event: "manual" },
  steps: [
    lib.gradle("jvmTest", ["gradle jvmTest --scan --rerun-tasks"]),
  ],
}
