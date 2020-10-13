package guilib.widgets;

import guilib.Theme;
import guilib.constants.MouseButton;
import processing.core.PConstants;
import processing.core.PGraphics;

import java.util.List;

abstract class SliderBase extends Widget {

    private final int knobRadius = 7;
    private double ratio;
    private boolean draggingKnob = false;
    private boolean hoveringKnob = false;
    private Label label;
    private List<Widget> children;
    private boolean labelTextSet = false;

    public SliderBase() {
        label = new Label("");
        children = List.of(label);
        setRatio(0);
    }

    protected abstract String getLabelText();

    @Override
    public void updateSize(int minWidth, int minHeight, int maxWidth, int maxHeight) {
        label.updateSize(minWidth, minHeight, maxWidth, maxHeight);
        int prefHeight = label.getHeight() + 2 * getMargin();
        setSize(minWidth, Utility.constrainDimension(minHeight, prefHeight, maxHeight));
    }

    @Override
    public void onMousePressed(int x, int y, MouseButton button) {
        draggingKnob = true;
        setRatioByMouse(x);
        requestRender();
    }

    @Override
    public void onMouseReleased(int x, int y, MouseButton button) {
        draggingKnob = false;
        requestRender();
    }

    @Override
    public void onMouseHovered(int x1, int y1, int x2, int y2) {
        hoveringKnob = onKnob(x2, y2);
        requestRender();
    }

    @Override
    public void onMouseDragged(int x1, int y1, int x2, int y2) {
        hoveringKnob = onKnob(x2, y2);
        if (draggingKnob) {
            setRatioByMouse(x2);
        }
    }

    @Override
    public void onMouseWheelMoved(int delta) {
        setRatio(getRatioFromKnobLineX(getKnobLineX() - delta * 3));
        onUserChange();
    }

    private boolean onKnob(int x, int y) {
        int knobX = getKnobX();
        int knobY = height / 2;
        return Math.hypot(x - knobX, y - knobY) < knobRadius;
    }

    private void setRatioByMouse(int mouseX) {
        setRatio(getRatioFromKnobLineX(mouseX - getMargin()));
        onUserChange();
    }

    @Override
    protected final void render(PGraphics context) {

        if (!labelTextSet) {
            label.setText(getLabelText());
            labelTextSet = true;
        }

        // background
        clear(context);

        int labelHeight = label.getHeight();

        int y = labelHeight + (getHeight() - labelHeight) / 2;

        renderLine(context, getMargin(), getWidth() - getMargin(), y, draggingKnob, hoveringKnob);
        renderKnob(context, getKnobX(), y, draggingKnob, hoveringKnob);
    }

    private int getKnobX() {
        return getMargin() + getKnobLineX();
    }

    protected int getMargin() {
        return knobRadius + 2;
    }

    private int getKnobLineX() {
        int lineWidth = getWidth() - 2 * getMargin();
        return (int) (getKnobDrawRatio() * lineWidth);
    }

    protected double getKnobDrawRatio() {
        return getRatio();
    }

    protected void renderLine(PGraphics context, int x1, int x2, int y, boolean dragging, boolean hovering) {
        context.stroke(getColor2());
        context.line(x1, y, x2, y);
    }

    protected void renderKnob(PGraphics context, int x, int y, boolean dragging, boolean hovering) {
        context.fill(getColor1());
        context.stroke(getColor2());
        context.ellipseMode(PConstants.CENTER);
        context.ellipse(x, y, knobRadius * 2, knobRadius * 2);
    }

    protected final int getColor1() {
        int color = Theme.getTheme().primary;
        if (draggingKnob || hoveringKnob) {
            color = Utility.light(color, 1.3);
        }
        return color;
    }

    protected final int getColor2() {
        int color = Theme.getTheme().backgroundContrast;
        if (draggingKnob || hoveringKnob) {
            color = Utility.light(color, 1.3);
        }
        return color;
    }

    protected final double getRatio() {
        return ratio;
    }

    /**
     * @param linePixels where is the position on the line (in pixels, starting from left)?
     * @return the ratio if the knob was at that position on the line.
     */
    private double getRatioFromKnobLineX(int linePixels) {
        return linePixels / (double) (width - 2 * getMargin());
    }

    /**
     * Constrains the given ratio with constrainRatio() and requests render if the value changed.
     */
    protected final void setRatio(double ratio) {
        double newRatio = constrainRatio(ratio);
        if (newRatio != this.ratio || !labelTextSet) {
            this.ratio = newRatio;
            label.setText(getLabelText());
            labelTextSet = true;
            requestRender();
        }
    }

    /**
     * Gets invoked if the user causes a change of the value by interacting with the slider.
     */
    protected abstract void onUserChange();

    protected double constrainRatio(double ratio) {
        return Utility.constrain(0, ratio, 1);
    }

    @Override
    public Iterable<Widget> getChildren() {
        return children;
    }
}
