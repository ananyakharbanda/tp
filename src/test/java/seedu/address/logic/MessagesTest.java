package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class MessagesTest {

    @Test
    public void format_withRemark_includesRemark() {
        // EP: non-empty remark is shown after the labels (#208)
        Person person = new PersonBuilder().withLabels("Tutorial 1").withRemark("Needs help with recursion").build();

        assertEquals("Amy Bee; Student ID: A0000000A; Email: amy@gmail.com; Labels: [tutorial 1]; "
                + "Remark: Needs help with recursion", Messages.format(person));
    }

    @Test
    public void format_multipleLabels_listsLabelsInDisplayOrder() {
        // EP: labels are listed alphabetically ignoring case, not in set order (#209)
        Person person = new PersonBuilder().withLabels("Zeta", "alpha", "Lab B").build();

        assertEquals("Amy Bee; Student ID: A0000000A; Email: amy@gmail.com; Labels: [alpha][lab b][zeta]",
                Messages.format(person));
    }

    @Test
    public void format_emptyRemark_omitsRemark() {
        // Boundary: an empty remark adds no remark section
        Person person = new PersonBuilder().withLabels("Tutorial 1").build();

        assertEquals("Amy Bee; Student ID: A0000000A; Email: amy@gmail.com; Labels: [tutorial 1]",
                Messages.format(person));
    }
}
