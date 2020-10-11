package guilib.widgets;

import processing.core.PGraphics;

import java.util.List;

public class ListView extends Widget {

    private Orientation orientation;
    private Align align;
    private List<Widget> children;

    private int margin;

    public ListView(Orientation orientation, Align align, List<Widget> children, int margin) {
        this.orientation = orientation;
        this.align = align;
        this.children = children;
        this.margin = margin;
    }

    @Override
    public void updateSize(int minWidth, int minHeight, int maxWidth, int maxHeight) {

        if (orientation == Orientation.HORIZONTAL) {

            setHeight(maxHeight);

            int totalWidth = 0;

            for (Widget child : children) {
                child.updateSize(0, getHeight(), -1, getHeight());

                child.dx = totalWidth;
                child.dy = alignChild(child.getHeight());
                totalWidth += child.getWidth();
                totalWidth += margin;
            }

            if (!children.isEmpty()) {
                totalWidth -= margin;
            }

            setWidth(Utility.constrainDimension(minWidth, totalWidth, maxWidth));

        } else {

            setWidth(maxWidth);

            int totalHeight = 0;

            for (Widget child : children) {
                child.updateSize(getWidth(), 0, getWidth(), -1);

                child.dx = alignChild(child.getWidth());
                child.dy = totalHeight;
                totalHeight += child.getHeight();
                totalHeight += margin;
            }

            if (!children.isEmpty()) {
                totalHeight -= margin;
            }

            setHeight(Utility.constrainDimension(minHeight, totalHeight, maxHeight));
        }
    }

    private int alignChild(int size) {
        switch (align) {
            case START:
                return 0;
            case END:
                if (orientation == Orientation.HORIZONTAL) {
                    return height;
                } else {
                    return width;
                }
            case CENTER:
                if (orientation == Orientation.HORIZONTAL) {
                    return (height - size) / 2;
                } else {
                    return (width - size) / 2;
                }
            default:
                return 0;
        }
    }

    @Override
    public Iterable<Widget> getChildren() {
        return children;
    }

    @Override
    public void render(PGraphics context) {
        clear(context);
    }
}
