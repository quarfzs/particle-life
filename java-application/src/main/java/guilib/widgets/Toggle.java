package guilib.widgets;

import guilib.constants.MouseButton;
import processing.core.PGraphics;

import java.util.ArrayList;
import java.util.List;

public class Toggle extends Widget {

    public interface ChangeListener {
        void onChange(boolean state);
    }

    private boolean state;
    private ChangeListener changeListener = null;

    private int switchWidth = 30;
    private Label label;
    private List<Widget> children;

    public Toggle(String title, boolean state) {
        this.state = state;

        label = new Label(title);

        label.addOnClickListener(() -> this.setStateInternal(!this.state));

        children = new ArrayList<>(1);
        children.add(label);

        sizeChanged();
    }

    @Override
    public Iterable<Widget> getChildren() {
        return children;
    }

    @Override
    public void updateSize(int minWidth, int minHeight, int maxWidth, int maxHeight) {

        switchWidth = 16;
        minWidth = switchWidth + 1;

        label.updateSize(minWidth - switchWidth, 0, maxWidth == -1 ? -1 : Math.max(maxWidth - switchWidth,1), getHeight());

        int prefWidth = switchWidth + label.getWidth();
        int prefHeight = Math.max(switchWidth, label.getHeight());
        setSize(Utility.constrainDimension(minWidth, prefWidth, maxWidth), Utility.constrainDimension(minHeight, prefHeight, maxHeight));

        label.dx = switchWidth;
        label.dy = (getHeight() - label.getHeight()) / 2;
    }

    @Override
    public void onMousePressed(int x, int y, MouseButton button) {
        setStateInternal(!state);
    }

    public void setChangeListener(ChangeListener listener) {
        changeListener = listener;
    }

    public void setState(boolean state) {
        if (state != this.state) {
            this.state = state;
            requestRender();
        }
    }

    private void setStateInternal(boolean state) {
        if (state != this.state) {
            this.state = state;
            requestRender();
            if (changeListener != null) {
                changeListener.onChange(state);
            }
        }
    }

    public boolean getState() {
        return state;
    }

    @Override
    protected void render(PGraphics context) {
        clear(context);

        // switch
        if (state) {
            context.fill(100, 255, 100);
        } else {
            context.fill(context.color(200));
        }
        context.strokeWeight(1);
        context.stroke(0, 0, 0);
        context.rect(0, (getHeight() - switchWidth) / 2, switchWidth - 1, switchWidth - 1);
    }
}
