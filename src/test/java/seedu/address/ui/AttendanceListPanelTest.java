package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import seedu.address.model.attendance.AttendanceRecord;
import seedu.address.model.attendance.AttendanceReport;
import seedu.address.model.attendance.AttendanceReport.AttendanceEntry;
import seedu.address.model.person.Person;
import seedu.address.testutil.FxTestUtil;
import seedu.address.testutil.PersonBuilder;

/**
 * Checks the attendance view's content. JavaFX startup failures fail the suite; Linux CI uses a virtual display.
 */
public class AttendanceListPanelTest {

    private static final LocalDate DATE = LocalDate.parse("2026-10-20");
    private static final seedu.address.model.label.Label GROUP = new seedu.address.model.label.Label("Tutorial 1");

    @BeforeAll
    public static void startToolkit() throws Exception {
        FxTestUtil.initialize();
    }

    @Test
    public void constructor_report_showsTitleTotalsAndMembers() throws Exception {
        // EP: report with both present and absent members renders title, totals, rows, and both badge styles
        Person present = new PersonBuilder(ALICE).withLabels("Tutorial 1")
                .withAttendance(new AttendanceRecord(DATE, GROUP)).build();
        Person absent = new PersonBuilder(BENSON).withLabels("Tutorial 1").build();
        AttendanceReport report = AttendanceReport.of(List.of(present, absent), DATE, GROUP);

        Parent root = FxTestUtil.callAndWait(() -> layOut(new AttendanceListPanel(report).getRoot()));

        assertEquals("Attendance: tutorial 1 on 2026-10-20", ((Label) root.lookup("#title")).getText());
        assertEquals("Present: 1    Absent: 1", ((Label) root.lookup("#totals")).getText());
        assertEquals(report.getEntries(), ((ListView<?>) root.lookup("#attendanceListView")).getItems());
        assertEquals(1, root.lookupAll(".attendance_present").size());
        assertEquals(1, root.lookupAll(".attendance_absent").size());
    }

    @Test
    public void constructor_emptyReport_showsZeroTotals() throws Exception {
        // BVA: zero present and zero absent members render zero totals and no status badge
        AttendanceReport report = AttendanceReport.of(List.of(), DATE, GROUP);

        Parent root = FxTestUtil.callAndWait(() -> layOut(new AttendanceListPanel(report).getRoot()));

        assertEquals("Present: 0    Absent: 0", ((Label) root.lookup("#totals")).getText());
        assertTrue(((ListView<?>) root.lookup("#attendanceListView")).getItems().isEmpty());
        assertTrue(root.lookupAll("#status").isEmpty());
    }

    @Test
    public void attendanceCard_entry_showsStudentAndStatusBadge() throws Exception {
        // EP: present and absent entry cards render their student details and distinct status badges
        Node presentCard = FxTestUtil.callAndWait(() ->
                new AttendanceCard(new AttendanceEntry(ALICE, true), 1).getRoot());
        Node absentCard = FxTestUtil.callAndWait(() ->
                new AttendanceCard(new AttendanceEntry(BENSON, false), 2).getRoot());

        assertEquals("1. ", ((Label) presentCard.lookup("#id")).getText());
        assertEquals("Alice Pauline", ((Label) presentCard.lookup("#name")).getText());
        assertEquals("Student ID: A1234567B", ((Label) presentCard.lookup("#studentId")).getText());
        Label presentStatus = (Label) presentCard.lookup("#status");
        assertEquals("Present", presentStatus.getText());
        assertTrue(presentStatus.getStyleClass().contains("attendance_present"));
        assertFalse(presentStatus.getStyleClass().contains("attendance_absent"));

        Label absentStatus = (Label) absentCard.lookup("#status");
        assertEquals("Absent", absentStatus.getText());
        assertTrue(absentStatus.getStyleClass().contains("attendance_absent"));
    }

    @Test
    public void attendanceCard_longName_wrapsAndKeepsStatusVisible() throws Exception {
        String longName = "Alexandra ".repeat(10).trim() + "A";
        Person student = new PersonBuilder(ALICE).withName(longName).withLabels("Tutorial 1").build();

        FxTestUtil.callAndWait(() -> {
            // EP: an absent student with a maximum-length name at the minimum list width
            // BVA: 100-character name and 320-pixel list width within the minimum 340-pixel list pane
            double narrowNameHeight = assertBadgeVisible(student, 320);

            // EP: the same student at a wide list width
            double wideNameHeight = assertBadgeVisible(student, 1200);
            assertTrue(narrowNameHeight > wideNameHeight);
            return null;
        });
    }

    @Test
    public void attendanceCard_longNames_wrapAndKeepStatusVisibleWhenResized() throws Exception {
        FxTestUtil.callAndWait(() -> {
            // EP: present and absent students both keep their badges visible with long names
            // BVA: the present student's name is exactly the 100-character maximum
            Person present = new PersonBuilder(ALICE).withName("M".repeat(100)).withLabels("Tutorial 1")
                    .withAttendance(new AttendanceRecord(DATE, GROUP)).build();
            Person absent = new PersonBuilder(BENSON).withName("Long ".repeat(19) + "Name")
                    .withLabels("Tutorial 1").build();
            AttendanceReport report = AttendanceReport.of(List.of(present, absent), DATE, GROUP);
            Region root = new AttendanceListPanel(report).getRoot();
            Scene scene = new Scene(root, 400, 400);
            scene.getStylesheets().add(getClass().getResource("/view/DarkTheme.css").toExternalForm());

            for (double width : new double[] {400, 800, 340, 400}) {
                root.resize(width, 400);
                root.applyCss();
                for (int pass = 0; pass < 3; pass++) {
                    root.layout();
                }

                assertEquals(2, root.lookupAll("#cardPane").size());
                for (Node card : root.lookupAll("#cardPane")) {
                    Bounds cardBounds = card.localToScene(card.getLayoutBounds());
                    assertTrue(cardBounds.getMaxX() <= width + 1, "Attendance card exceeds the list width");
                    Label name = (Label) card.lookup("#name");
                    if (width <= 400) {
                        assertTrue(name.getHeight() > 2 * name.getFont().getSize(), "Long name should wrap");
                    }
                    Bounds nameBounds = name.localToScene(name.getLayoutBounds());
                    Label status = (Label) card.lookup("#status");
                    Bounds statusBounds = status.localToScene(status.getLayoutBounds());
                    assertTrue(nameBounds.getMaxX() <= statusBounds.getMinX(), "Name overlaps the status");

                    for (Node node : card.lookupAll(".label")) {
                        Label label = (Label) node;
                        Bounds bounds = label.localToScene(label.getLayoutBounds());
                        assertTrue(bounds.getMinX() >= cardBounds.getMinX(), "Label exceeds the card's left edge");
                        assertTrue(bounds.getMaxX() <= cardBounds.getMaxX() + 1,
                                "Label exceeds the card's right edge");
                        assertTrue(bounds.getMaxY() <= cardBounds.getMaxY() + 1,
                                "Label exceeds the card's bottom edge");
                        assertEquals(label.getText(), ((Text) label.lookup(".text")).getText(),
                                "Label text is truncated");
                    }
                }
                root.lookupAll(".scroll-bar").stream().map(ScrollBar.class::cast)
                        .filter(bar -> bar.getOrientation() == Orientation.HORIZONTAL)
                        .forEach(bar -> assertFalse(bar.isVisible(), "Attendance list needs horizontal scrolling"));
            }
            return null;
        });
    }

    @Test
    public void constructor_longGroupHeading_remainsReadableInSmallAndResizedWindows() throws Exception {
        FxTestUtil.callAndWait(() -> {
            var group = new seedu.address.model.label.Label("W".repeat(60));
            Person member = new PersonBuilder(ALICE).withLabels(group.labelName).build();
            for (List<Person> members : List.of(List.of(member), List.<Person>of())) {
                AttendanceReport report = AttendanceReport.of(members, DATE, group);
                Region root = new AttendanceListPanel(report).getRoot();
                Scene scene = new Scene(root, 400, 180);
                scene.getStylesheets().add(getClass().getResource("/view/DarkTheme.css").toExternalForm());
                root.applyCss();
                Region header = (Region) root.lookup(".attendance_header");
                // Leave less space than the empty-list placeholder's natural height, on every platform.
                double compactHeight = header.prefHeight(340) + 10;
                double[][] sizes = {{400, 180}, {800, 400}, {340, 160}, {340, compactHeight}, {400, 180}};
                for (double[] size : sizes) {
                    root.resize(size[0], size[1]);
                    root.applyCss();
                    for (int pass = 0; pass < 3; pass++) {
                        root.layout();
                    }

                    Label title = (Label) root.lookup("#title");
                    Label totals = (Label) root.lookup("#totals");
                    ListView<?> list = (ListView<?>) root.lookup("#attendanceListView");
                    assertEquals("Attendance: " + group.labelName + " on " + DATE, title.getText());
                    assertEquals(title.getText(), ((Text) title.lookup(".text")).getText(),
                            "Heading must display the full group and date without an ellipsis");
                    assertTrue(title.getHeight() + 1 >= title.prefHeight(title.getWidth()),
                            "Heading must have enough height for every wrapped line");
                    if (size[0] <= 400) {
                        assertTrue(title.getHeight() > 2 * title.getFont().getSize(), "Heading should wrap");
                    }
                    Bounds titleBounds = title.localToScene(title.getLayoutBounds());
                    Bounds totalsBounds = totals.localToScene(totals.getLayoutBounds());
                    Bounds listBounds = list.localToScene(list.getLayoutBounds());
                    assertTrue(titleBounds.getMaxX() <= size[0], "Heading must fit within the window width");
                    assertTrue(titleBounds.getMaxY() <= totalsBounds.getMinY(), "Heading must not overlap totals");
                    assertTrue(totalsBounds.getMaxY() <= listBounds.getMinY(), "Totals must not overlap the list");
                    assertEquals(totals.getText(), ((Text) totals.lookup(".text")).getText());
                    assertTrue(list.getHeight() > 0, "The student list must retain space below the heading");
                    assertTrue(listBounds.getMaxY() <= size[1] + 1,
                            "The list must fit within the window height at " + size[0] + " x " + size[1]
                                    + "; bottom was " + listBounds.getMaxY());
                }
            }
            return null;
        });
    }

    private static double assertBadgeVisible(Person student, double width) {
        AttendanceReport report = AttendanceReport.of(List.of(student), DATE, GROUP);
        Parent root = layOut(new AttendanceListPanel(report).getRoot(), width);
        ListView<?> list = (ListView<?>) root.lookup("#attendanceListView");
        Label status = (Label) root.lookup("#status");
        Label name = (Label) root.lookup("#name");
        Bounds listBounds = list.localToScene(list.getBoundsInLocal());
        Bounds statusBounds = status.localToScene(status.getBoundsInLocal());

        assertTrue(statusBounds.getMaxX() <= listBounds.getMaxX());
        return name.getHeight();
    }

    /**
     * Places {@code root} in a scene and lays it out, so that its list creates cells for the visible rows.
     */
    private static Parent layOut(Parent root) {
        return layOut(root, 400);
    }

    private static Parent layOut(Parent root, double width) {
        new Scene(root, width, 400);
        root.applyCss();
        root.layout();
        return root;
    }

}
