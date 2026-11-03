package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.animation.AnimationTimer;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import seedu.address.logic.LogicManager;
import seedu.address.model.ModelManager;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.FxTestUtil;
import seedu.address.testutil.PersonBuilder;

/**
 * Checks that student cards distinguish group labels from Student ID badges.
 */
public class PersonCardTest {

    @TempDir
    public Path temporaryFolder;

    @BeforeAll
    public static void startToolkit() throws Exception {
        FxTestUtil.initialize();
    }

    @Test
    public void constructor_studentWithGroupLabel_displaysDistinctBadges() throws Exception {
        FxTestUtil.runAndWait(() -> {
            // EP: a student with a group label has both badge types
            Parent root = new PersonCard(new PersonBuilder(ALICE).withLabels("Tutorial 1").build(), 1).getRoot();
            Scene scene = new Scene(root, 400, 300);
            scene.getStylesheets().add(PersonCardTest.class.getResource("/view/DarkTheme.css").toExternalForm());
            root.applyCss();

            Label groupBadge = (Label) ((FlowPane) root.lookup("#labels")).getChildren().get(0);
            Label studentIdBadge = (Label) ((FlowPane) root.lookup("#studentIds")).getChildren().get(0);
            assertEquals("tutorial 1", groupBadge.getText());
            assertEquals("Student ID: A1234567B", studentIdBadge.getText());
            assertTrue(groupBadge.getStyleClass().contains("group_label_badge"));
            assertTrue(studentIdBadge.getStyleClass().contains("student_id_badge"));
            assertNotNull(groupBadge.getBackground());
            assertNotNull(studentIdBadge.getBorder());
            assertNotEquals(groupBadge.getBackground(), studentIdBadge.getBackground());
        });
    }

    @Test
    public void constructor_studentWithoutGroupLabel_displaysStudentIdOnly() throws Exception {
        FxTestUtil.runAndWait(() -> {
            // EP: a student without group labels still has an identifiable Student ID badge
            Parent root = new PersonCard(new PersonBuilder(ALICE).withLabels().build(), 1).getRoot();
            FlowPane labels = (FlowPane) root.lookup("#labels");
            FlowPane studentIds = (FlowPane) root.lookup("#studentIds");
            assertTrue(labels.getChildren().isEmpty());
            assertEquals(1, studentIds.getChildren().size());
            assertTrue(studentIds.getChildren().get(0).getStyleClass().contains("student_id_badge"));
        });
    }

    @Test
    public void longGroupLabel_narrowAndResizedList_wrapsWithoutClippingDetails() throws Exception {
        FxTestUtil.runAndWait(() -> {
            // EP: a card with a long group label keeps all student details visible when resized
            // BVA: 60-character label at narrow and wide list widths
            var person = new PersonBuilder().withName("Long Label Student")
                    .withStudentId("A0123460F")
                    .withEmail("a".repeat(64) + "@example.com")
                    .withRemark("Short remark")
                    .withLabels("Tutorial", "W".repeat(60)).build();
            Region root = new PersonListPanel(FXCollections.observableArrayList(person)).getRoot();
            Scene scene = new Scene(root, 400, 400);
            scene.getStylesheets().add(getClass().getResource("/view/DarkTheme.css").toExternalForm());

            for (double width : new double[] {400, 800, 340, 400}) {
                root.resize(width, 400);
                root.applyCss();
                for (int pass = 0; pass < 3; pass++) {
                    root.layout();
                }

                Region card = (Region) root.lookup("#cardPane");
                Bounds cardBounds = card.localToScene(card.getLayoutBounds());
                assertTrue(cardBounds.getMaxX() <= width + 1, "Card exceeds the list width");
                for (Node node : card.lookupAll(".label")) {
                    Label label = (Label) node;
                    Bounds bounds = label.localToScene(label.getLayoutBounds());
                    assertTrue(bounds.getMaxX() <= cardBounds.getMaxX() + 1,
                            "Label exceeds the card width: " + label.getText());
                    assertTrue(bounds.getMaxY() <= cardBounds.getMaxY() + 1,
                            "Label exceeds the card height: " + label.getText());
                    Text text = (Text) label.lookup(".text");
                    assertEquals(label.getText(), text.getText(), "Label text is truncated");
                    assertTrue(label.getHeight() + 1 >= label.prefHeight(label.getWidth()),
                            "Wrapped label has insufficient height");
                }
                Label group = (Label) card.lookupAll("#labels .label").stream()
                        .filter(node -> ((Label) node).getText().startsWith("w")).findFirst().orElseThrow();
                Label email = (Label) card.lookup("#email");
                if (width <= 400) {
                    assertTrue(group.getHeight() > 2 * group.getFont().getSize(), "Group should wrap");
                    assertTrue(email.getHeight() > 2 * email.getFont().getSize(), "Email should wrap");
                }
                root.lookupAll(".scroll-bar").stream().map(ScrollBar.class::cast)
                        .filter(bar -> bar.getOrientation() == javafx.geometry.Orientation.HORIZONTAL)
                        .forEach(bar -> assertFalse(bar.isVisible(), "List needs horizontal scrolling"));
            }
        });
    }

    @Test
    public void longGroupLabel_liveCommandsAndResize_keepCompleteBadgesAboveStudentIds() throws Exception {
        // BVA: maximum-length group labels at the application's minimum supported window size.
        // EP: adding, filtering, restoring the list, and resizing recreate or relayout visible cards.
        Path dataFile = temporaryFolder.resolve("students.json");
        LogicManager logic = new LogicManager(new ModelManager(), new StorageManager(
                new JsonAddressBookStorage(dataFile),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
        Stage stage = FxTestUtil.callAndWait(() -> {
            Stage windowStage = new Stage();
            MainWindow window = new MainWindow(windowStage, logic, dataFile);
            window.show();
            window.fillInnerParts();
            windowStage.setWidth(450);
            windowStage.setHeight(400);
            return windowStage;
        });
        try {
            awaitLayoutPulses();
            String group = "W".repeat(60);
            enterCommandAndCheckLabels(stage, "add n/Alice i/A0000001B e/alice@example.com l/" + group, 1);
            enterCommandAndCheckLabels(stage, "add n/Beth i/A0000002B e/beth@example.com l/" + group, 2);
            enterCommandAndCheckLabels(stage, "find n/Beth", 1);
            enterCommandAndCheckLabels(stage, "list", 2);
            for (double width : new double[] {800, 450, 600, 450}) {
                FxTestUtil.runAndWait(() -> stage.setWidth(width));
                awaitLayoutPulses();
                FxTestUtil.runAndWait(() -> assertCompleteGroupLabels(stage, 2));
            }
        } finally {
            FxTestUtil.runAndWait(stage::close);
        }
    }

    private void enterCommandAndCheckLabels(Stage stage, String command, int expectedCount) throws Exception {
        FxTestUtil.runAndWait(() -> {
            TextField input = (TextField) stage.getScene().lookup("#commandTextField");
            input.setText(command);
            input.fireEvent(new ActionEvent());
            assertTrue(input.getText().isEmpty(), "Command should succeed: " + command);
        });
        awaitLayoutPulses();
        FxTestUtil.runAndWait(() -> assertCompleteGroupLabels(stage, expectedCount));
    }

    private void assertCompleteGroupLabels(Stage stage, int expectedCount) {
        ListView<?> students = (ListView<?>) stage.getScene().lookup("#personListView");
        assertEquals(expectedCount, students.getItems().size());
        int checkedBadges = 0;
        for (Node node : students.lookupAll(".list-cell")) {
            if (!(node instanceof ListCell<?> cell) || cell.isEmpty() || cell.getGraphic() == null) {
                continue;
            }
            Node card = cell.getGraphic();
            FlowPane groups = (FlowPane) card.lookup("#labels");
            Node studentIds = card.lookup("#studentIds");
            Bounds groupBounds = groups.localToScene(groups.getLayoutBounds());
            Bounds studentIdBounds = studentIds.localToScene(studentIds.getLayoutBounds());
            for (Node badge : groups.getChildren()) {
                Label label = (Label) badge;
                Bounds bounds = label.localToScene(label.getLayoutBounds());
                Text text = (Text) label.lookup(".text");
                assertNotNull(text, "Group badge must have rendered text");
                assertEquals(label.getText(), text.getText(), "Group label is truncated");
                assertTrue(label.getHeight() + 1 >= label.prefHeight(label.getWidth()),
                        "Wrapped group label has insufficient height");
                assertTrue(bounds.getMaxY() <= groupBounds.getMaxY() + 1,
                        "Group label exceeds its container height");
                assertTrue(bounds.getMaxY() <= studentIdBounds.getMinY() + 1,
                        "Group label overlaps the Student ID badge");
                checkedBadges++;
            }
        }
        assertTrue(checkedBadges > 0, "At least one student badge must be rendered");
    }

    private void awaitLayoutPulses() throws Exception {
        CompletableFuture<Void> completed = new CompletableFuture<>();
        FxTestUtil.runAndWait(() -> new AnimationTimer() {
            private int pulses;

            @Override
            public void handle(long now) {
                if (++pulses == 3) {
                    stop();
                    completed.complete(null);
                }
            }
        }.start());
        completed.get(10, TimeUnit.SECONDS);
    }

}
