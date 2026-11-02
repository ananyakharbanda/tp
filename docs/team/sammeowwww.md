---
  layout: default.md
  title: "Samuel Lau's Project Portfolio Page"
---

# Samuel Lau

## Project: TeachAssist

TeachAssist is a Java desktop application that helps teaching assistants manage student records, group labels, remarks,
and attendance through a command-line interface and a JavaFX graphical interface.

## Selected contributions

The following are selected contributions supported by commits and merged pull requests in the project history.

### Features and data model

* Implemented the add-student workflow, including field validation, specific error feedback, and duplicate Student ID
  checks ([PR #63](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/63)).
* Implemented deletion by Student ID, including students hidden by a search filter
  ([commit `39db8eda`](https://github.com/AY2627S1-CS2103T-F12-1/tp/commit/39db8edaa6f96a566f6f64e59524c22160fdccb3),
  [PR #64](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/64)).
* Added optional student remarks to the model, commands, JSON storage, and student cards
  ([PR #53](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/53)).
* Removed obsolete phone and address fields, then removed tags after group labels replaced them
  ([PR #57](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/57),
  [PR #91](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/91)).
* Enabled the edit command to add and remove group labels
  ([PR #122](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/122)).
* Added attendance records and JSON persistence, then implemented the mark command
  ([PR #140](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/140),
  [PR #141](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/141)).
* Normalized group labels to lowercase across commands, attendance, JSON storage, and the UI, with regression tests
  (commit `94eca132`).

### Reliability and code quality

* Saved student changes before updating the live model, so a failed save does not apply the proposed change
  ([commit `44ca1cd7`](https://github.com/AY2627S1-CS2103T-F12-1/tp/commit/44ca1cd7569b56f51d4a4a30e3bbaa000c90c624)).
* Protected student data after startup loading failures
  ([commit `44a6fdc5`](https://github.com/AY2627S1-CS2103T-F12-1/tp/commit/44a6fdc56f473e444e137c09dac66b1080669721)).
* Fixed misleading list and find results after loading failure, attendance rows with long names, and the visual
  distinction between Student IDs and group labels
  ([PR #225](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/225)).
* Added outcome logging for the add and mark commands
  ([PR #179](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/179)).
* Refactored commands, parsers, and model classes for clearer responsibilities and defensive checks, and expanded
  related tests ([PR #212](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/212)).
* Improved add, delete, and edit test coverage using equivalence partitioning and boundary value analysis
  ([PR #174](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/174)).

### Documentation

* Updated the Developer Guide's target user profile, value proposition, and user stories
  ([PR #42](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/42)).
* Updated the User Guide and Developer Guide for add and delete, including their execution sequence diagrams
  ([commit `f4c38f50`](https://github.com/AY2627S1-CS2103T-F12-1/tp/commit/f4c38f50ffb1817985a1c50bd571ced2c80c97d4),
  [PR #92](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/92)).
* Documented add and delete error-message precedence and the edit-label execution sequence
  ([PR #106](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/106),
  [PR #154](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/154)).
* Updated the User Guide screenshot and clarified attendance and data-file recovery guidance
  ([PR #123](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/123),
  [PR #256](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/256)).
* Clarified remark prefix parsing and explained how attendance is retained and can be unmarked after label removal
  (commits `6f4feb55`, `b4d3ba54`, `75d00d9a`).
* Improved the edit, mark, and unmark sequence diagrams and their implementation walkthroughs
  (commits `0e4d6f0d`, `dfdf5568`).

[Team responsibilities](../AboutUs.md)
