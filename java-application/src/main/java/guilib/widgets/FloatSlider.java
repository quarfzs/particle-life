package guilib.widgets;

import java.util.ArrayList;

public class FloatSlider extends SliderBase {

    public interface ChangeListener {
        void onChange(double value);
    }

    private String title;
    private double min;
    private double max;
    private int digits;
    private ArrayList<ChangeListener> listeners = new ArrayList<>();

    public FloatSlider(String title, double min, double max, int digits) {
        this.title = title;
        this.min = min;
        this.max = max;
        this.digits = digits;

        setValue(min);
    }

    public void setValue(double value) {
        setRatio(getRatioFromValue(value));
    }

    public double getValue() {
        return min + getRatio() * (max - min);
    }

    private double getRatioFromValue(double value) {
        return (value - min) / (max - min);
    }

    @Override
    protected String getLabelText() {
        return String.format(String.format("%%s: %%4.%df", digits), title, getValue());
    }

    @Override
    protected void onUserChange() {
        listeners.forEach(changeListener -> changeListener.onChange(getValue()));
    }

    public void addChangeListener(ChangeListener listener) {
        listeners.add(listener);
    }
}
