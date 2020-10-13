package guilib.widgets;

import guilib.Theme;
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

    private boolean highlighted = false;

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
        setSize(
                Utility.constrainDimension(minWidth, prefWidth, maxWidth),
                Math.min(Utility.constrainDimension(minHeight, prefHeight, maxHeight), prefHeight)
        );

        label.dx = switchWidth;
        label.dy = (getHeight() - label.getHeight()) / 2;
    }

    @Override
    public void onMousePressed(int x, int y, MouseButton button) {

    }

    @Override
    public void onMouseReleased(int x, int y, MouseButton button) {
        highlighted = Utility.rectIntersect(x, y, 0, 0, width, height);
        requestRender();

        if (Utility.rectIntersect(x, y, 0, 0, width, height)) {
            setStateInternal(!state);
        }
    }

    @Override
    public void onMouseHovered(int x1, int y1, int x2, int y2) {
        highlighted = Utility.rectIntersect(x2, y2, 0, 0, width, height);
        requestRender();
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
        int color = state ? Theme.getTheme().onColor : Theme.getTheme().offColor;
        if (highlighted) {
            color = Utility.light(color, 1.1);
        }

        int top = (getHeight() - switchWidth) / 2;

        context.noStroke();
        context.fill(color);
        context.rect(0, top, switchWidth, switchWidth);

        Utility.drawShadowOutline(context, 0, top, switchWidth - 1, switchWidth - 1);
    }
}
