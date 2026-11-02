---
  layout: default.md
  title: "Ananya Kharbanda's Project Portfolio Page"
---

# Ananya Kharbanda

## Project: TeachAssist

TeachAssist is a Java desktop application that helps teaching assistants manage student records, group labels, remarks, and attendance through a command-line interface and a JavaFX graphical interface.

## Contributions

My work covers student listing and remark editing, UI improvements, application reliability, testing, and project documentation. The contributions below link to merged pull requests, project commits, and issue reports. Related issues are grouped where they describe the same work.

### Features

* Implemented student listing, including restoring the complete list after searching, validating arguments, handling an empty list, and aligning command feedback with the feature specification ([issue #65](https://github.com/AY2627S1-CS2103T-F12-1/tp/issues/65), [issue #67](https://github.com/AY2627S1-CS2103T-F12-1/tp/issues/67), [issue #95](https://github.com/AY2627S1-CS2103T-F12-1/tp/issues/95), [issue #99](https://github.com/AY2627S1-CS2103T-F12-1/tp/issues/99), [PR #68](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/68), [PR #96](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/96), [PR #100](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/100)).
* Added support for editing and clearing student remarks, preserving remarks when omitted, and validating command parameters ([PR #66](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/66)).
* Integrated group-label filtering into the find command while preserving exact, case-insensitive label matching ([PR #130](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/130)).
* Removed the redundant label command, with labels assigned through the add command ([issue #120](https://github.com/AY2627S1-CS2103T-F12-1/tp/issues/120), [PR #121](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/121)).

### UI and usability

* Made long student names and email addresses wrap within student cards ([issue #133](https://github.com/AY2627S1-CS2103T-F12-1/tp/issues/133), [PR #134](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/134)).
* Adjusted the application window to fit scaled displays ([issue #135](https://github.com/AY2627S1-CS2103T-F12-1/tp/issues/135), [PR #136](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/136)).
* Fixed long group labels clipping student details in narrow windows ([issue #214](https://github.com/AY2627S1-CS2103T-F12-1/tp/issues/214), [PR #220](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/220)).
* Kept attendance badges visible beside long student names ([issue #215](https://github.com/AY2627S1-CS2103T-F12-1/tp/issues/215), [PR #222](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/222)).
* Made long attendance headings display the group and date ([issue #216](https://github.com/AY2627S1-CS2103T-F12-1/tp/issues/216), [PR #223](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/223)).
* Pointed the Help window to the TeachAssist User Guide and restored it when reopened after being minimized ([issue #131](https://github.com/AY2627S1-CS2103T-F12-1/tp/issues/131), [issue #240](https://github.com/AY2627S1-CS2103T-F12-1/tp/issues/240), [PR #132](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/132), [PR #250](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/250)).
* Fixed the attendance list overflowing below a wrapped heading ([commit `6dabbd2e`](https://github.com/AY2627S1-CS2103T-F12-1/tp/commit/6dabbd2e7c4100bbed90a08b14e25a2ac369640b)).

### Startup, storage, and privacy

* Preserved existing Unicode labels when upgrading to lowercase label storage, with a regression test covering loading, saving, and reloading the data ([PR #294](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/294)).
* Fixed startup crashes caused by null JSON data and file-logging failures ([issue #124](https://github.com/AY2627S1-CS2103T-F12-1/tp/issues/124), [issue #126](https://github.com/AY2627S1-CS2103T-F12-1/tp/issues/126), [PR #125](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/125), [PR #127](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/127)).
* Kept application files beside the JAR and corrected packaged-path tests and coverage reporting ([issue #128](https://github.com/AY2627S1-CS2103T-F12-1/tp/issues/128), [PR #129](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/129)).
* Rejected malformed student-data JSON, duplicate keys, and invalid field types ([issue #234](https://github.com/AY2627S1-CS2103T-F12-1/tp/issues/234), [PR #244](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/244)).
* Added protection against concurrent application instances overwriting student data ([issue #235](https://github.com/AY2627S1-CS2103T-F12-1/tp/issues/235), [PR #245](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/245)).
* Removed student details and remarks from diagnostic logs ([issue #237](https://github.com/AY2627S1-CS2103T-F12-1/tp/issues/237), [PR #252](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/252)).
* Blocked attendance reports when student data failed to load ([issue #238](https://github.com/AY2627S1-CS2103T-F12-1/tp/issues/238), [PR #248](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/248)).
* Fixed JavaFX compatibility with native Apple Silicon Java ([issue #236](https://github.com/AY2627S1-CS2103T-F12-1/tp/issues/236), [PR #246](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/246)).

### Testing and bug reporting

* Added the original remark-editing tests for clearing remarks, parameter order, unknown prefixes, and the remark-length boundary in [PR #66](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/66) ([original implementation](https://github.com/AY2627S1-CS2103T-F12-1/tp/commit/da5ac3850fbd69489c8d6d8b53b6e38c57206832)). Sam subsequently revised these tests for Student ID targeting and added further cases and comments; the current tests include both contributions.
* Made JavaFX test initialization reliable and bounded so initialization failures or timeouts fail explicitly ([issue #239](https://github.com/AY2627S1-CS2103T-F12-1/tp/issues/239), [PR #251](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/251)).
* Recorded the team's alpha-testing task split and completion of my assigned testing areas: adding and deleting students, marking and unmarking attendance, viewing attendance, and clearing records ([issue #190](https://github.com/AY2627S1-CS2103T-F12-1/tp/issues/190)).
* Reported reproducible alpha bugs involving concurrent data overwrites, clipped student details, hidden attendance badges, truncated attendance headings, and pasted tab separators ([issue #213](https://github.com/AY2627S1-CS2103T-F12-1/tp/issues/213), [issue #214](https://github.com/AY2627S1-CS2103T-F12-1/tp/issues/214), [issue #215](https://github.com/AY2627S1-CS2103T-F12-1/tp/issues/215), [issue #216](https://github.com/AY2627S1-CS2103T-F12-1/tp/issues/216), [issue #217](https://github.com/AY2627S1-CS2103T-F12-1/tp/issues/217)).
* Added application lifecycle tests and improved packaged-path and logging-subprocess coverage ([commit `d496be75`](https://github.com/AY2627S1-CS2103T-F12-1/tp/commit/d496be75229f7f5a34adc3083f6525ec90330738), [commit `058313fd`](https://github.com/AY2627S1-CS2103T-F12-1/tp/commit/058313fd5041a36cee81a8d11dc9db4b5c277cb5), [commit `673827f6`](https://github.com/AY2627S1-CS2103T-F12-1/tp/commit/673827f627fb7a0fef5ce7832b4e01930a13b170)).

The tab-separator report in issue #217 was resolved by Tristan's documentation changes in [PR #219](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/219); my contribution was identifying and reporting the problem. The concurrent-overwrite report in issue #213 was initially addressed by the same documentation PR; I later implemented write protection under issue #235 and PR #245.

### Documentation and project maintenance

* Corrected UML diagram titles, method-call receivers, activity guards, architecture dependencies, and parser-label visibility ([PR #295](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/295), [PR #296](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/296), [PR #298](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/298), [PR #299](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/299), [PR #300](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/300)); removed obsolete UML sources ([PR #301](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/301)).
* Corrected Developer Guide label syntax and the undo user story, and updated the README and homepage feature summaries ([PR #302](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/302), [PR #303](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/303), [PR #304](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/304)).
* Refreshed the application screenshot, clarified Mac JAR launch instructions, and corrected the Help-window release notes ([PR #305](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/305), [PR #306](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/306), [PR #307](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/307)).
* Updated the project README ([issue #35](https://github.com/AY2627S1-CS2103T-F12-1/tp/issues/35), [PR #37](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/37)).
* Added the Student Listing sequence diagram and its PlantUML source ([issue #97](https://github.com/AY2627S1-CS2103T-F12-1/tp/issues/97), [PR #98](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/98)).
* Cleaned up TeachAssist branding, website content, setup documentation, portfolio placeholders, and student-facing terminology ([issue #160](https://github.com/AY2627S1-CS2103T-F12-1/tp/issues/160), [issue #161](https://github.com/AY2627S1-CS2103T-F12-1/tp/issues/161), [PR #163](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/163)).
* Completed and corrected the User Guide, Developer Guide, testing guidance, and architecture diagrams ([issue #167](https://github.com/AY2627S1-CS2103T-F12-1/tp/issues/167), [PR #168](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/168)).
* Rewrote the Developer Guide use cases to cover all implemented commands, shared failure handling, and recovery behaviour; simplified the UML explanations in [PR #266](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/266) ([implementation commit](https://github.com/AY2627S1-CS2103T-F12-1/tp/commit/e0949ad043024698e0b00636301fd338fe8ecedd)).
* Authored four UML diagrams in that PR: [saving before publishing](https://github.com/AY2627S1-CS2103T-F12-1/tp/blob/master/docs/diagrams/SaveSequenceDiagram.puml), [attendance report refresh](https://github.com/AY2627S1-CS2103T-F12-1/tp/blob/master/docs/diagrams/AttendanceRefreshSequenceDiagram.puml), [student data classes](https://github.com/AY2627S1-CS2103T-F12-1/tp/blob/master/docs/diagrams/StudentDataClassDiagram.puml), and [attendance model classes](https://github.com/AY2627S1-CS2103T-F12-1/tp/blob/master/docs/diagrams/AttendanceModelClassDiagram.puml).
* Aligned guide expectations with undo behaviour and startup loading failures ([issue #241](https://github.com/AY2627S1-CS2103T-F12-1/tp/issues/241), [PR #249](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/249)).
* Updated the application screenshot and corrected the label-search activity diagram ([issue #253](https://github.com/AY2627S1-CS2103T-F12-1/tp/issues/253), [PR #254](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/254)).
* Added my profile photo and updated my About Us entry and portfolio ([issue #10](https://github.com/AY2627S1-CS2103T-F12-1/tp/issues/10), [issue #26](https://github.com/AY2627S1-CS2103T-F12-1/tp/issues/26), [PR #19](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/19), [PR #30](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/30)).

### Authorship verification

The [course RepoSense report](https://nus-cs2103-ay2627-s1.github.io/tp-dashboard/?search=ananyakharbanda)
generated on 2 November 2026 at 11:41 SGT recognises my Git author name, `Ananya Kharbanda`, under
`ananyakharbanda`. This was checked against the report's commit and per-file attribution data and the repository history.

| Area reviewed | Attribution evidence |
|---------------|----------------------|
| Student listing | The report credits my listing feedback and `ListCommandParser`, matching [the original implementation](https://github.com/AY2627S1-CS2103T-F12-1/tp/commit/b4304e7fb4e36ee61a2c1106e152633592478b85) and subsequent listing fixes. |
| Label-search integration | My `FindCommandParser` integration and `LabelMatchesPredicate` changes remain credited, including [the integration commit](https://github.com/AY2627S1-CS2103T-F12-1/tp/commit/01ef08ba6c40b27b0a62e116bad30b1f390d732f). |
| Reliability fixes | The report credits my work in `AppPaths`, startup handling, JSON validation, concurrent-write protection, and private diagnostic logging; the corresponding PRs are linked above. |
| Regression tests | My contributions remain visible in listing, label-search, application lifecycle, logging, JSON validation, and storage tests. |

The review found no missing account alias. My contributions are documented through Git history and the
linked pull requests, including the original remark-editing implementation in
[PR #66](https://github.com/AY2627S1-CS2103T-F12-1/tp/pull/66).

[Team responsibilities](../AboutUs.md)
