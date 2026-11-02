package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Point;
import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.geometry.Rectangle2D;
import seedu.address.commons.core.GuiSettings;

public class WindowPlacementTest {

    private static final double MINIMUM_WIDTH = 450;
    private static final double MINIMUM_HEIGHT = 400;
    // 1280 x 720 at 150%, reserving 60 physical pixels for the taskbar.
    private static final Rectangle2D SCALED_SCREEN = new Rectangle2D(0, 0, 1280 / 1.5, (720 - 60) / 1.5);
    private static final Rectangle2D LARGE_SCREEN = new Rectangle2D(0, 0, 1920, 1040);

    @Test
    public void fitToScreen_newWindowOnScaledDisplay_fitsAndCentres() {
        GuiSettings actual = fit(new GuiSettings(), SCALED_SCREEN);
        assertEquals(new GuiSettings(740, 440, 56, 0), actual);
        assertInside(actual, SCALED_SCREEN);
    }

    @Test
    public void fitToScreen_savedWindowOnUnscaledDisplay_preservesSizeAndPosition() {
        GuiSettings saved = new GuiSettings(900, 700, 50, 60);
        assertEquals(saved, fit(saved, LARGE_SCREEN));
    }

    @Test
    public void fitToScreen_savedWindowAfterIncreasingScaling_shrinksAndMovesInsideUsableArea() {
        GuiSettings actual = fit(new GuiSettings(1280, 720, 500, 100), SCALED_SCREEN);
        assertEquals(new GuiSettings(853, 440, 0, 0), actual);
        assertInside(actual, SCALED_SCREEN);
    }

    @Test
    public void fitToScreen_savedWindowNearScreenEdge_keepsEveryEdgeVisible() {
        GuiSettings actual = fit(new GuiSettings(740, 600, 500, 100), SCALED_SCREEN);
        assertEquals(new GuiSettings(740, 440, 113, 0), actual);
        assertInside(actual, SCALED_SCREEN);
    }

    @Test
    public void fitToScreen_disconnectedMonitor_recoversOnPrimaryScreen() {
        GuiSettings actual = fit(new GuiSettings(740, 600, 2000, 100), SCALED_SCREEN);
        assertEquals(new GuiSettings(740, 440, 113, 0), actual);
        assertInside(actual, SCALED_SCREEN);
    }

    @Test
    public void fitToScreen_savedWindowAboveOrLeftOfScreen_recoversOnPrimaryScreen() {
        GuiSettings actual = fit(new GuiSettings(740, 600, -1000, -100), SCALED_SCREEN);
        assertEquals(new GuiSettings(740, 440, 0, 0), actual);
        assertInside(actual, SCALED_SCREEN);
    }

    @Test
    public void fitToScreen_secondaryMonitorWithNegativeCoordinates_preservesItsPlacement() {
        Rectangle2D secondary = new Rectangle2D(-1366, 0, 1366, 728);
        GuiSettings saved = new GuiSettings(900, 600, -1200, 100);
        GuiSettings actual = WindowPlacement.fitToScreen(saved, List.of(LARGE_SCREEN, secondary), LARGE_SCREEN,
                MINIMUM_WIDTH, MINIMUM_HEIGHT);
        assertEquals(saved, actual);
        assertInside(actual, secondary);
    }

    @Test
    public void fitToScreen_topMenuBarAndTaskbar_staysInsideVisualBounds() {
        Rectangle2D screen = new Rectangle2D(0, 24, 1280, 656);
        GuiSettings actual = fit(new GuiSettings(1280, 720, 0, 0), screen);
        assertEquals(new GuiSettings(1280, 656, 0, 24), actual);
        assertInside(actual, screen);
    }

    @Test
    public void fitToScreen_windowBelowMinimumSize_growsEnoughForControls() {
        GuiSettings actual = fit(new GuiSettings(200, 100, 20, 20), SCALED_SCREEN);
        assertEquals(new GuiSettings(450, 400, 20, 20), actual);
        assertInside(actual, SCALED_SCREEN);
    }

    @Test
    public void fitToScreen_nonFiniteSavedSizes_recoversToUsableSize() {
        for (double size : new double[]{Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
            GuiSettings actual = fit(new GuiSettings(size, size, 0, 0), SCALED_SCREEN);
            assertEquals(new GuiSettings(450, 400, 0, 0), actual);
            assertInside(actual, SCALED_SCREEN);
        }
    }

    @Test
    public void fitToScreen_fractionalSecondaryScreenBounds_roundsWindowInward() {
        Rectangle2D screen = new Rectangle2D(-853.5, 24.5, 853.5, 415.5);
        GuiSettings actual = fit(new GuiSettings(1000, 600, -800, 40), screen);
        assertEquals(new GuiSettings(853, 415, -853, 25), actual);
        assertInside(actual, screen);
    }

    private GuiSettings fit(GuiSettings settings, Rectangle2D screen) {
        return WindowPlacement.fitToScreen(settings, List.of(screen), screen, MINIMUM_WIDTH, MINIMUM_HEIGHT);
    }

    private void assertInside(GuiSettings settings, Rectangle2D screen) {
        Point point = settings.getWindowCoordinates();
        assertTrue(point.getX() >= screen.getMinX());
        assertTrue(point.getY() >= screen.getMinY());
        assertTrue(point.getX() + settings.getWindowWidth() <= screen.getMaxX());
        assertTrue(point.getY() + settings.getWindowHeight() <= screen.getMaxY());
    }
}
