package seedu.address.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import seedu.address.model.person.Person;

/**
 * A UI component that displays information of a {@code Person}.
 */
public class PersonCard extends UiPart<Region> {

    private static final String FXML = "PersonListCard.fxml";

    /**
     * Note: Certain keywords such as "location" and "resources" are reserved keywords in JavaFX.
     * As a consequence, UI elements' variable names cannot be set to such keywords
     * or an exception will be thrown by JavaFX during runtime.
     *
     * @see <a href="https://github.com/se-edu/addressbook-level4/issues/336">The issue on AddressBook level 4</a>
     */

    public final Person person;

    @FXML
    private HBox cardPane;
    @FXML
    private Label name;
    @FXML
    private Label id;
    @FXML
    private Label email;
    @FXML
    private Label remark;
    @FXML
    private FlowPane labels;
    @FXML
    private FlowPane studentIds;

    /**
     * Creates a {@code PersonCard} with the given {@code Person} and index to display.
     */
    public PersonCard(Person person, int displayedIndex) {
        super(FXML);
        this.person = person;
        id.setText(displayedIndex + ". ");
        name.setText(person.getName().fullName);
        email.setText(person.getEmail().value);
        remark.setText("Remark: " + person.getRemark().value);
        remark.setVisible(!person.getRemark().value.isEmpty());
        remark.setManaged(remark.isVisible());
        person.getLabels().stream()
                .sorted(seedu.address.model.label.Label.DISPLAY_ORDER)
                .forEach(label -> {
                    Label groupBadge = new Label(label.labelName) {
                        @Override
                        protected double computePrefHeight(double width) {
                            // FlowPane asks for height without supplying the badge's constrained width.
                            return super.computePrefHeight(width < 0 ? labels.getWidth() : width);
                        }
                    };
                    groupBadge.setWrapText(true);
                    groupBadge.setMinWidth(0);
                    // A single long badge must fit within the row, just like multiple short badges.
                    groupBadge.maxWidthProperty().bind(labels.widthProperty());
                    groupBadge.getStyleClass().add("group_label_badge");
                    labels.getChildren().add(groupBadge);
                });
        Label studentIdBadge = new Label("Student ID: " + person.getStudentId());
        studentIdBadge.getStyleClass().add("student_id_badge");
        studentIds.getChildren().add(studentIdBadge);
    }
}
