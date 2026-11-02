package seedu.address.ui;

import java.awt.Point;
import java.util.List;

import javafx.geometry.Rectangle2D;
import seedu.address.commons.core.GuiSettings;

/**
 * Fits restored window settings into a screen's usable area, measured in JavaFX logical coordinates.
 */
final class WindowPlacement {

    private WindowPlacement() {
    }

    /**
     * Keeps the saved window on its screen, falling back to the primary screen if that screen is unavailable.
     * New windows are centred. Screen bounds exclude taskbars and already account for display scaling.
     */
    static GuiSettings fitToScreen(GuiSettings settings, List<Rectangle2D> screens, Rectangle2D primaryScreen,
                                  double minimumWidth, double minimumHeight) {
        Point coordinates = settings.getWindowCoordinates();
        Rectangle2D screen = primaryScreen;
        if (coordinates != null) {
            for (Rectangle2D candidate : screens) {
                if (candidate.contains(coordinates.getX(), coordinates.getY())) {
                    screen = candidate;
                    break;
                }
            }
        }

        // Preferences store integer positions; round inward to stay inside fractional scaled screen bounds.
        double left = Math.ceil(screen.getMinX());
        double top = Math.ceil(screen.getMinY());
        double right = Math.floor(screen.getMaxX());
        double bottom = Math.floor(screen.getMaxY());
        double width = fitSize(settings.getWindowWidth(), minimumWidth, right - left);
        double height = fitSize(settings.getWindowHeight(), minimumHeight, bottom - top);
        double x = coordinates == null ? left + (right - left - width) / 2 : coordinates.getX();
        double y = coordinates == null ? top + (bottom - top - height) / 2 : coordinates.getY();
        x = Math.max(left, Math.min(x, right - width));
        y = Math.max(top, Math.min(y, bottom - height));
        return new GuiSettings(width, height, (int) Math.floor(x), (int) Math.floor(y));
    }

    private static double fitSize(double savedSize, double minimumSize, double availableSize) {
        double preferredSize = Double.isFinite(savedSize) ? savedSize : minimumSize;
        return Math.min(Math.max(preferredSize, minimumSize), availableSize);
    }
}
