local lib = import "lib.libsonnet";

{
  name: "build",
  clone: lib.shallowClone,
  steps: [
    lib.gradle(
      "jvmTest",
      ["gradle jvmTest --scan"],
      { event: "push" },
    ),
  ],
}
