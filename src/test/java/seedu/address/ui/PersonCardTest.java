package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import seedu.address.testutil.FxTestUtil;
import seedu.address.testutil.PersonBuilder;

/**
 * Checks that student cards distinguish group labels from Student ID badges.
 */
public class PersonCardTest {

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

}
