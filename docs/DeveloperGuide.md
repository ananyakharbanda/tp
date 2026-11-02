---
  layout: default.md
  title: "Developer Guide"
  pageNav: 3
---

# TeachAssist Developer Guide

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## **Acknowledgements**

* TeachAssist's initial codebase, architecture and documentation structure are adapted from [AddressBook Level 3](https://github.com/se-edu/addressbook-level3), created by the [SE-EDU initiative](https://se-education.org).
* Application libraries: [JavaFX](https://openjfx.io/) for the graphical interface and [Jackson](https://github.com/FasterXML/jackson) for JSON persistence.
* Testing and build tools: [JUnit 5](https://junit.org/junit5/), [Gradle](https://gradle.org/), [Shadow](https://gradleup.com/shadow/), [Checkstyle](https://checkstyle.org/) and [JaCoCo](https://www.jacoco.org/jacoco/).
* Documentation tools: [MarkBind](https://markbind.org/) for the website and [PlantUML](https://plantuml.com/) for UML diagrams.
* Ananya Kharbanda used an AI coding assistant for implementation suggestions, regression tests, code review,
  UML revisions, and documentation checks. Suggested changes were checked against the implementation and
  validated using relevant automated tests, code-style checks, diagram rendering, and documentation builds.
* OpenAI Codex assisted @sammeowwww with implementation code, test cases, sequence diagrams, and documentation for
  student, label, and attendance features. The contributor gave explicit requirements, reviewed the generated changes
  against the intended behavior, and validated them with automated tests, Checkstyle, and manual testing before
  accepting them.
* OpenAI Codex assisted @DaRealTristan with implementation code, test cases, sequence diagrams, and documentation for 
  find and unmark features as well as bug fixes for storage. The contributor gave explicit requirements, 
  reviewed the generated changes against the intended behavior, and validated them with automated tests, Checkstyle, 
  and manual testing before accepting them.
* OpenAI Codex assisted @amberrrruby with implementing test cases, functional code, UML diagrams and documentation.
  TDD is used throughout, and generated changes are verified to fit the intended behavior with implemented tests.
  Diagrams and documentation are manually checked later.

--------------------------------------------------------------------------------------------------------------------

## **Setting up, getting started**

Refer to the guide [_Setting up and getting started_](SettingUp.md).

--------------------------------------------------------------------------------------------------------------------

## **Design**

<!-- @@author ananyakharbanda -->
The diagrams use three distinct views. Component diagrams describe responsibilities and dependencies; class
and sequence diagrams describe selected implementation relationships; activity diagrams describe decisions in a
workflow. Each caption states the scope and any omitted steps. A message written as an action in a component
sequence is conceptual, while messages in an object sequence use the implemented operations.
<!-- @@author -->

### Architecture

<!-- @@author ananyakharbanda -->
<puml src="diagrams/ArchitectureDiagram.puml" alt="Dependencies between application components" />
<!-- @@author -->

The ***Architecture Diagram*** given above explains the high-level design of the App.

The following provides a quick overview of the main components and their interactions.

**Main components of the architecture**

**`Main`** (consisting of classes [`Main`](https://github.com/AY2627S1-CS2103T-F12-1/tp/blob/master/src/main/java/seedu/address/Main.java) and [`MainApp`](https://github.com/AY2627S1-CS2103T-F12-1/tp/blob/master/src/main/java/seedu/address/MainApp.java)) is in charge of the app launch and shut down.
* At app launch, it initializes the other components in the correct sequence, and connects them up with each other.
* At shut down, it shuts down the other components and invokes cleanup methods where necessary.

The bulk of the app's work is done by the following four components:

* [**`UI`**](#ui-component): The UI of the App.
* [**`Logic`**](#logic-component): The command executor.
* [**`Model`**](#model-component): Holds the data of the App in memory.
* [**`Storage`**](#storage-component): Reads data from, and writes data to, the hard disk.

[**`Commons`**](#common-classes) represents a collection of classes used by multiple other components.

**How the architecture components interact with each other**

<!-- @@author ananyakharbanda -->
The component sequence below follows a valid `delete i/A0123456B` request. It shows only the responsibilities of
UI, Logic, Model, and Storage. Parsing, pending-model construction, and filesystem operations are expanded in
the implementation section.

<puml src="diagrams/ArchitectureSequenceDiagram.puml" alt="Deletion at component level" />
<!-- @@author -->

Each of the four main components (also shown in the diagram above),

* defines its *API* in an `interface` with the same name as the Component.
* provides its functionality through a concrete `{Component Name}Manager` class that implements the corresponding API interface.

For example, the `Logic` component defines its API in `Logic.java` and implements it in `LogicManager.java`. Other components interact with a component through its interface rather than its concrete class, preventing them from coupling to that component's implementation, as illustrated in the following partial class diagram.

<!-- @@author ananyakharbanda -->
<puml src="diagrams/ComponentManagers.puml" alt="Component implementations and interfaces" />
<!-- @@author -->

The sections below give more details of each component.

### UI component

<!-- @@author ananyakharbanda -->
The **API** of this component is specified in [`Ui.java`](https://github.com/AY2627S1-CS2103T-F12-1/tp/blob/master/src/main/java/seedu/address/ui/Ui.java)

The partial class diagram shows UI ownership and object creation. Common `UiPart` inheritance and dependencies
outside the UI package are omitted from the drawing and described below.
<!-- @@author -->

<puml src="diagrams/UiClassDiagram.puml" alt="Structure of the UI Component"/>

The UI consists of a `MainWindow` and its parts, such as `CommandBox`, `ResultDisplay`, `PersonListPanel`, `AttendanceListPanel`, and `StatusBarFooter`. All of these, including `MainWindow`, inherit from the abstract `UiPart` class, which captures common behavior among classes that represent visible GUI parts.

<!-- @@author ananyakharbanda -->
The `UI` component uses the JavaFX UI framework. The layouts of these UI parts are defined in matching `.fxml` files in `src/main/resources/view`. For example, [`MainWindow.fxml`](https://github.com/AY2627S1-CS2103T-F12-1/tp/blob/master/src/main/resources/view/MainWindow.fxml) specifies the layout of [`MainWindow`](https://github.com/AY2627S1-CS2103T-F12-1/tp/blob/master/src/main/java/seedu/address/ui/MainWindow.java).
<!-- @@author -->

The `UI` component,

* executes user commands using the `Logic` component.
* listens for changes to `Model` data so that the UI can be updated with the modified data.
* keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
* depends on some classes in the `Model` component because it displays `Person` objects and `AttendanceReport` objects from the model.
* shows an `AttendanceListPanel` in place of the `PersonListPanel` while a command result carries an attendance report. See [Viewing attendance](#viewing-attendance).

### Logic component

<!-- @@author ananyakharbanda -->
**API** : [`Logic.java`](https://github.com/AY2627S1-CS2103T-F12-1/tp/blob/master/src/main/java/seedu/address/logic/Logic.java)
<!-- @@author -->

Here's a (partial) class diagram of the `Logic` component:

<!-- @@author ananyakharbanda -->
<puml src="diagrams/LogicClassDiagram.puml" alt="Logic classes and component interfaces" />

The [add](#adding-student-information) and [delete](#deleting-student-information) diagrams show command execution
on a pending model. The separate [save-before-publish diagram](#saving-before-publishing-changes) shows how
`LogicManager` saves and publishes those changes.
<!-- @@author -->


How the `Logic` component works:

1. When `Logic` is called upon to execute a command, the command is passed to an `AddressBookParser` object, which in turn creates a parser that matches the command (e.g., `DeleteCommandParser`) and uses it to parse the command.
1. This results in a `Command` object (more precisely, an object of one of its subclasses e.g., `DeleteCommand`) which is executed by the `LogicManager`.
1. A read-only command queries or filters the live model. A modifying command runs on a pending model;
   `LogicManager` saves the pending records before publishing them. Undo uses its separate restoration path.
1. The result of the command execution is encapsulated as a `CommandResult` object which is returned from `Logic`.

<!-- @@author ananyakharbanda -->
The parsing class diagram uses the implemented `AddCommandParser` as a representative. Other command parsers
use the same `Parser` interface; their individual dependencies are omitted.

<puml src="diagrams/ParserClasses.puml" alt="Parser classes illustrated by AddCommandParser" />
<!-- @@author -->

How the parsing works:
* When called upon to parse a user command, the `AddressBookParser` class creates an `XYZCommandParser` (`XYZ` is a placeholder for the specific command name, e.g., `AddCommandParser`). The parser uses the other classes shown above to parse the user command and create an `XYZCommand` object (e.g., `AddCommand`). The `AddressBookParser` returns that object as a `Command` object.
* All `XYZCommandParser` classes, such as `AddCommandParser` and `DeleteCommandParser`, implement the `Parser` interface so they can be treated similarly where appropriate, for example during testing.

### Model component
**API** : [`Model.java`](https://github.com/AY2627S1-CS2103T-F12-1/tp/blob/master/src/main/java/seedu/address/model/Model.java)

<!-- @@author ananyakharbanda -->
The first class diagram shows model ownership and interfaces. Student fields are expanded in the second;
attendance-report classes are shown under [Reporting attendance](#reporting-attendance).

<puml src="diagrams/ModelClassDiagram.puml" alt="Model ownership and read-only interfaces" />

<puml src="diagrams/StudentDataClassDiagram.puml" alt="Student fields and persisted attendance" />
<!-- @@author -->


The `Model` component,

* stores the student data i.e., all `Person` objects (which are contained in a `UniquePersonList` object).
* uses `studentId` as the domain identifier for `Person` duplicate checks. The JavaFX `id` label in `PersonListCard.fxml` is only the displayed list index, not the student's identifier.
* stores group labels as `Label` objects inside each `Person`.
* stores attendance as `AttendanceRecord` objects (a date and a group) inside each `Person`, and builds `AttendanceReport` objects from the student list on request.
  Each report contains `AttendanceEntry` values pairing a student with their presence status. Reports are derived
  by the logic layer using `AttendanceReport#of`; they are not saved in the address book.
* stores the `Person` objects selected by the current filter, such as search results, in a separate _filtered_ list. It exposes this list as an unmodifiable `ObservableList<Person>` that the UI can observe and bind to, so the UI updates when the list changes.
* stores a `UserPrefs` object that represents the user's preferences (currently, just the GUI settings). This is exposed to the outside as a `ReadOnlyUserPrefs` object.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)


<box type="info" seamless>

<!-- @@author ananyakharbanda -->
**Alternative (not implemented):** The design below keeps a unique list of labels in `AddressBook`, and each `Person` references values from that list. This lets `AddressBook` maintain one value object per unique label instead of each `Person` holding its own objects.<br>
<!-- @@author -->

<puml src="diagrams/BetterModelClassDiagram.puml" alt="Unimplemented alternative with a shared label collection" />
</box>


### Storage component

<!-- @@author ananyakharbanda -->
**API** : [`Storage.java`](https://github.com/AY2627S1-CS2103T-F12-1/tp/blob/master/src/main/java/seedu/address/storage/Storage.java)

<puml src="diagrams/StorageClassDiagram.puml" alt="Storage classes and JSON adapters" />
<!-- @@author -->

The `Storage` component,
* can save both student data and user preference data in JSON format, and read them back into corresponding objects.
* expects each stored `Person` JSON object to contain a valid `studentId`. There is currently no migration or default-value strategy for old data files missing this field.
* stores group labels through `JsonAdaptedLabel`. Missing or null `labels` fields are treated as empty label sets for compatibility with older data files.
* ignores legacy `tags` fields when loading older data files and omits them when saving.
* reports loading failures as `DataLoadingException`, distinguishing unreadable files, invalid data, and duplicate Student IDs. See [Loading student data at startup](#loading-student-data-at-startup).
* writes student data atomically and reports a data folder that cannot be created as `DataStorageLocationException`. See [Saving before publishing changes](#saving-before-publishing-changes).
* is implemented by `StorageManager`, which delegates the actual JSON file access to `JsonAddressBookStorage` and `JsonUserPrefsStorage` (one class per data file).
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)

### Common classes

Classes used by multiple components are in the `seedu.address.commons` package.

--------------------------------------------------------------------------------------------------------------------

## **Implementation**

This section describes some noteworthy details on how certain features are implemented.

### Group labels

<!-- @@author ananyakharbanda -->
An optional group label is assigned when creating a student with `add ... l/LABEL_NAME`.
`Label` validates the value, and `ParserUtil#parseLabel` trims command input before constructing it.
`JsonAdaptedLabel` and `JsonAdaptedPerson` persist labels under each student's `labels` JSON field.
`EditCommand` preserves labels unless label changes are requested, and `PersonCard` renders them in its `FlowPane`.
Existing saved labels remain compatible, including records containing multiple labels.
<!-- @@author -->

### Edit command

Format: `edit i/STUDENT_ID [n/NAME] [e/EMAIL] [r/REMARK] [l/LABEL_NAME]... [dl/LABEL_NAME]...`

The diagram follows a successful edit that replaces `tutorial 1` with `tutorial 2`, from parsing through the
pending-model update. It expands Logic while keeping Model at a high level. The reference frame points to the
shared [save-before-publish flow](#saving-before-publishing-changes); error paths are omitted.

<!-- @@author ananyakharbanda -->
<puml src="diagrams/EditLabelSequenceDiagram.puml" alt="EditCommand updates labels on the pending model" />
<!-- @@author -->

1. `EditCommandParser` checks the command structure and parses the Student ID and requested changes.
   It collects repeated `l/` and `dl/` values in an `EditPersonDescriptor`.
2. `EditCommand` reads the complete student list and finds the target by Student ID. A filtered view
   does not affect which student is edited.
3. The command builds a replacement `Person`, retaining fields not supplied in the command, the Student ID,
   and attendance. It copies the existing labels, removes each `dl/` label, then adds each `l/` label.
4. Once the changes are valid, the command replaces the student in the pending model and shows the full
   student list. `LogicManager` saves the pending model before publishing it to the live model, as described
   in the shared [save-before-publish flow](#saving-before-publishing-changes).

Label names are lowercased when constructed, so matching remains case-insensitive. Removing a missing label or
adding one already present throws a
`CommandException`, leaving the record unchanged. An empty `l/` clears all labels and cannot be combined
with another label change. An empty `r/` becomes an empty remark.

### Mark and unmark attendance

Command formats:

```text
mark i/STUDENT_ID d/YYYY-MM-DD l/GROUP
unmark i/STUDENT_ID d/YYYY-MM-DD l/GROUP
```

The accepted inputs, examples, and feedback are documented under
[Recording attendance](UserGuide.md#recording-attendance-mark) and
[Removing attendance](UserGuide.md#removing-attendance-unmark) in the User Guide.

The diagrams follow successful `mark` and `unmark` commands from parsing through the pending-model update.
They expand Logic while keeping Model at a high level. The pending copy retains the current student filter.
The reference frames point to the shared [save-before-publish flow](#saving-before-publishing-changes);
error paths are omitted.

<!-- @@author ananyakharbanda -->
<puml src="diagrams/MarkSequenceDiagram.puml" alt="MarkCommand adds attendance to the pending model" />

<puml src="diagrams/UnmarkSequenceDiagram.puml" alt="UnmarkCommand removes attendance from the pending model" />
<!-- @@author -->

<!-- @@author sammeowwww -->
#### Walkthrough

Each `Person` owns a set of `AttendanceRecord` values covering all tutorial groups for which that student was
marked present. Each record contains a date and a group. The commands search only the target student's set for
a record with the requested date and group; they do not search a separate attendance list for each tutorial.

1. `AddressBookParser` routes the input to `MarkCommandParser` or `UnmarkCommandParser`. The parsers accept `i/`,
   `d/`, and `l/` in any order, each exactly once and with a non-empty value. They reuse `ParserUtil` to validate
   the Student ID, a real `YYYY-MM-DD` date, and the group label before creating the command.
2. The command searches the complete student list by Student ID, including students hidden by the active filter.
   A missing student causes a `CommandException` before any record is changed.
3. The commands apply different attendance checks:
   * **Mark:** Find a current group label, then check the student's attendance set for the same date and group.
     Reject a matching record; otherwise, add the new `AttendanceRecord` to a copy of that set.
   * **Unmark:** Find a record with the same date and group in the student's set, then remove it from a copy. Current
     group membership is not required: `EditCommand` retains historical attendance after a label is removed.
     Success feedback uses the matched record's lowercase group name.
4. Each command builds a replacement `Person` with the updated attendance and unchanged other fields, then
   replaces the student in the pending model. Other attendance entries and the active filter remain unchanged.
5. `LogicManager` saves the pending model before publishing it to the live model and refreshing any open attendance
   report. Parsing, command, or save failures leave the live records, filter, and saved file unchanged. A successful
   change can later be reversed with `undo`. The existing attendance JSON format is reused.

The general command parser rejects multiline input first. Command-specific structure checks then reject unknown
prefixes, text before the first prefix, repeated prefixes, and missing or empty values in that order. Repeated and
missing/empty checks follow `i/`, `d/`, `l/` order. See [Attendance parameter errors](#attendance-parameter-errors)
for the resulting messages.
<!-- @@author -->

#### Tests

`MarkCommandTest` and `UnmarkCommandTest` cover record updates and command errors. `UnmarkCommandTest` also
checks that other dates and groups remain, hidden students can be updated, removed group labels do not block
removal, and marking followed by unmarking restores the original student.
The command parser tests cover parameter validation, and `AddressBookParserTest` checks unmark routing.
`LogicManagerTest` covers persistence and reload, failed saves, filter preservation, and startup failure protection.
### Adding student information

Command format:
````text
add n/NAME i/STUDENT_ID e/EMAIL [r/REMARK] [l/LABEL_NAME]
````
Accepted values are documented in the [UG field rules](UserGuide.md#field-rules); exact messages and validation order
are in [Appendix: Errors](#appendix-errors).

#### Validation and identity

* `ArgumentTokenizer` recognises supported prefixes at the start of arguments or after whitespace, including tabs.
* `AddCommandParser` checks line breaks, unknown alphabetic prefixes, preamble text, repeated prefixes,
  missing mandatory fields, then empty mandatory fields. Errors are reported before parsing field values.
  Repeated/missing/empty checks follow `n/`, `i/`, `e/` order; repeated `r/` and `l/` are checked after them.
* `ParserUtil` trims values and converts invalid fields into `ParseException`s. Model constructors also validate,
  so records created by storage or other callers follow the same rules.
* `Name` accepts 1–100 Unicode code points after trimming and collapsing repeated spaces, requires a letter,
  and permits Unicode letters/combining marks, spaces, apostrophes, hyphens, and periods. Case is preserved.
* `StudentId` accepts nine ASCII alphanumeric characters and stores uppercase using `Locale.ROOT`.
  `Person#isSamePerson` compares this normalised ID, not the name or email.
* `Email` enforces the supported ASCII format, local-part limit of 64, overall limit of 254, domain-label limits
  of 63, and a final label of 2–63 letters. Only the domain is lowercased using `Locale.ROOT`.
* `Remark` accepts at most 4,000 Unicode code points; command parsing trims only surrounding whitespace.
  Empty or omitted remarks become empty strings. `r/` can appear anywhere and does not consume later parameters.
* `Label` accepts 1–60 characters and requires at least one letter or digit. For length checking, lowercase
  `i` plus combining dot counts as the single `İ` from which it can expand. It permits
  letters, combining marks, digits, spaces, hyphens, underscores, apostrophes, parentheses, and periods. Empty labels
  are rejected, omitted labels become an empty label set, and `l/` can appear anywhere without consuming later parameters.

For example, `add n/  Alex   Tan i/a0123456b e/Alex@EXAMPLE.COM l/Tutorial 1 r/Quiz: 8/10`
creates a student with name `Alex Tan`, ID `A0123456B`, email `Alex@example.com`, label `tutorial 1`,
and remark `Quiz: 8/10`.
An alphabetic token such as `x/value` is rejected even inside a remark; `8/10` and URLs remain literal text.
A duplicate ID rejects the complete command without merging any details or remarks.

#### Saving before publishing changes

<!-- @@author ananyakharbanda -->
`LogicManager` coordinates this transaction for `add`, `edit`, `delete`, `mark`, `unmark`, and `clear`.
`undo` uses the separate restoration flow described in [Undoing data changes](#undoing-data-changes).

1. Parse the input into a `Command`, then check startup data availability. `Command#isModifyingData` identifies
   data-changing commands; future modifying commands must override it too.
2. Copy the live address book for undo, and create a pending `ModelManager` with the current records, preferences,
   and filter. Execute the command on this pending model. A command error stops the transaction before saving.
3. Pass the pending address book to `Storage#saveAddressBook`. The storage implementation locks the data file's
   sidecar, checks for external changes, and atomically replaces the file as described below.
4. After a successful save, retain the previous address book for undo if the records changed, then publish the
   pending records and filter. Only then return success and refresh or close an open attendance report.
   A save failure leaves the live records, view, and undo history unchanged. The attempted save does not replace
   the destination file, including newer contents written by another window.

The object sequence below starts after parsing and the startup-data check. It shows the shared transaction;
command-specific record updates are expanded in their own diagrams. Calls to retrieve records, preferences, and
filters are omitted to keep the transaction visible. `recordUndoSnapshot` retains at most 100 prior states.

<puml src="diagrams/SaveSequenceDiagram.puml" alt="Execute on a pending model, save, then publish" />
<!-- @@author -->

**Storage conflict detection and replacement**

<!-- @@author ananyakharbanda -->
`JsonAddressBookStorage` normalises the absolute target path and serialises in-process file access with
`FILE_ACCESS_LOCK`. During each save it creates the parent folder, opens the sibling `addressbook.json.lock`,
and calls `FileChannel#tryLock`. An unavailable lock gives `DataFileInUseException`. The lock is released after
saving, but the sidecar file remains so concurrent writers continue locking the same file identity.

While holding the lock, storage writes a complete temporary JSON file beside the destination and reads its bytes.
It compares the destination's current bytes with the bytes remembered from its last successful read or save.
A difference, including a file created or removed by another writer, gives `DataFileChangedException`.
Otherwise, `Files.move` with `ATOMIC_MOVE` and `REPLACE_EXISTING` replaces the destination and the new bytes are
remembered. There is no fallback overwrite when atomic replacement is unsupported. Temporary-file cleanup is
attempted after success or failure; a cleanup failure is logged.

The lock coordinates TeachAssist writers. External editors do not participate in that lock, so users should still
close TeachAssist before editing its data file. This protection applies to student data, not `preferences.json`.

`LogicManager#saveStudentData` maps these exceptions to the distinct messages and recovery actions in
[Saving errors](#saving-errors). A changed file requires restarting to load current data; a busy writer can be retried.
`JsonAddressBookStorageConcurrencyTest` covers stale writers and lock contention.

The add sequence below shows only `AddCommand#execute` on the pending model, for a student with a unique ID.
Field parsing and the shared save transaction are outside its scope.
<!-- @@author -->

<puml src="diagrams/AddSequenceDiagram.puml" alt="AddCommand checks identity and updates the pending model" />

<!-- @@author sammeowwww -->

#### Add command walkthrough

1. Before the diagram begins, `AddCommandParser` validates the supplied values, creates a `Person`, and returns an
   `AddCommand`. `LogicManager` checks that student data is available and prepares a pending model containing the
   current records and filter.
2. `AddCommand#execute` asks the pending model whether it already contains the student. Identity is based on the
   normalised Student ID. If that ID exists, the command throws a `CommandException` before adding anything.
3. For a new ID, the command adds the `Person` to the pending model, retrieves the complete student list for its
   count, and returns a `CommandResult` with the student's ID, name, optional remark, and total count.
4. After the diagram ends, `LogicManager` saves the pending records. Only after a successful save does it record
   the previous state for `undo` and publish the pending model, as shown in the
   [shared save sequence](#saving-before-publishing-changes). A save failure leaves the live records unchanged.

If startup loading fails, `MainApp` keeps the original file and starts with an empty fallback model.
`LogicManager` blocks modifying commands, `list`, `find`, and `attendance` until the file is fixed and the
application is restarted. See [Blocking student-data commands after a failed load](#blocking-student-data-commands-after-a-failed-load).
<!-- @@author  -->

Successful adds display `Added student ID: NAME.`, followed by ` Remark: TEXT` when the remark is non-empty,
and the total student count on the next line. The count uses the complete record list.
`PersonCard` displays labels and non-empty remarks with wrapping, and hides the remark field for
empty remarks.
JSON storage preserves remarks and defaults missing/null remark fields to empty strings.

#### Verification

Tests cover field boundaries and normalisation, label parsing, parameter errors and ordering, duplicate IDs with changed details,
feedback with/without remarks, JSON round trips, filtered-list handling, startup loading failures, and save failures.
Replacement-failure tests simulate an error after the temporary file is written and check that the original file,
records, and active filter remain unchanged. They also check temporary-file cleanup and failed first saves.

### Find command

Command format: `find n/KEYWORD [MORE_KEYWORDS]...`, `find i/KEYWORD [MORE_KEYWORDS]...`,
`find e/KEYWORD [MORE_KEYWORDS]...`, `find r/KEYWORD [MORE_KEYWORDS]...`, or `find l/LABEL_NAME`.
Supply exactly one search field. Label searches replace the former standalone filter command.
See the [User Guide](UserGuide.md#finding-students-by-name-student-id-email-remark-or-label-find) for examples and
[Find errors](#find-errors) for error messages and validation order.

#### Validation and matching

  <!-- @@author ananyakharbanda -->
* `ArgumentTokenizer` recognises `n/`, `i/`, `e/`, `r/`, and `l/`.
* `FindCommandParser` checks words before the first prefix, unknown prefixes, repeated prefixes, missing prefixes,
  multiple search-field types, empty values, and then label validity, in that order.
* Name, ID, email, and remark values are split into whitespace-separated keywords. `NameContainsKeywordsPredicate`,
  `StudentIdContainsKeywordsPredicate`, `EmailContainsKeywordsPredicate`, and `RemarkContainsKeywordsPredicate`
  match any keyword using
  `StringUtil#containsIgnoreCase`, including partial matches.
* `RemarkContainsKeywordsPredicate` searches `Person#getRemark().value` only. Empty remarks do not match
  non-empty keywords. Numeric slash text such as `8/10` is accepted under the existing prefix rules.
* A label value is kept whole, validated by `ParserUtil#parseLabel`, and passed to `LabelMatchesPredicate`.
  This predicate lowercases both the requested and stored label names with `Locale.ROOT`, then compares the complete
  values, using the same rule as `EditCommand` and `JsonAdaptedPerson`. Internal spaces are preserved.
  This also handles Unicode lowercase expansions, such as `İ` becoming `i` followed by a combining dot,
  while keeping `I` and the dotless `ı` distinct.
  <!-- @@author -->

For example, `find n/alex david` finds names containing either keyword, while `find l/Discrete Math Tutorial`
finds students with that complete group label. `find l/Tutorial` does not match `Discrete Math Tutorial`.
`find r/recursion loops` searches remarks for either keyword, and `find r/curs` also matches `recursion`.
Empty `r/`, repeated `r/`, and combinations such as `find r/recursion l/Tutorial 1` are rejected before execution.

#### Execution, persistence, and startup behaviour

```text
AddressBookParser → FindCommandParser → FindCommand
FindCommand → Model#updateFilteredPersonList
```

<!-- @@author ananyakharbanda -->
Every search replaces the active predicate and evaluates the complete student list; searches do not narrow the
previous result set. `FindCommand` returns the standard `N students listed.` (or `1 student listed.` for one match) feedback. Zero matches clear the view.
`list` restores all students. Invalid commands preserve the previous view.

`FindCommand` remains read-only and executes without saving when student data is available. After a startup
loading failure, `LogicManager` blocks it until the data file is fixed and the app restarted. A missing data file
or a successfully loaded empty file permits a search with zero results. Student records and the saved file are
unchanged by a search.
<!-- @@author -->

Tests in `RemarkContainsKeywordsPredicateTest`, `FindCommandParserTest`, and `FindCommandTest` cover remark
matching, invalid remark search inputs, result counts, and searching students hidden by a previous filter.

<!-- @@author ananyakharbanda -->
The first object sequence isolates `FindCommand#execute` after parsing and the startup-data check. All five
search modes share this execution path; only the supplied predicate differs.

<puml src="diagrams/FindCommandSequenceDiagram.puml" alt="FindCommand applies a predicate and reports the result count" />

The label-parser sequence shows construction of the predicate and command after structure validation. The
activity diagram separately shows user-visible validation and result decisions, without implementation classes:

<puml src="diagrams/LabelFindSequenceDiagram.puml" alt="FindCommandParser constructs a label predicate and command" />

<puml src="diagrams/LabelFindActivityDiagram.puml" alt="Label-search input, availability, and result decisions" />
<!-- @@author -->

#### Verification

<!-- @@author ananyakharbanda -->
Tests cover all five prefixes, malformed and repeated parameters, mixed search fields, multi-word and boundary-length
labels, invalid labels, exact and case-insensitive matching, multiple student labels, empty and zero-result lists,
searches after other searches, restoration with `list`, saved-data preservation, and rejection of the removed command.
<!-- @@author -->

### Deleting student information

Command format: `delete i/STUDENT_ID`. The display index is not used to identify students for deletion.

`DeleteCommandParser` accepts exactly one non-empty `i/`. It rejects line breaks, unknown prefixes, preamble text,
repeated IDs, missing IDs, and empty IDs before calling `ParserUtil#parseStudentId`.
`ArgumentTokenizer#findUnknownPrefix` shares the same case-sensitive alphabetic-prefix detection with `add`.
An invalid Student ID retains the field constraint message instead of being replaced with a usage error.

`DeleteCommand` stores the validated `StudentId` and searches `model.getAddressBook().getPersonList()` for an exact
normalised ID match. It retrieves the existing `Person` and passes that object to `model.deletePerson`.
This removes the entire record, including remarks and labels, without separately deleting its fields.
The complete record list is searched so a student hidden by a filter can still be deleted.

For example, after `find n/Benson`, `delete i/a1234567b` removes Alice's `A1234567B` record while the displayed Benson
results remain unchanged. If Alice is already absent, the command reports
`Student with ID A1234567B is not in the records.` and changes no data.
Successful feedback is `Deleted student A1234567B: Alice Pauline.`

Deletion preserves the current filter and returns true from `Command#isModifyingData`.
`LogicManager` therefore performs it on the temporary model, saves the proposed records, and only then publishes
the change. If saving fails, the live record, filter, and previous saved file remain unchanged.
If startup loading failed, deletion is blocked by the existing loading guard.

<!-- @@author ananyakharbanda -->
The sequence diagram isolates `DeleteCommand#execute` on a pending model after parsing and the startup-data
check. It assumes the student exists. The shared [transaction](#saving-before-publishing-changes) handles saving
and any failure before publishing the deletion.

<puml src="diagrams/DeleteSequenceDiagram.puml" alt="DeleteCommand removes a student from the pending model" />
<!-- @@author -->

[PlantUML source](diagrams/DeleteSequenceDiagram.puml)

Tests cover normalised IDs, visible/hidden students, an empty displayed list, nonexistent IDs, malformed parameters,
whole-record removal from persisted JSON, filter preservation, and save failures.

### Viewing attendance

The command `attendance d/YYYY-MM-DD l/GROUP` shows which current members of a tutorial group were marked present on
a date. Its accepted values and messages are documented in the
[User Guide](UserGuide.md#viewing-attendance-attendance). Recording and removing attendance are described in
[Mark and unmark attendance](#mark-and-unmark-attendance).

#### Storing attendance

Each `Person` holds an immutable set of `AttendanceRecord` objects. A record is a date and a group `Label`. Two records
are equal when their dates match and their group names match after `toLowerCase(Locale.ROOT)`, the same rule used to
reject duplicate labels. This makes the date and group pair unique for each student.

`JsonAdaptedPerson` saves the records under each person's `attendance` field, and `JsonAdaptedAttendanceRecord`
validates each date and group when loading. A missing or `null` `attendance` field loads as an empty set, so data files
written before attendance existed still load. Records are stored inside the `Person`, so deleting a student deletes
their records, and `edit` keeps them.

Only presence is stored. A student is absent when they have no record for the date and group.

`AttendanceCommandParser` checks `d/` and `l/` in the same order as the `mark` and `unmark` parsers, then reuses
`ParserUtil#parseDate` and `ParserUtil#parseLabel`.

#### Reporting attendance

<!-- @@author ananyakharbanda -->
<puml src="diagrams/AttendanceModelClassDiagram.puml" alt="Derived report entries reference current students" />
<!-- @@author -->

`AttendanceReport#of(students, date, group)` keeps the students whose current labels include the group, in the order
given, and pairs each one with whether they have the matching record. It also gives the present and absent totals.
Membership comes from current labels, so a student who lost the label is left out even if their records remain.

`AttendanceCommand` is read-only. It builds a report from `model.getAddressBook().getPersonList()`, not the filtered
list, so `find` does not hide group members. It returns `CommandResult#showingAttendance(message, report)`.

#### Showing and refreshing the attendance view

`CommandResult` carries an optional `AttendanceReport`. `MainWindow#executeCommand` shows an `AttendanceListPanel` in
the student list area when a result carries a report, and shows the `PersonListPanel` again when it does not. Each
`AttendanceCard` shows a member's name, Student ID, and a **Present** or **Absent** badge.

`LogicManager` remembers the report being displayed in `displayedAttendance`. After each successful command it:

* stores the result's report if there is one;
* otherwise, if the result reports an attendance update and a report is displayed, rebuilds that report from the live
  model and attaches it to the result with `CommandResult#withAttendanceReport`. `MarkCommand` and `UnmarkCommand`
  report updates by returning `CommandResult#updatingAttendance(message)`;
* otherwise, clears `displayedAttendance`, so the UI returns to the student list.

Because the rebuild happens after saving and publishing, the view only changes once the change is saved. A failed
command throws before this step, so the view stays as it was. A successful `undo` returns a plain result, so it closes
the view; the restored records are shown in the student list.

<!-- @@author ananyakharbanda -->
The first object sequence shows `AttendanceCommand#execute` building a report from the full student list after
parsing and the startup-data check. The second isolates `LogicManager#updateDisplayedAttendance`, which runs
only after successful execution (and saving for a data change). UI rendering and command parsing are described
above rather than repeated in either diagram.

<puml src="diagrams/AttendanceSequenceDiagram.puml" alt="Build an attendance report from the complete student list" />

<puml src="diagrams/AttendanceRefreshSequenceDiagram.puml" alt="Select, refresh, or close the displayed attendance report" />
<!-- @@author -->

#### Design considerations

**Aspect: Where to keep the displayed report**

* **Alternative 1 (current choice):** `LogicManager` remembers it and attaches a refreshed report to the result.
  * Pros: `MainWindow` stays a simple renderer, and the refresh rules are tested without JavaFX.
  * Cons: `LogicManager` holds a small piece of display state.
* **Alternative 2:** `MainWindow` remembers the open date and group and asks `Logic` for a new report after `mark`.
  * Pros: Display state stays in the UI.
  * Cons: The UI must know which commands change attendance, and the refresh can only be tested through the GUI.

**Aspect: How to record absence**

* **Alternative 1 (current choice):** Store only presence; absent means not marked.
  * Pros: One command per present student, and no need to finalise a session.
  * Cons: An absence cannot be distinguished from a session the TA has not taken yet.
* **Alternative 2:** Store explicit Present and Absent records.
  * Pros: Every student has a recorded status for every session.
  * Cons: Needs a way to record or finalise absences, and more data to keep consistent when labels change.

#### Verification

* `AttendanceRecordTest` covers equality ignoring group case. `JsonAdaptedPersonTest` covers legacy files without
  `attendance`, invalid saved dates and groups, and duplicate records. `EditCommandTest` checks that edits keep records.
* `AttendanceCommandParserTest` covers invalid dates, and missing, empty, repeated, and unknown parameters.
* `AttendanceCommandTest` covers group members hidden by `find` and groups with no members.
* `AttendanceReportTest` covers several groups on one date, nobody marked, and students who no longer have the label.
* `LogicManagerTest` covers the view refreshing straight after `mark` and `unmark`, a failed mark leaving the view
  open, `undo` and other commands closing it, and marking, restarting, and viewing the saved attendance.
* `MainAppTest` loads a data file without `attendance`, marks students, restarts through the normal startup path,
  and checks the saved attendance and duplicate-mark check.
* `AttendanceListPanelTest` lays out the panel and checks its title, totals, members, and Present and Absent badges.
  The Linux CI job, which uploads coverage, runs under `xvfb-run` so that JavaFX tests have a virtual display.

### Loading student data at startup

Commands available after a failed load are documented in the [User Guide](UserGuide.md#loading-the-data).
Exact startup messages and data-file rules are in [Loading errors](#loading-errors).

#### Loading and error reporting

The loading path is:

```text
MainApp#initModelManager → StorageManager → JsonAddressBookStorage → JsonSerializableAddressBook
MainApp#initLogic → LogicManager
```

1. `JsonAddressBookStorage#readAddressBook` returns `Optional.empty()` if the data file does not exist.
2. For an existing file, `JsonAddressBookStorage` reads its bytes once with `Files.readAllBytes`, decodes them as
   UTF-8, and parses them with `JsonUtil#fromJsonString`. Failure to read the bytes gives
   `Unable to load student data.`; invalid JSON gives
   `Unable to load student data. The data file may be corrupted or invalid.`
   Both are reported as `DataLoadingException`.
3. `JsonSerializableAddressBook#toModelType` converts records in file order. A missing or invalid field throws
   `IllegalValueException`, which is reported with the corrupted or invalid message. A repeated Student ID throws
   `DuplicateStudentIdException`, a subclass of `IllegalValueException`. `JsonAddressBookStorage` catches it first so
   that its specific message, `Unable to load student data: duplicate Student ID A0123456B detected.`, is kept.
   Student IDs are compared after normalisation to uppercase. Only after conversion succeeds does storage remember
   the same bytes for conflict detection on future saves. A missing file is remembered as absent.
4. `MainApp` records the outcome as the startup message: `N students loaded.` (`1 student loaded.` for one record),
   the no-existing-data message, or the `DataLoadingException` message.
5. `MainWindow#fillInnerParts` shows `Logic#getStartupMessage` in the result display when the window opens.

<!-- @@author ananyakharbanda -->
The object sequence below shows startup orchestration through the `Storage` interface. Its three outcomes are a
successful load, a missing file, and a loading exception. Storage deserialisation is described above and omitted
here; the diagram focuses on constructing the model and logic with the correct availability state.
<!-- @@author -->

<puml src="diagrams/LoadSequenceDiagram.puml" alt="Loading student data at startup" />

#### Blocking student-data commands after a failed load

When loading fails, `MainApp` starts with an empty `AddressBook` and passes `isStudentDataAvailable = false` to
`LogicManager`. `LogicManager#execute` parses each command first, so malformed commands still receive their usual
error messages. It then rejects modifying commands (including `undo`), `list`, `find`, and `attendance` with
`Student data is unavailable because loading failed. Fix the data file and restart.`

This prevents the empty in-memory list from being saved over the user's data file, which may still be recoverable.
`list`, `find`, and `attendance` are blocked because the empty fallback model is not a successfully loaded dataset.
`help` and `exit` remain available. A missing file or a successfully loaded empty file still permits student-data
commands and genuine zero-total attendance reports. After fixing the file, restart to retry loading.

Saving is described in [Saving before publishing changes](#saving-before-publishing-changes).

#### Design considerations

**Aspect: What to do when the data file cannot be loaded**

* **Alternative 1 (current choice):** Start with an empty list, keep the file, and block commands that change or query student data.
  * Pros: The user's data file is never overwritten, and the user sees why loading failed.
  * Cons: The app cannot be used to change records until the file is fixed.
* **Alternative 2:** Start with an empty list and allow modifying commands, as in AddressBook-Level3.
  * Pros: The app remains fully usable.
  * Cons: The first save overwrites the original file, losing data that might have been fixable.

#### Verification

`MainAppTest` covers a missing file, files with zero, one, and several students, a corrupted file, and duplicate Student
IDs. It checks that modifying commands and student-data queries are blocked and the file is unchanged after a failed
load. Missing files and successfully loaded empty files remain usable.
`JsonAddressBookStorageTest` covers unreadable files, invalid JSON, invalid values, and duplicate Student IDs.

### Undoing data changes

`LogicManager` owns an in-memory deque of complete `AddressBook` snapshots. It retains at most 100 prior states.
The snapshots contain student data only; GUI settings and the current `find` predicate are not recorded.
History begins empty on every application start and is never written to JSON.

Before a normal data-changing command runs, `LogicManager` copies the live address book. It executes the command on
the existing pending model and saves that proposed address book. Only after the save succeeds does it append the
pre-change snapshot, discarding the oldest snapshot first when the deque is full, and publish the pending model.
If the saved address book equals the snapshot, as for `clear` on an empty list, no snapshot is appended, so `undo`
never reports a change it cannot show.
Thus parsing, validation, execution, or save failures leave the live records and undo history unchanged.

For `undo`, `LogicManager` examines the newest snapshot without removing it. It saves that snapshot before replacing
the live address book, resets the displayed list with `Model.PREDICATE_SHOW_ALL_PERSONS`, and then removes the
snapshot. Showing all students makes the restored records visible; filters are not part of undo history.
If saving fails, live records, the displayed list, the saved file, and history remain unchanged.
If no snapshot exists, it returns `Nothing to undo.` without saving and preserves the student filter.
Both successful outcomes close an open attendance report.

Undo is destructive: restoring a snapshot discards the state being reversed, so there is no redo or forward branch.
A later successful data change records the then-current state as a new predecessor.

<!-- @@author ananyakharbanda -->
The state diagram illustrates changes to records and undo history only; it omits persistence operations.
<!-- @@author -->

<puml src="diagrams/UndoHistoryStateDiagram.puml" alt="Bounded destructive undo history" />

<!-- @@author ananyakharbanda -->
The object sequence starts at `LogicManager#executeUndo`, after parsing and the startup-data check. It shows
restoration and persistence, with UI updates handled after the method returns.

<puml src="diagrams/UndoSequenceDiagram.puml" alt="Save the previous state before restoring it and consuming history" />
<!-- @@author -->

#### Design consideration

**Aspect: How undo stores prior state:**

* **Current choice: Complete address-book snapshots in `LogicManager`.**
  * Pros: One transaction handles every data-changing command and keeps saving before publishing explicit.
  * Cons: Each retained state copies the address-book list structure.
* **Alternative: Command-specific inverse operations.**
  * Pros: Can retain less state for some commands.
  * Cons: Every data-changing command must implement and validate its own reverse operation.

--------------------------------------------------------------------------------------------------------------------

## **Documentation, logging, testing, dev-ops**

<!-- @@author ananyakharbanda -->
Manual scenarios and their expected results are maintained in
[Appendix: Instructions for manual testing](#appendix-instructions-for-manual-testing).
The following guides cover development workflows:
<!-- @@author -->

* [Documentation guide](Documentation.md)
* [Testing guide](Testing.md)
* [Logging guide](Logging.md)
* [Configuration guide](Configuration.md)
* [DevOps guide](DevOps.md)

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Requirements**

### Product scope

**Target user profile**:

TeachAssist is for teaching assistants (TAs) who support a fixed group of undergraduate students through weekly tutorials and consultations throughout a semester.
They need to retrieve and update student information quickly while handling teaching, assessment, and follow-up tasks.
They can type quickly and prefer entering text commands over mouse-driven input.

**Value proposition**: Help teaching assistants organise student information and learning progress so they can remember individual students' needs and provide consistent, targeted support throughout the semester.

TeachAssist supports the management of student information, learning progress, interactions, and follow-up needs. It does not aim to replace a learning management system (LMS) for distributing teaching materials, conducting assessments, calculating official grades, or communicating directly with students.

### User stories
Priorities: High (must have) - `* * *`, Medium (nice to have) - `* *`, Low (unlikely to have) - `*`.

High-priority stories cover the agreed MVP, including finding students by name. Medium-priority stories capture other requirements considered by the team; they are not commitments for the MVP or final product. No stories are currently assigned low priority. These are product requirements, not a list of implemented features.

TA refers to a teaching assistant. Story IDs are retained from the project notes, including the gap between US29 and US34.

#### Student information

| ID | Priority | As a/an ... | I want to ... | So that ... |
|----|----------|--------|-------------|----------------|
| US01 | `* * *` | TA | add a student under my care | I can keep information about them throughout the semester |
| US02 | `* * *` | TA | view a student's information | I can quickly recall who the student is |
| US03 | `* * *` | TA | update a student's information | my records remain accurate when circumstances change |
| US04 | `* * *` | TA | remove a student who is no longer under my care | my records remain relevant |
| US05 | `* * *` | TA handling multiple tutorial groups | associate students with their tutorial groups | I can distinguish students from different classes |
| US06 | `* * *` | TA | view students belonging to a particular tutorial group | I can prepare for that group's tutorial |
| US07 | `* *` | TA who remembers only partial information about a student | search for students using information I remember | I can retrieve their records quickly |

#### Student notes and interactions

| ID | Priority | As a/an ... | I want to ... | So that ... |
|----|----------|--------|-------------|----------------|
| US08 | `* * *` | TA | record notes about a student | I can remember important information about them later |
| US09 | `* * *` | TA preparing to meet a student | view my previous notes about the student | I can provide support consistent with our earlier interactions |
| US10 | `* *` | TA | record a consultation with a student | I can remember what we discussed |
| US11 | `* *` | TA | view a student's past interactions chronologically | I can understand how their situation has developed over the semester |
| US12 | `* * *` | TA who made an incorrect note | edit or remove the note | misleading information does not remain in the student's record |
| US13 | `* *` | busy TA | quickly record a short observation about a student during or immediately after class | I do not forget it before recording it later |

#### Learning progress

| ID | Priority | As a/an ... | I want to ... | So that ... |
|----|----------|--------|-------------|----------------|
| US14 | `* *` | TA | record topics that a student is struggling with | I know where the student may need additional support |
| US15 | `* *` | TA | record topics that a student has improved in | I can track their learning progress over time |
| US16 | `* *` | TA preparing for a consultation | view a student's learning progress | I can tailor the consultation to their needs |
| US17 | `* *` | TA preparing a tutorial | see which topics students in my class are commonly struggling with | I can spend more time addressing those areas |
| US18 | `* *` | TA | update a student's progress for a topic | the student's record reflects their current level of understanding |

#### Follow-up tasks with students

| ID | Priority | As a/an ... | I want to ... | So that ... |
|----|----------|--------|-------------|----------------|
| US19 | `* *` | TA | record a follow-up action associated with a student | I do not forget things I need to do for them |
| US20 | `* *` | TA | view my outstanding follow-ups | I know which students still require my attention |
| US21 | `* *` | TA | mark a follow-up as completed | I can distinguish finished tasks from those still requiring action |
| US22 | `* *` | TA handling many students | see which students currently require follow-up | nobody accidentally gets overlooked |
| US23 | `* *` | TA | associate a follow-up with a deadline | I know which matters should be handled first |
| US24 | `* *` | TA | view overdue follow-ups | I can address things I have failed to complete on time |

#### Attendance and participation

| ID | Priority | As a/an ... | I want to ... | So that ... |
|----|----------|--------|-------------|----------------|
| US25 | `* *` | TA | record a student's tutorial attendance | I can keep track of whether they have been attending classes |
| US26 | `* *` | TA | record a student's participation in tutorials | I can remember how actively they have been engaging in class |
| US27 | `* *` | TA | view a student's attendance history | I can notice repeated absences |
| US28 | `* *` | TA | identify students whom I have had little interaction with | I can make an effort to engage them |
| US29 | `* *` | TA preparing for a tutorial | see students who have recently missed tutorials | I am aware that they may need additional support |

#### Finding and organising information

| ID | Priority | As a/an ... | I want to ... | So that ... |
|----|----------|--------|-------------|----------------|
| US34 | `* * *` | TA in a hurry | quickly find a student by name | I can access their information without interrupting my workflow |
| US35 | `* *` | TA with many students | filter students based on information relevant to my current task | I only see the students I need to focus on |
| US36 | `* *` | TA | view students who need my attention | I can prioritize whom to follow up with |
| US37 | `* *` | TA | organize students using meaningful categories | I can retrieve groups of related students easily |
| US38 | `* *` | experienced user | perform common operations efficiently | managing student information does not distract me from teaching |

#### Recovering from mistakes

<!-- @@author ananyakharbanda -->
| ID | Priority | As a/an ... | I want to ... | So that ... |
|----|----------|--------|-------------|----------------|
| US39 | `* *` | TA who made an accidental change | undo my latest saved data change | I can restore the previous student records |
<!-- @@author -->

### Use cases

<!-- @@author ananyakharbanda -->
The **system** is TeachAssist and the **actor** is a Teaching Assistant (TA). These use cases describe the
implemented application. The user stories above also include backlog ideas; they do not imply additional commands.
Exact syntax and field constraints belong in the [User Guide](UserGuide.md#features).

| Implemented command | Use case |
|---------------------|----------|
| `add` | [UC01: Add a student](#uc01-add-a-student) |
| `find` | [UC02: Search student details](#uc02-search-student-details), [UC04: Find students by group label](#uc04-find-students-by-group-label) |
| `edit` | [UC03: Edit a student](#uc03-edit-a-student) |
| `delete` | [UC05: Delete a student](#uc05-delete-a-student) |
| `mark` | [UC06: Mark attendance](#uc06-mark-attendance) |
| `attendance` | [UC07: View group attendance](#uc07-view-group-attendance) |
| `unmark` | [UC08: Unmark attendance](#uc08-unmark-attendance) |
| `clear` | [UC09: Clear all students](#uc09-clear-all-students) |
| `undo` | [UC10: Undo a data change](#uc10-undo-a-data-change) |
| `list` | [UC11: List all students](#uc11-list-all-students) |
| `help` | [UC12: View help](#uc12-view-help) |
| `exit` | [UC13: Exit TeachAssist](#uc13-exit-teachassist) |
<!-- @@author -->

**Common extensions**

<!-- @@author ananyakharbanda -->
These failure outcomes apply wherever their condition arises. Each ends the use case without applying the
requested change. Rejected commands preserve the current records, view, and available undo history.

| Condition | Applies to | Observable outcome |
|-----------|------------|--------------------|
| The input is malformed or contains invalid, missing, empty, repeated, or unsupported parameters | All commands, according to their accepted syntax | TeachAssist reports the input error. Commands that take no arguments reject extra text. |
| Startup student-data loading failed | UC01–UC11, after input validation | TeachAssist reports that student data is unavailable and asks the TA to fix the file and restart. `help` and `exit` remain available. A missing file or successfully loaded empty file does not cause this restriction. |
| The requested data change cannot be saved | UC01, UC03, UC05, UC06, UC08, UC09, and UC10 when a change is available to undo | TeachAssist reports the appropriate [saving error](#saving-errors). It does not apply the change or overwrite the destination. A changed file requires restarting; an active writer requires retrying; storage failures require repairing the storage problem. |

Successful viewing commands do not save student data. Successful data changes can be undone within the current
session, subject to the history limit in UC10. A failed command never closes an open attendance view.
<!-- @@author -->

#### UC01: Add a student

**MSS**

   <!-- @@author ananyakharbanda -->
1. TA requests to add a student with a name, Student ID, email, and optional remark and group label.
2. TeachAssist adds and saves the student.
3. TeachAssist shows all students and confirms the added student's details and total student count.
   <!-- @@author -->

   Use case ends.

**Extensions**

  <!-- @@author ananyakharbanda -->
* 2a. The Student ID is already in use, ignoring case.
  * 2a1. TeachAssist reports the duplicate ID without merging or changing either record.
  <!-- @@author -->

  Use case ends.

#### UC02: Search student details

**MSS**

   <!-- @@author ananyakharbanda -->
1. TA requests a search of one field: name, Student ID, email, or remark, with one or more keywords.
2. TeachAssist shows students whose selected field contains any keyword, ignoring case, and reports the count.
   The search covers all records and replaces the previous search or attendance view.
   <!-- @@author -->

   Use case ends.

**Extensions**

  <!-- @@author ananyakharbanda -->
* 2a. No students match.
  * 2a1. TeachAssist shows an empty student list and reports `0 students listed.`
  <!-- @@author -->

  Use case ends.

<!-- @@author ananyakharbanda -->
Group-label searches use the complete-label matching rule in UC04, rather than partial keywords.
<!-- @@author -->

#### UC03: Edit a student

**MSS**

   <!-- @@author ananyakharbanda -->
1. TA identifies a student by Student ID and requests changes to the name, email, remark, or group labels.
2. TeachAssist saves the updated student, including when the student is hidden by a search. The Student ID,
   attendance records, and unspecified fields are retained. Label removals take effect before additions.
3. TeachAssist shows all students and confirms the updated details.
   <!-- @@author -->

   Use case ends.

**Extensions**

  <!-- @@author ananyakharbanda -->
* 2a. No student has the supplied Student ID.
  * 2a1. TeachAssist reports that the student is not in the records.
  <!-- @@author -->

  Use case ends.
* 2b. A label to remove is absent, or a label to add is still present after removals.
  * 2b1. TeachAssist reports the conflicting label and rejects the entire edit, including other field changes.

  Use case ends.

<!-- @@author ananyakharbanda -->
An empty remark clears it. An empty `l/` clears all labels and cannot accompany other label changes.
Student IDs cannot be edited; changing an ID requires a separate deletion and addition.
<!-- @@author -->

#### UC04: Find students by group label

**MSS**

   <!-- @@author ananyakharbanda -->
1. TA requests students with one complete group label.
2. TeachAssist shows all matching students and the count, replacing the previous search or attendance view.
   Matching ignores case but requires exact internal spacing. A multi-word label is one search value.
   <!-- @@author -->

   Use case ends.

**Extensions**

  <!-- @@author ananyakharbanda -->
* 2a. No students have the label.
  * 2a1. TeachAssist shows an empty student list and reports `0 students listed.`
  <!-- @@author -->

  Use case ends.

<!-- @@author ananyakharbanda -->
This is the label mode of `find`; there is no separate `filter` command.
<!-- @@author -->

#### UC05: Delete a student

**MSS**

   <!-- @@author ananyakharbanda -->
1. TA requests deletion using a Student ID.
2. TeachAssist removes and saves the student and all their remarks, labels, and attendance records,
   even when that student is hidden by a search.
3. TeachAssist confirms the deletion and shows the student list with the previous search still applied.
   <!-- @@author -->

   Use case ends.

**Extensions**

  <!-- @@author ananyakharbanda -->
* 2a. No student has the supplied Student ID.
  * 2a1. TeachAssist reports that the student is not in the records.
  <!-- @@author -->

  Use case ends.

#### UC06: Mark attendance

**MSS**

1. TA requests to mark a student present for a date and group, identifying the student by Student ID.
2. TeachAssist records and saves the attendance without changing other student details or attendance entries.
3. TeachAssist confirms the Student ID, lowercase group name, and date. If an attendance view is open, it
   refreshes that view; otherwise the current student search remains applied.

   Use case ends.

**Extensions**

  <!-- @@author ananyakharbanda -->
* 2a. The student does not exist.
  * 2a1. TeachAssist reports the missing student.
  <!-- @@author -->

  Use case ends.
* 2b. The student does not currently have the requested group label.
  * 2b1. TeachAssist reports the missing group membership.

  Use case ends.
* 2c. The student is already marked for that date and group.
  * 2c1. TeachAssist reports that attendance is already recorded.

  Use case ends.

#### UC07: View group attendance

**MSS**

   <!-- @@author ananyakharbanda -->
1. TA requests attendance for a date and group.
2. TeachAssist replaces the student list with all current members of that group in record order, including
   students hidden by a search. Students with an attendance entry are Present; the others are Absent.
   TeachAssist shows the present and absent totals.
   <!-- @@author -->

   Use case ends.

**Extensions**

* 2a. No students currently have the label.
  * 2a1. TeachAssist shows an empty attendance view with zero totals.

  Use case ends.

<!-- @@author ananyakharbanda -->
The report uses current group membership. Removing a label excludes that student from later reports, while their
attendance records are retained. Successful `mark` and `unmark` commands refresh the open report for its original
date and group, even if the command changed a different date or group. Another `attendance` command selects a new
report. Other successful commands return to the student list (or exit the app); failed commands keep the report open.
<!-- @@author -->

#### UC08: Unmark attendance

**MSS**

1. TA identifies an attendance entry by Student ID, date, and group and requests its removal.
2. TeachAssist removes and saves that entry, even if the student no longer has the group label.
   Other attendance entries and student details are retained.
3. TeachAssist confirms removal using the lowercase group name and refreshes an open attendance view.
   If the student list is shown, its current search remains applied.

   Use case ends.

**Extensions**

  <!-- @@author ananyakharbanda -->
* 2a. The student does not exist.
  * 2a1. TeachAssist reports the missing student.
  <!-- @@author -->

  Use case ends.
* 2b. The student has no attendance entry for that date and group.
  * 2b1. TeachAssist reports that the attendance is not marked.

  Use case ends.

#### UC09: Clear all students

**MSS**

   <!-- @@author ananyakharbanda -->
1. TA requests to clear the student records.
2. TeachAssist removes and saves all students and their associated data, including records hidden by a search,
   without asking for confirmation.
3. TeachAssist shows an empty student list and confirms the clear. The current student search remains applied.
   <!-- @@author -->

   Use case ends.

**Extensions**

  <!-- @@author ananyakharbanda -->
* 2a. There are already no students.
  * 2a1. TeachAssist saves the empty records without adding a new change to undo history.
  <!-- @@author -->

  Use case resumes at step 3.

<!-- @@author ananyakharbanda -->
The clear can be reversed using UC10 while that change remains available in the current session.
<!-- @@author -->

#### UC10: Undo a data change

**MSS**

   <!-- @@author ananyakharbanda -->
1. TA requests to undo the latest data change.
2. TeachAssist restores and saves the records from before the latest successfully saved change.
3. TeachAssist shows all students and reports `Undid last change.` An open attendance view closes.
   <!-- @@author -->

   Use case ends.

**Extensions**

  <!-- @@author ananyakharbanda -->
* 2a. No change is available to undo, including immediately after restarting.
  * 2a1. TeachAssist reports `Nothing to undo.` without saving or changing records. The existing student
    search is retained and an open attendance view closes.
  <!-- @@author -->

  Use case ends.

<!-- @@author ananyakharbanda -->
Up to 100 changes are available in the current session. Commands that leave records unchanged do not add an undo
step. There is no redo. Previous searches and window settings are not restored, and restarting clears undo history.
<!-- @@author -->

#### UC11: List all students

**MSS**

   <!-- @@author ananyakharbanda -->
1. TA requests the complete student list.
2. TeachAssist replaces any search or attendance view with all students in record order and reports
   `Listed all students.`
   <!-- @@author -->

   Use case ends.

**Extensions**

  <!-- @@author ananyakharbanda -->
* 2a. There are no students.
  * 2a1. TeachAssist shows the empty list and reports `No students found.`
  <!-- @@author -->

  Use case ends.

#### UC12: View help

**MSS**

   <!-- @@author ananyakharbanda -->
1. TA requests help using `help`, the Help menu, or F1.
2. TeachAssist opens the Help window with the User Guide address and a **Copy URL** button.
   <!-- @@author -->

   Use case ends.

**Extensions**

  <!-- @@author ananyakharbanda -->
* 2a. The Help window is already open or minimised.
  * 2a1. TeachAssist restores and brings that window to the front.
  <!-- @@author -->

  Use case ends.

<!-- @@author ananyakharbanda -->
Help remains available after a loading failure. A successful `help` command also returns an open attendance view
to the student list. Opening the Help window through the menu or F1 does not execute that command.
<!-- @@author -->

#### UC13: Exit TeachAssist

**MSS**

   <!-- @@author ananyakharbanda -->
1. TA requests to exit using `exit` or closes the main window.
2. TeachAssist closes. Student data from successful changes is already saved; it is loaded on the next launch.
   <!-- @@author -->

   Use case ends.

<!-- @@author ananyakharbanda -->
Exit remains available after a loading failure. Undo history does not survive restarting.
<!-- @@author -->

### Non-Functional Requirements

1.  Should work on any _mainstream OS_ as long as it has Java `25` or above installed.
2.  Should support 1000 students. The release acceptance target is a usable window within 5 seconds of launching
    the JAR, and visible command feedback and list updates within 1 second in at least 19 of 20 measured runs per
    command on the recorded test machine, using a local writable disk. This is a verification target, not a claim
    that benchmarking has passed. See [Performance verification](#performance-verification) for the dataset and procedure.

### Glossary

* **Mainstream OS**: Windows, Linux, Unix, or macOS
* **TA**: Teaching Assistant, the intended user of TeachAssist.
* **Student ID**: The unique identifier used to distinguish student records.
* **Group label**: A name assigned to students to organise and find them by group, such as a tutorial group.
* **Remark**: Optional free-text notes attached to a student record.

--------------------------------------------------------------------------------------------------------------------

## Appendix: Instructions for manual testing

### Setup and baseline

<!-- @@author ananyakharbanda -->
Use Java 25 and a release JAR copied into a separate, writable test folder. These scenarios deliberately delete and
replace test records; use no real student data. Follow the [User Guide quick start](UserGuide.md#quick-start) to launch
the JAR. With no `data/addressbook.json`, expect an empty list and
`No existing student data found. Starting with an empty student list.`

Before each scenario below, start a fresh application session with no data file in the test folder and enter:
<!-- @@author -->

```text
add n/Alex Tan i/A0123456B e/alex@example.com r/Needs help with recursion l/Tutorial 1
add n/Beth Lim i/A0234567C e/beth@example.com l/Tutorial 1
```

<!-- @@author ananyakharbanda -->
Expect Alex and Beth to appear, with `1 student listed.` after the first add and `2 students listed.` after the second.
Unless stated otherwise, run each scenario's steps in order. For rejected commands, verify that student details,
the current view, and the contents of `data/addressbook.json` are unchanged.
<!-- @@author -->

### Adding, finding, and listing students

<!-- @@author ananyakharbanda -->
| Step | Command | Expected result |
|------|---------|-----------------|
| 1 | `add n/Another Name i/a0123456b e/another@example.com` | `Student ID A0123456B already exists.`; two students remain. |
| 2 | `add n/Bad Name i/A0345678D` | `Missing required parameter: e/.`; no student is added. |
| 3 | `find n/al` | Only Alex appears; `1 student listed.` |
| 4 | `find i/a023` | Only Beth appears, proving searches replace the previous filter. |
| 5 | `find e/example.com` | Both students appear. |
| 6 | `find r/curs` | Only Alex appears. |
| 7 | `find l/tutorial 1` | Both students appear; matching ignores case. |
| 8 | `find l/Tutorial` | Empty list; `0 students listed.`; label matching is exact. |
| 9 | `find r/` | `Parameter r/ cannot be empty.`; the empty view is preserved. |
| 10 | `list` | Both students appear; `Listed all students.` |
| 11 | `list 3` | `Invalid command format!` followed by `Usage: list`; both students remain visible. |
| 12 | `add n/ i/A0345678D e/valid@example.com` | The empty name is rejected; the two baseline students remain. |
| 13 | `find n/Alex i/A0123456B` | The mixed search fields are rejected; both students remain visible. |
<!-- @@author -->

### Editing students and labels

| Step | Command | Expected result |
|------|---------|-----------------|
| 1 | `find n/Beth` | Only Beth appears. |
| 2 | `edit i/a0123456b e/alex.new@example.com r/Revision complete` | Hidden student Alex is updated; both students appear. Feedback begins `Edited student: Alex Tan`; Alex's card shows the new remark. |
| 3 | `edit i/A0123456B dl/Tutorial 1 l/Tutorial 2 l/Consultation` | Alex has `tutorial 2` and `consultation`; Beth keeps `tutorial 1`. |
| 4 | `edit i/A0123456B n/Changed Name dl/Missing` | `Student A0123456B does not have label "missing".`; Alex's name and labels are unchanged. |
| 5 | `edit i/A0123456B l/Consultation` | `Student A0123456B already has label "consultation".` |
| 6 | `edit i/A0123456B l/New l/new` | `Parameter l/ repeats label "new".`; neither label is added. |
| 7 | `edit i/A0123456B` | `At least one field to edit must be provided.` |
| 8 | `edit i/A0123456B l/ dl/Tutorial 2` | `An empty l/ cannot be combined with other label changes.` |
| 9 | `edit i/A0123456B l/ r/` | All of Alex's labels and the remark are cleared; name and email are retained. |
| 10 | `find r/Revision` | No results, because Alex's remark was cleared. |
| 11 | `edit i/A0123456B l/Tutorial 1` | Alex's label is restored and both students are displayed. |

### Marking, viewing, and unmarking attendance

| Step | Command | Expected result |
|------|---------|-----------------|
| 1 | `attendance d/2026-10-20 l/Tutorial 1` | Alex and Beth are Absent; totals are 0 present, 2 absent. |
| 2 | `mark i/A0123456B d/2026-10-20 l/tutorial 1` | Feedback is `Marked student A0123456B present for tutorial 1 on 2026-10-20.` Alex becomes Present; totals become 1 present, 1 absent. |
| 3 | Repeat step 2 | Already-marked error; the attendance view and totals are unchanged. |
| 4 | `unmark i/A0123456B d/2026-10-20 l/Tutorial 1` | Alex becomes Absent; totals become 0 present, 2 absent. |
| 5 | Repeat step 4 | Not-marked error; the view remains unchanged. |
| 6 | `mark i/A0123456B d/2026-02-30 l/Tutorial 1` | `Date must use a valid YYYY-MM-DD date.` |
| 7 | Repeat step 2, then `edit i/A0123456B dl/Tutorial 1` | Attendance is recorded, then Alex's label is removed; the edit returns to the student list. |
| 8 | `attendance d/2026-10-20 l/Tutorial 1` | Only Beth appears; totals are 0 present, 1 absent. |
| 9 | `unmark i/A0123456B d/2026-10-20 l/Tutorial 1` | Removal succeeds even without current group membership; the view still contains only Beth. |
| 10 | `attendance d/2026-10-20 l/Missing` | Empty attendance view with zero totals. |
| 11 | `list` | Both students appear in the normal student list. |
| 12 | `find n/Alex`, then `attendance d/2026-10-20 l/Tutorial 1` | Beth is shown as Absent despite being hidden by the preceding search; Alex is excluded because step 7 removed the label. |
| 13 | `mark i/A0123456B d/2026-10-21 l/Tutorial 2` | Missing-group error; the open attendance view is unchanged. |
| 14 | `attendance d/2026-10-20 i/A0123456B` | Unsupported-prefix error; the attendance view is unchanged. |

### Deleting, clearing, and undoing changes

<!-- @@author ananyakharbanda -->
| Step | Command | Expected result |
|------|---------|-----------------|
| 1 | `find n/Beth` | Only Beth appears. |
| 2 | `delete i/a0123456b` | `Deleted student A0123456B: Alex Tan.`; Beth remains visible. |
| 3 | Repeat step 2 | `Student with ID A0123456B is not in the records.` |
| 4 | `undo` | `Undid last change.`; Alex and Beth both appear. The failed deletion did not consume history. |
| 5 | `find n/Alex`, then `clear` | Empty list; `Address book has been cleared!`; both records are removed from the saved file. |
| 6 | `undo` | Both complete records are restored and visible. |
| 7 | `undo 1` | `Invalid command format!` followed by `Usage: undo`; records are unchanged. |
| 8 | Run `undo` until the list is empty, then once more | The original two adds are reversed; the final response is `Nothing to undo.` |
| 9 | `clear`, then `undo` | Clearing the empty list succeeds without adding a snapshot; undo reports `Nothing to undo.` because step 8 exhausted the history. The list remains empty. |

The automated tests in `LogicManagerTest` cover eviction at the 100-snapshot history limit and failed-save preservation.
<!-- @@author -->

### Help, exit, and restart persistence

   <!-- @@author ananyakharbanda -->
1. With the baseline students, run `mark i/A0123456B d/2026-10-20 l/Tutorial 1`.
2. Run `help`. Confirm that the help window shows the UG address and that **Copy URL** copies it.
   Minimise the Help window, then run `help` again; it should be restored. Repeat with F1, then close that window.
3. Run `help extra`. Expect an invalid-format message and help usage without changing student records.
4. Run `exit`. Confirm that the application closes. Relaunch the same JAR from the same test folder.
5. Expect `2 students loaded.`. Verify both students' details, Alex's remark, and both labels.
6. Run `attendance d/2026-10-20 l/Tutorial 1`. Expect Alex Present and Beth Absent.
7. Run `undo`. Expect `Nothing to undo.` because history is not persisted; the attendance view closes.
   <!-- @@author -->

### Loading and saving failures

<!-- @@author ananyakharbanda -->
Use separate copies of the baseline test folder for each case. Close the app before changing files, except for the
cases explicitly requiring a running app: the data-folder obstruction, failed replacement, and stale-window save.

| Case | Preparation and action | Expected result |
|------|------------------------|-----------------|
| Invalid JSON | Back up `data/addressbook.json`, replace its contents with `{`, then launch. Run `list`, `find n/Alex`, `attendance d/2026-10-20 l/Tutorial 1`, then `clear`. | Startup reports corrupted or invalid data. Each command reports `Student data is unavailable because loading failed. Fix the data file and restart.`; no empty-list result or attendance report is shown. The invalid file remains untouched. Restore the backup and restart to recover both students. |
| Duplicate ID | Duplicate a complete student object in the JSON `persons` array, then launch. | Startup reports the duplicate Student ID; modifying commands, `list`, `find`, and `attendance` are blocked. Restore the backup and restart. |
| Cannot create data folder | Start in a fresh test folder with no `data` path. Launch and confirm the no-existing-data startup message. While the app stays open, create a regular file named `data` beside the JAR, then try a valid add. | `Unable to create data storage location.`; no student is added. Remove the test obstruction and retry; the add now succeeds. Creating the obstruction before launch instead tests loading failure. |
| Failed replacement | In a baseline folder, launch and run `find n/Beth`. While the app remains open, rename `data/addressbook.json` to `addressbook.backup.json` in the same folder. Create a non-empty directory named `addressbook.json` in its place, then run `delete i/A0123456B`. | `Unable to save student data.`; Beth remains the only visible student. `list` still shows both students. The backup file is unchanged. Close the app, remove the test directory, restore the backup filename, and restart. |
| Stale window | Launch the same baseline JAR twice before editing, so both windows load Alex and Beth. In window A, mark Alex present for Tutorial 1 on 2026-10-20. In window B, try `edit i/A0234567C r/Changed in B`. | B reports `Student data has changed outside this window. Restart TeachAssist to load the latest data before editing.` Its attempted remark is not applied and A's attendance remains saved. Close B and restart it; Alex's attendance is loaded and Beth's remark is unchanged. |

Lock contention is covered deterministically by `JsonAddressBookStorageConcurrencyTest`; racing two manual
commands is unreliable because the lock is held only during saving. It should produce
`Student data is being saved by another window. Please try again.` when another writer holds the lock.

For the screen-size, scaling, and disconnected-monitor scenarios, follow
[Display settings and window usability](Testing.md#display-settings-and-window-usability).
<!-- @@author -->

### Performance verification

<!-- @@author ananyakharbanda -->
This procedure evaluates NFR 2; it does not record a passing result. Record the app version, OS, Java version, CPU,
RAM, disk type, screen settings, dataset, timings, and any failures in the release test results.

1. Copy the release JAR into a fresh test folder. With the app closed, run the Python 3 script below from that folder
   to create 1000 synthetic students. The script refuses to overwrite an existing data file.
   <!-- @@author -->

   ```python
   import json
   from pathlib import Path

   students = []
   for number in range(1000):
       group = f"Tutorial {number % 10 + 1}"
       students.append({
           "name": "Performance Student",
           "studentId": f"T{number:07d}A",
           "email": f"student{number}@example.com",
           "remark": "Needs practice with recursion. " * 7,
           "labels": [group],
           "attendance": [{"date": f"2026-10-{day:02d}", "group": group}
                          for day in range(1, 11)],
       })
   Path("data").mkdir(exist_ok=True)
   with Path("data/addressbook.json").open("x", encoding="utf-8") as output:
       json.dump({"persons": students}, output, indent=2)
   ```

2. Measure three launches from starting the Java process until the window is usable and reports `1000 students loaded.`.
   Each launch should take at most 5 seconds. Retain the original fixture for repeatable runs.
3. After one warm-up of each operation, measure 20 runs each of `find n/Performance`, `list`, and
   `attendance d/2026-10-20 l/Tutorial 1`. Time from pressing Enter until both feedback and the resulting view
   finish updating, using a screen recording or equivalent timing tool. For at least 19 runs per command, the
   elapsed time should be at most 1 second.
4. For `add`, `edit`, `delete`, and `clear`, perform one untimed warm-up run followed by `undo` to restore the
   initial state. Then run that modifying command 20 times, executing `undo` after each successful run to restore
   the state needed for the next run. Time the modifying command and each `undo` separately. At least 19 of the
   20 runs of the modifying command must take at most 1 second. Similarly, at least 19 of the 20 succeeding `undo`
   commands must take at most 1 second.

   * `add n/Added Performance i/T1000000A e/added@example.com l/Tutorial 1`
   * `edit i/T0000000A r/Performance check`
   * `delete i/T0000000A`
   * `mark i/T0000000A d/2026-10-20 l/Tutorial 1`
   * `unmark i/T0000000A d/2026-10-01 l/Tutorial 1`
   * `clear`

   For `mark` and `unmark`, perform separate measurements from the student list and from the corresponding
   attendance report. For each starting view, perform one untimed warm-up run followed by `undo`, then execute the
   modifying command 20 times with `undo` after each successful run. Before every warm-up and timed command, use
   untimed setup to establish the starting view: `list` for the student-list measurement, or the relevant
   `attendance` command for the attendance-report measurement: `attendance d/2026-10-20 l/Tutorial 1` for mark and
   `attendance d/2026-10-01 l/Tutorial 1` for unmark. `undo` closes the report, so this setup keeps every timed
   command in that measurement on the same starting view. Apply the 19-of-20 requirement separately to each
   starting-view measurement and its corresponding `undo` commands.

   Outside the timed commands, verify that add and delete respectively produce 1001 and 999 students before undo,
   and 1000 students after undo. Verify that edit changes and restores the remark. For mark, use
   `attendance d/2026-10-20 l/Tutorial 1` to verify 100 members with 1 present and 99 absent after mark, and
   0 present and 100 absent after undo. For unmark, use `attendance d/2026-10-01 l/Tutorial 1` to verify 100 present
   before unmark, 99 present and 1 absent after unmark, and 100 present after undo.
   Verify that clear produces an empty list and that undo restores all 1000 students.
5. Also measure representative data-dependent rejected commands. After one untimed warm-up of each, run each command
   20 times and apply the 19-of-20 requirement. No `undo` is needed because both commands must leave the fixture
   unchanged.

   * `add n/Performance Student i/T0000999A e/student999@example.com l/Tutorial 10`
   * `mark i/T9999999A d/2026-10-20 l/Tutorial 1`

   Verify that the first command reports a duplicate Student ID and the second reports a missing student ID, and that
   the original 1000 students remain unchanged.
6. After the fixed-state measurements, perform the following stateful sequences. Begin each sequence from the normal
   student list. Time every command individually.
   At least 19 of the 20 modifying commands, and at least 19 of the 20 following `undo` commands, must take at most
   1 second. Verify the stated intermediate state, then verify that all 20 undos restore the original 1000-student
   fixture before beginning the next sequence.

   * Add 20 students consecutively with IDs `T1000000A` through `T1000019A`, then undo all 20 adds. Verify 1020
     students before undoing and 1000 afterwards.
   * Edit 20 different fixture students with a distinct replacement remark, then undo all 20 edits. Verify the
     edited remarks before undoing and the original remarks afterwards.
   * Delete 20 different fixture students, then undo all 20 deletes. Verify 980 students before undoing and 1000
     afterwards.
   * Mark 20 different fixture students for `2026-10-20`, using each student's existing tutorial label, then undo
     all 20 marks. Verify that the 20 records are present before undoing and absent afterwards.
   * Unmark the existing `2026-10-01` attendance records of 20 different fixture students, using each student's
     existing tutorial label, then undo all 20 unmarks. Verify that the 20 records are absent before undoing and
     restored afterwards.

   `clear` is excluded from these sequences because a second consecutive clear operates on an already empty list.
   `help`, `exit`, and syntax or parse failures are excluded because their work does not depend on the 1000-student
   dataset; functional testing covers those paths.
7. Scroll to the end of the full student list and confirm that the command box stays usable and no cards are missing.
   Record freezes or rendering errors separately from command timings. A failure requires investigation; this dataset
   does not establish performance for maximum-length remarks or unbounded attendance histories.

## Appendix: Errors

This appendix records error messages, recovery actions, and validation order. For everyday command usage, see the
[User Guide](UserGuide.md#features). Examples use representative Student IDs, labels, and prefixes; messages substitute
the relevant values. Only the first detected error is reported. Failed commands preserve student records, the active
view, undo history, and the saved file. Shared errors below also apply to the command-specific sections.

### General command errors

| Condition | Message | Recovery |
|-----------|---------|----------|
| Multiline command | `Invalid command format!` followed by `help: Shows program usage instructions.` and `Example: help` | Enter one command line. |
| Unknown or incorrectly capitalized command, e.g., `Add` | `Unknown command.` | Use the lowercase command word in the UG. |
| Unknown alphabetic prefix token, e.g., `x/` | `Unknown parameter: x/.` | Remove the unsupported prefix; this also applies inside remarks. |

The general command parser runs before command-specific validation. Unicode space separators and Hangul fillers
(`U+115F`, `U+1160`, `U+3164`, `U+FFA0`) count as spacing around commands and prefixes and are trimmed from value
boundaries. Zero-width spaces and byte order marks are not normalized as whitespace. Labels preserve internal spacing while letters are lowercased. Remarks preserve internal characters;
name spacing is normalized. See [UG whitespace handling](UserGuide.md#whitespace-and-label-matching).

### Field validation errors

| Condition | Message | Recovery |
|-----------|---------|----------|
| Invalid name | `Name must contain 1-100 characters, include a letter, and use only letters, spaces, apostrophes, hyphens, or periods.` | Follow the UG field rules and limits. |
| Invalid Student ID | `Student ID must contain exactly 9 letters or digits, with no spaces.` | Follow the UG field rules and limits. |
| Invalid email | `Email must have the form name@example.com and meet the supported email format.` | Follow the UG field rules and limits. |
| Invalid label | `Label names should be 1 to 60 characters long, contain at least one letter or digit, and only contain letters, digits, spaces, hyphens, underscores, apostrophes, parentheses, and periods.` | Follow the UG field rules and limits. |
| Oversized remark | `Remark must not exceed 4000 characters.` | Follow the UG field rules and limits. |
| Invalid date, e.g., `2026-02-30` or `2026-2-03` | `Date must use a valid YYYY-MM-DD date.` | Use a real calendar date with zero-padded month and day. |

The label constraint also applies to attendance groups. Empty edit names and emails produce their field constraint
messages. Length limits apply after trimming/normalization and count Unicode code points for names, labels, and remarks.

Email validation accepts at most 254 characters and exactly one `@`. The local part has 1–64 ASCII letters, digits,
periods, underscores, plus signs, or hyphens; it cannot begin/end with a period or contain consecutive periods.
The domain has at least two dot-separated labels of 1–63 ASCII letters, digits, or hyphens, without boundary hyphens.
Its final label has 2–63 ASCII letters. Spaces are rejected. Only the domain is lowercased; mailbox existence is not checked.

### Command-specific errors

#### Add errors

| Condition | Message | Recovery |
|-----------|---------|----------|
| Text before the first parameter | `Invalid command format. Usage: add n/NAME i/STUDENT_ID e/EMAIL [r/REMARK] [l/LABEL_NAME]` | Follow the command format in the UG. |
| Repeated parameter, e.g., two `n/` values | `Parameter n/ must be specified only once.` | Keep only the permitted number of parameters. |
| Missing compulsory parameter | `Missing required parameter: i/.` | Supply the required non-empty value. |
| Empty compulsory parameter | `Parameter n/ cannot be empty.` | Supply the required non-empty value. |
| Empty label | `Parameter l/ cannot be empty.` | Supply the required non-empty value. |
| Duplicate Student ID | `Student ID A0123456B already exists.` | Use a new Student ID, or edit the existing student. |

Validation order: unknown prefixes, preamble, repeated prefixes, missing required prefixes, empty required
values, empty optional label, then name, Student ID, email, label, and remark constraints. Repeated-prefix checks use
`n/`, `i/`, `e/`, `r/`, `l/` order; missing/empty required checks use `n/`, `i/`, `e/` order. Data availability is checked
after parsing, then duplicate Student ID, then saving. For example, `add n/ i/123 e/bad p/123` reports the unknown `p/`
before the empty name or invalid values.

#### Edit errors

| Condition | Message | Recovery |
|-----------|---------|----------|
| Text before the first parameter | `Invalid command format!` followed by the edit usage | Follow the command format in the UG. |
| Repeated single-value parameter, e.g., two `n/` values | `Multiple values specified for the following single-valued field(s): n/` | Keep only the permitted number of parameters. |
| Missing Student ID | `Missing required parameter: i/.` | Supply the required non-empty value. |
| Empty Student ID | `Parameter i/ cannot be empty.` | Supply the required non-empty value. |
| Invalid Student ID, name, email, remark, or label | The corresponding field constraint message in [Field validation errors](#field-validation-errors); empty `n/` and `e/` also produce these messages | Follow the UG field rules and limits. |
| No field to change, e.g., `edit i/A0123456B` | `At least one field to edit must be provided.` | Supply at least one field or label change. |
| Empty label removal | `Parameter dl/ cannot be empty.` | Supply the required non-empty value. |
| Same label repeated under one prefix | `Parameter l/ repeats label "tutorial 1".` (or `dl/`, using the normalized value) | Specify each label only once per prefix. |
| Empty `l/` combined with another label change | `An empty l/ cannot be combined with other label changes.` | Clear all labels separately from other label changes. |
| Student does not exist | `Student with ID A0123456B is not in the records.` | Use `list` or `find` to check the Student ID. |
| Removing a label the student does not have | `Student A0123456B does not have label "tutorial 1".` | Check the student’s labels and use the matching group name. |
| Adding a label still present after removals | `Student A0123456B already has label "tutorial 1".` | Omit the existing label, or remove it before adding its replacement. |

Validation order: unknown prefixes, preamble, duplicate single-value prefixes, required/non-empty/valid
Student ID, supplied name, email, remark, label changes, then whether any field was supplied. Label additions are parsed
before removals; an empty `l/` is checked for conflicting changes first. When several single-value prefixes repeat,
their order in the message is not guaranteed. After parsing, data availability and student existence are checked.
All label removals execute before additions; saving occurs last.

#### Find errors

| Condition | Message | Recovery |
|-----------|---------|----------|
| Extra words before the search parameter | `Invalid command format!` followed by the correct `find` format | Follow the command format in the UG. |
| Repeated same search parameter | `Parameter n/ must be specified only once.` (or the repeated prefix, such as `l/`) | Keep only the permitted number of parameters. |
| Missing search parameter | `Missing required parameter: n/, i/, e/, r/, or l/.` | Supply the required non-empty value. |
| More than one search parameter type | `Parameter n/, i/, e/, r/, or l/ must be specified only once.` | Keep only the permitted number of parameters. |
| Empty search parameter | `Parameter l/ cannot be empty.` (or the supplied prefix) | Supply the required non-empty value. |
| Using the removed command, such as `filter l/Tutorial 1` | `Unknown command.` | Use a supported lowercase command and prefix from the UG. |

Validation order: preamble, unknown prefixes, repeated prefixes, missing prefixes, mixed search fields,
empty value, then label validation where applicable. Data availability is checked after parsing and before searching.
Name, ID, email, and remark searches normalize Unicode spaces and supported Hangul fillers for matching. Label searches
preserve internal Unicode spaces and Hangul fillers and compare complete names after `Locale.ROOT` lowercasing.

#### Delete errors

| Condition | Message | Recovery |
|-----------|---------|----------|
| Text before the `i/` parameter | `Invalid command format. Usage: delete i/STUDENT_ID` | Follow the command format in the UG. |
| Repeated Student ID | `Parameter i/ must be specified only once.` | Keep only the permitted number of parameters. |
| Missing Student ID | `Missing required parameter: i/.` | Supply the required non-empty value. |
| Empty Student ID | `Parameter i/ cannot be empty.` | Supply the required non-empty value. |
| Student does not exist | `Student with ID A0123456B is not in the records.` | Use `list` or `find` to check the Student ID. |

Validation order: unknown prefixes, preamble, repeated `i/`, missing `i/`, empty `i/`, invalid Student
ID, data availability, student existence, then saving. For example, `delete i/123 x/1` reports the unknown prefix first.

#### Mark errors

| Condition | Message | Recovery |
|-----------|---------|----------|
| Student does not exist | `Student with ID A0123456B is not in the records.` | Use `list` or `find` to check the Student ID. |
| Student does not have the group label | `Student A0123456B does not have group label "tutorial 1".` | Check the student’s labels and use the matching group name. |
| Attendance already exists | `Student A0123456B is already marked present for tutorial 1 on 2026-10-20.` | Use `attendance` to check the existing entry; use `unmark` to remove it. |

After parsing, data availability, student existence, current group membership, and existing attendance
are checked in that order, then saving. Success feedback uses the stored lowercase group name.

#### Unmark errors

| Condition | Message | Recovery |
|-----------|---------|----------|
| Student does not exist | `Student with ID A0123456B is not in the records.` | Use `list` or `find` to check the Student ID. |
| No attendance entry for the date and group | `Student A0123456B is not marked present for tutorial 1 on 2026-10-20.` | Check the date and stored group name, including internal spacing. |

After parsing, data availability, student existence, and matching attendance are checked, then saving.
Current group membership is not required. Success feedback uses the attendance entry’s lowercase group name.

#### Attendance parameter errors

The following parsing errors are shared by `mark`, `unmark`, and `attendance`. For `attendance`, omit `i/` and use
`attendance d/YYYY-MM-DD l/GROUP` as the usage text; `i/` is an unknown parameter.

| Condition | Message | Recovery |
|-----------|---------|----------|
| Text before the first parameter | `Invalid command format. Usage: unmark i/STUDENT_ID d/YYYY-MM-DD l/GROUP` (or the corresponding `mark` usage) | Follow the command format in the UG. |
| Repeated parameter | `Parameter d/ must be specified only once.` | Keep only the permitted number of parameters. |
| Missing parameter | `Missing required parameter: d/.` | Supply the required non-empty value. |
| Empty parameter | `Parameter l/ cannot be empty.` | Supply the required non-empty value. |
| Invalid group name | See the label message in [Field validation errors](#field-validation-errors). | Follow the UG field rules and limits. |

Validation order: unknown prefixes, preamble, repeated prefixes, missing/empty values, then field values.
Repeated and missing/empty checks use `i/`, `d/`, `l/` order for mark/unmark and `d/`, `l/` for attendance.
Each required prefix is checked for presence and a non-empty value before moving to the next. Fields are validated in
Student ID, date, and group order (date and group for attendance). The startup data-availability check follows parsing
for `mark`, `unmark`, and `attendance`; all three are blocked after a failed load. `attendance` does not save data.

#### List, clear, undo, help, and exit errors

| Condition | Message | Recovery |
|-----------|---------|----------|
| Additional non-whitespace input to `list`, `clear`, `undo`, or `exit`, e.g., `list n/Samuel` | `Invalid command format!` followed by `Usage: list` (substitute the command word) | Remove all arguments. |
| Additional non-whitespace input to `help` | `Invalid command format!` followed by `help: Shows program usage instructions.` and `Example: help` | Enter `help` alone. |

Argument validation precedes the startup data-availability check. `list`, `clear`, and `undo` are blocked after a failed
load; `help` and `exit` remain available. An empty undo history produces `Nothing to undo.` as successful feedback,
preserves the current student filter, and closes any attendance view. It does not save data.

### Loading errors

| Condition | Message | Recovery |
|-----------|---------|----------|
| File cannot be read, e.g., permission denied | `Unable to load student data.` | Close the app, restore read access or a readable backup, and restart. |
| Malformed JSON, extra root values/trailing garbage, `null` root, wrong structure, or invalid record | `Unable to load student data. The data file may be corrupted or invalid.` | Close the app, fix or restore the file, and restart. |
| Duplicate Student ID after normalization | `Unable to load student data: duplicate Student ID A0123456B detected.` | Give each student a unique ID, then restart. |
| Valid modifying command, `list`, `find`, or `attendance` after startup loading failed | `Student data is unavailable because loading failed. Fix the data file and restart.` | Fix or restore the data file with the app closed, then restart. |

The entire JSON document is deserialized before model validation. `JsonUtil#fromJsonString` uses a Jackson 2.7 parser
and explicitly checks `nextToken()` after binding the root value; additional tokens or invalid trailing content fail
loading. Exactly one student-data object is accepted, with optional surrounding JSON whitespace (space, tab, carriage
return, line feed). A `null` root is rejected. JSON syntax whitespace is distinct from Unicode padding inside strings.
After deserialization, model conversion checks students in file order, stopping at the first failure. No partial
student list is published. On failure the file is preserved; `help` and `exit` remain available.

Each student requires valid `name`, `studentId`, and `email` values. Missing/null `remark` becomes empty text;
missing/null `labels` and `attendance` become empty collections. Legacy `tags` and other unknown properties are ignored
and are not saved again. Null student, label, or attendance entries are invalid.

The root object contains a `persons` array. For example, this is a valid minimal data file:

```json
{
  "persons": [
    { "name": "Alex Tan", "studentId": "A0123456B", "email": "alex@example.com" }
  ]
}
```

Loading trims surrounding ASCII control whitespace, Unicode space separators, and the four supported Hangul fillers
from text fields, labels, dates, and attendance groups before validation. Names also normalize these Unicode spaces and
fillers to ASCII spaces and collapse repeated spaces. Internal spacing and punctuation in remarks, labels, and
attendance groups are preserved. Student IDs become uppercase; email domains, labels, and attendance groups become
lowercase. Label and group length checks count the lowercase `i` plus combining-dot sequence as one character.
This preserves labels accepted before lowercasing was introduced, including their saved lowercase forms.
Duplicate Student IDs, labels within a student, and attendance date/group pairs within a student are checked after
normalization; label/group comparison ignores case but preserves internal spacing, Unicode spaces, and Hangul fillers.
The normalized values are written on the next successful save.

Attendance is stored as `attendance`: a list of `{"date": "2026-10-20", "group": "tutorial 1"}` entries per student.
Dates must be real calendar dates in `YYYY-MM-DD` format; group names follow label constraints.

Successful startup reports `N students loaded.` (`1 student loaded.` for one student). A missing file reports
`No existing student data found. Starting with an empty student list.` and allows normal use.

If main-window initialization fails, an error dialog titled `Fatal error during initializing` shows the exception
details. Dismissing it exits the application with failure status; this is separate from student-data loading failure.

### Saving errors

| Condition | Message | Recovery |
|-----------|---------|----------|
| Storage folder cannot be created | `Unable to create data storage location.` | Use a writable home folder and remove any file obstructing the `data` folder. Retry the command. |
| Data file differs from the last successful read or save | `Student data has changed outside this window. Restart TeachAssist to load the latest data before editing.` | Restart to load the current file, review the latest records, then retry the intended change. |
| Another writer holds the data-file lock | `Student data is being saved by another window. Please try again.` | Wait for the other save to finish and retry. If the file has since changed, follow the changed-file recovery above. |
| Writing or atomic replacement fails, including unsupported atomic replacement | `Unable to save student data.` | Restore writable storage supporting atomic file replacement, then retry. |

Saving occurs after parsing, data-availability checks, and command-specific validation. Storage holds a sidecar
file lock, writes a complete temporary file, checks the destination against the last remembered bytes, then replaces
it atomically. See [Saving before publishing changes](#saving-before-publishing-changes).
Failures preserve live records, current view, and undo history; the attempted save does not replace the destination. Successful modifying commands save even if the records are unchanged, but unchanged records
do not create an undo snapshot. Undo with no history does not save.
