package seedu.address.testutil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.stage.Stage;

public class FxTestUtilTest {
    @Test
    public void initialize_repeatedCalls_preservesRunningToolkit() throws Exception {
        FxTestUtil.initialize();
        FxTestUtil.initialize();

        assertTrue(FxTestUtil.callAndWait(Platform::isFxApplicationThread));
        assertFalse(FxTestUtil.callAndWait(Platform::isImplicitExit));
    }

    @Test
    public void callAndWait_nestedCall_runsWithoutDeadlock() throws Exception {
        assertEquals("completed", FxTestUtil.callAndWait(() -> FxTestUtil.callAndWait(() -> "completed")));
    }

    @Test
    public void runAndWait_assertionFailure_propagatesOriginalFailure() {
        AssertionError failure = new AssertionError("Expected verification failure");

        AssertionError actual = assertThrows(AssertionError.class, () -> FxTestUtil.runAndWait(() -> {
            throw failure;
        }));

        assertSame(failure, actual);
    }

    @Test
    public void callAndWait_exception_propagatesOriginalException() {
        Exception failure = new Exception("Expected checked exception");

        Exception actual = assertThrows(Exception.class, () -> FxTestUtil.callAndWait(() -> {
            throw failure;
        }));

        assertSame(failure, actual);
    }

    @Test
    public void runAndWait_lastWindowClosed_toolkitRemainsUsable() throws Exception {
        FxTestUtil.runAndWait(() -> {
            Stage window = new Stage();
            window.show();
            window.close();
        });

        assertTrue(FxTestUtil.callAndWait(Platform::isFxApplicationThread));
    }
}
