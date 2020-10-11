package guilib.widgets;

import processing.core.PConstants;
import processing.core.PGraphics;

public final class Utility {

    private static PGraphics colorContext;

    public static void setColorContext(PGraphics context) {
        colorContext = context;
    }

    public static int light(int color, double relativeBrightness) {
        colorContext.pushStyle();
        float originalHue = colorContext.hue(color);
        float originalSaturation = colorContext.saturation(color);
        float originalBrightness = colorContext.brightness(color);
        colorContext.colorMode(PConstants.HSB);
        int finalColor = colorContext.color(originalHue, originalSaturation, (float) (originalBrightness * relativeBrightness));
        colorContext.popStyle();
        return finalColor;
    }

    public static boolean rectIntersect(int x, int y, int left, int top, int width, int height) {
        return x > left && x < left + width && y > top && y < top + height;
    }

    public static int constrain(int min, int value, int max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    public static double constrain(double min, double value, double max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    /**
     * Ignores the constraints if they are -1.
     */
    public static int constrainDimension(int min, int value, int max) {
        if (min != -1 && value < min) return min;
        if (max != -1 && value > max) return max;
        return value;
    }
}
