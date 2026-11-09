local secrets = [
  "BUILD_CACHE_USER",
  "BUILD_CACHE_USER_PASSWORD",
  "REPO_PASSWORD",
  "REPO_USERNAME",
];

{
  buildImage:: "dussim/build-env:jdk27_gradle-9.8.2",

  shallowClone:: [
    {
      name: "clone",
      image: "codefloe.com/crow-plugins/clone",
      settings: {
        partial: true,
        depth: 1,
        lfs: false,
        recursive: false,
      },
    },
  ],

  gradle(name, commands, when):: {
    name: name,
    image: $.buildImage,
    environment: {
      CI: "true",
      OPEN_API_PATH: "/mnt/viessmann-api/openApi",
    } + {
      [secret]: { from_secret: secret }
      for secret in secrets
    },
    commands: commands,
    when: when,
  },
}
