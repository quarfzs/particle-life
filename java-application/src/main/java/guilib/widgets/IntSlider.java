package guilib.widgets;

import guilib.Theme;
import processing.core.PGraphics;

import java.util.ArrayList;

public class IntSlider extends SliderBase {

    public interface ChangeListener {
        void onChange(int value);
    }

    private String title;
    private int min;
    private int max;
    private int step;
    private ArrayList<ChangeListener> listeners = new ArrayList<>();

    public IntSlider(String title, int min, int max, int step) {
        this.title = title;
        this.min = min;
        this.max = max;
        this.step = step;

        setValue(min);
    }

    @Override
    protected void renderLine(PGraphics context, int x1, int x2, int y, boolean dragging, boolean hovering) {
        context.stroke(getColor2());
        context.line(x1, y, x2, y);

        int lineWidth = getWidth() - 2 * getMargin();
        int nSpaces = (max - min) / step;

        final int tickSize = 1;
        for (int i = 0; i <= nSpaces; i++) {
            int x = getMargin() + (int) (lineWidth * i / (double) nSpaces);
            context.line(x, y - tickSize, x, y + tickSize);
        }
    }

    @Override
    protected double constrainRatio(double ratio) {
        double f = (max - min) / (double) step;
        return Utility.constrain(0, Math.round(ratio * f) / f, 1);
    }

    @Override
    public void onMouseWheelMoved(int delta) {
        setRatio(getRatioFromValue(getValue() - delta * step));
        onUserChange();
    }

    private double getRatioFromValue(int value) {
        return (value - min) / (double) (max - min);
    }

    public void setValue(int value) {
        setRatio(getRatioFromValue(value));
    }

    public int getValue() {
        return min + (int) Math.round(getRatio() * (max - min));
    }

    @Override
    protected String getLabelText() {
        return String.format("%s: %d", title, getValue());
    }

    @Override
    protected void onUserChange() {
        listeners.forEach(changeListener -> changeListener.onChange(getValue()));
    }

    public void addChangeListener(ChangeListener listener) {
        listeners.add(listener);
    }
}
