package guilib.widgets;

import processing.core.PGraphics;

import java.util.ArrayList;

public class ScrollView extends Widget {

    private Orientation orientation;
    private Widget child;
    private ArrayList<Widget> children = new ArrayList<>(1);

    private double scrollOffset = 0;

    public ScrollView(Orientation orientation, Widget child) {
        this.orientation = orientation;
        this.child = child;

        children.add(child);
    }

    @Override
    public void updateSize(int minWidth, int minHeight, int maxWidth, int maxHeight) {

        setSize(maxWidth, maxHeight);

        int offset = (int) Math.round(scrollOffset);

        if (orientation == Orientation.HORIZONTAL) {
            child.dx = offset;
            child.dy = 0;
            child.updateSize(0, 0, -1, getHeight());
        } else {
            child.dx = 0;
            child.dy = offset;
            child.updateSize(0, 0, getWidth(), -1);
        }
    }

    @Override
    public Iterable<Widget> getChildren() {
        return children;
    }

    @Override
    public void render(PGraphics context) {

    }
}
