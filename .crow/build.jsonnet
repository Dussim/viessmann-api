local lib = import "lib.libsonnet";

{
  name: "build",
  when: { event: "push" },
  clone: lib.shallowClone,
  steps: [
    lib.gradle("jvmTest", ["gradle jvmTest --scan"]),
  ],
}
