package guilib.widgets;

import processing.core.PGraphics;

import java.util.ArrayList;
import java.util.List;

public class Container extends Widget {

    private List<Widget> children = new ArrayList<>(1);

    public Container(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void setContent(Widget widget) {
        widget.setSize(width, height);
        children.clear();
        children.add(widget);
    }

    @Override
    public void updateSize(int minWidth, int minHeight, int maxWidth, int maxHeight) {
        children.forEach(widget -> widget.updateSize(minWidth, minHeight, maxWidth, maxHeight));
        if (children.isEmpty()) {
            this.setSize(minWidth, minHeight);
        } else {
            this.setSize(Utility.constrainDimension(minWidth, children.get(0).getWidth(), maxWidth), Utility.constrainDimension(minHeight, children.get(0).getHeight(), maxWidth));
        }
    }

    @Override
    public Iterable<Widget> getChildren() {
        return children;
    }

    @Override
    protected void render(PGraphics context) {
    }
}
