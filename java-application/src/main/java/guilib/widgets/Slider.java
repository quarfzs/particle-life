package guilib.widgets;

public abstract class Slider<T> {

    private ChangeListener changeListener = null;

    public interface ChangeListener<T> {
        void onChange(T value);
    }

    public void setChangeListener(ChangeListener listener) {
        changeListener = listener;
    }

    protected void onChange(T value) {
        if (this.changeListener != null) {
            this.changeListener.onChange(value);
        }
    }
}
