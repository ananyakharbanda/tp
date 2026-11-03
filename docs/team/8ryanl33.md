---
  layout: default.md
  title: "Bryan Lee Jun You's Project Portfolio Page"
---

# Bryan Lee Jun You

## Project: TeachAssist

TeachAssist is a Java desktop application that helps teaching assistants manage student records, group labels, remarks,
and attendance through a command-line interface and a JavaFX graphical interface.

## Selected contributions

The following are selected contributions supported by commits and merged pull requests in the project history.

### Features

* Implemented the read-only `attendance` command, which shows every current member of a group as Present or Absent
  for a date, with totals, including students hidden by a search
  ([PR #150](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/150)).
* Showed the attendance view in place of the student list, refreshing it after a successful `mark` and closing it
  after other successful commands ([PR #150](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/150)).
* Showed the outcome of loading student data when the app opens: the number of students loaded, that no data file
  exists yet, or why loading failed ([PR #80](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/80),
  [PR #110](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/110)).

### Data loading and saving

* Reported unreadable data files separately from files with invalid contents
  ([PR #75](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/75)).
* Reported the duplicate Student ID when loading a file with repeated IDs, instead of a generic error
  ([PR #73](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/73),
  [PR #78](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/78)).
* Reported a specific error when the data folder cannot be created
  ([PR #81](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/81)).
* Saved student data after label changes, which were previously lost on restart
  ([PR #71](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/71)).

### Bug fixes

* Kept the active filter unwrapped across data changes, which had made filtering slower after every `mark`,
  `unmark`, `delete`, or `clear` under a search ([PR #228](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/228)).
* Showed the remark in the edit success message
  ([PR #229](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/229)).
* Skipped undo snapshots for commands that change nothing, so `undo` always reverses a real change
  ([PR #230](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/230)).
* Listed labels alphabetically ignoring case in the edit message and student cards
  ([PR #231](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/231)).
* Showed the startup error dialog when the main window fails to initialise, instead of exiting without a message
  ([PR #232](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/232)).

### Testing

* Tested the attendance workflow end to end, including restarting through the normal startup path, and the
  attendance view's totals and Present/Absent badges
  ([PR #158](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/158)).
* Covered attendance code in tests and ran the JavaFX UI tests on the Linux CI runner under a virtual display
  ([commit `210fe2d9`](https://github.com/AY2627S1-CS2103T-F12-1/tp/commit/210fe2d9c1906f71b2de78745505132cc801e3f7)).

### Documentation

* User Guide: documented startup loading messages and the `attendance` command
  ([PR #112](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/112),
  [PR #158](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/158)).
* Developer Guide: described student data loading and viewing attendance, with sequence diagrams, design
  considerations, and use cases ([PR #114](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/114),
  [PR #158](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/158)).
* Developer Guide: added the initial non-functional requirements
  ([PR #38](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/38)).

[Team responsibilities](../AboutUs.md)
