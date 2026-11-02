package seedu.address.testutil;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javafx.application.Platform;

/**
 * Starts JavaFX once per test JVM and bounds every wait for its event thread.
 * Toolkit failures fail tests explicitly; Linux test runs need a display such as xvfb-run.
 */
public final class FxTestUtil {
    private static final int TIMEOUT_SECONDS = 10;

    private FxTestUtil() {}

    /**
     * Waits for a confirmed JavaFX callback, retaining any initialization failure for subsequent callers.
     * Call from a test thread before scheduling JavaFX verifications.
     */
    public static void initialize() throws Exception {
        await(ToolkitState.STARTED, "toolkit initialization");
    }

    /**
     * Runs a verification on the JavaFX thread and propagates its exceptions and assertion failures.
     */
    public static void runAndWait(Runnable verification) throws Exception {
        callAndWait(() -> {
            verification.run();
            return null;
        });
    }

    /**
     * Returns a result produced on the JavaFX thread. After initialization, calls from that thread run directly.
     */
    public static <T> T callAndWait(Callable<T> action) throws Exception {
        initialize();
        if (Platform.isFxApplicationThread()) {
            return action.call();
        }
        CompletableFuture<T> result = new CompletableFuture<>();
        Platform.runLater(() -> {
            if (result.isDone()) {
                return;
            }
            try {
                result.complete(action.call());
            } catch (Throwable failure) {
                result.completeExceptionally(failure);
            }
        });
        return await(result, "test action");
    }

    private static CompletableFuture<Void> startToolkit() {
        CompletableFuture<Void> started = new CompletableFuture<>();
        Runnable confirmStartup = () -> {
            // Closing a test window must not shut down the shared event thread.
            Platform.setImplicitExit(false);
            started.complete(null);
        };
        Thread initializer = new Thread(() -> {
            try {
                try {
                    Platform.startup(confirmStartup);
                } catch (IllegalStateException alreadyInitialized) {
                    // A previous attempt can set JavaFX's initialized flag before failing.
                    // Only a callback proves that its event thread actually runs.
                    Platform.runLater(confirmStartup);
                }
            } catch (Throwable failure) {
                started.completeExceptionally(new AssertionError(
                        "JavaFX initialization failed. Check native libraries and display availability.", failure));
            }
        }, "javafx-test-startup");
        initializer.setDaemon(true);
        initializer.start();
        return started;
    }

    private static <T> T await(CompletableFuture<T> result, String operation) throws Exception {
        try {
            return result.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            AssertionError failure = new AssertionError("Timed out waiting for JavaFX " + operation + ".", e);
            // Keep startup timeouts as failures too, so later test classes do not retry a damaged toolkit.
            result.completeExceptionally(failure);
            throw failure;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        } catch (ExecutionException e) {
            if (e.getCause() instanceof Error error) {
                throw error;
            }
            if (e.getCause() instanceof Exception exception) {
                throw exception;
            }
            throw new AssertionError(e.getCause());
        }
    }

    /**
     * Lazily creates a single startup attempt, shared by every JavaFX test class.
     */
    private static class ToolkitState {
        private static final CompletableFuture<Void> STARTED = startToolkit();
    }
}
