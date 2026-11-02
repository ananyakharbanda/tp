---
  layout: default.md
  title: "Configuration guide"
---

# Configuration guide

TeachAssist stores student data in `data/addressbook.json`, preferences in `preferences.json`, and logs in
`addressbook.log.*`, all relative to the folder containing the running JAR. Launching the JAR from another directory
does not change these locations. When running compiled classes from an IDE or Gradle, the working directory is used.

`AppPaths` resolves these paths without initializing logging. Log-file patterns escape literal percent signs in folder
names so that `FileHandler` does not interpret them as pattern tokens. The logging level is configured through
`LOG_LEVEL` in `LogsCenter`; there is no `config.json` file. Changing this constant requires a rebuild.

Window size and position are stored under `guiSettings` in `preferences.json` and saved when TeachAssist exits.
See [Saving the data](UserGuide.md#saving-the-data) for student persistence and the [Logging guide](Logging.md) for logging details.
