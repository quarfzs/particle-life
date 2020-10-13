package engine.colormaker;

import processing.core.PGraphics;

import static processing.core.PConstants.HSB;
import static processing.core.PConstants.RGB;

public abstract class ColorMaker {

    private final PGraphics context;

    public ColorMaker(PGraphics context) {
        this.context = context;
    }

    /**
     * Values from 0 to 255.
     */
    protected int rgb(float r, float g, float b) {
        context.pushStyle();
        context.colorMode(RGB, 255, 255, 255);
        int c = context.color(r, g, b);
        context.popStyle();
        return c;
    }

    /**
     * Values from 0 to 1.
     */
    protected int hsb(float h, float s, float b) {
        context.pushStyle();
        context.colorMode(HSB, 1f, 1f, 1f);
        int c = context.color(h, s, b);
        context.popStyle();
        return c;
    }

    protected int lerpColor(int color1, int color2, float factor) {
        return context.lerpColor(color1, color2, factor);
    }

    /**
     * Value from 0 to 1.
     */
    public abstract int getColor(float hue);
}
