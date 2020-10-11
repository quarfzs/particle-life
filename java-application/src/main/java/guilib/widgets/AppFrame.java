package guilib.widgets;

import processing.core.PGraphics;

import java.util.ArrayList;
import java.util.List;

public class AppFrame extends Widget {

    private Widget childInner;
    private Widget childHorizontal;
    private Widget childVertical;

    private List<Widget> children;

    private double frameSizeFactor = 0.1;

    public AppFrame(Widget childInner, Widget childHorizontal, Widget childVertical) {
        this.childInner = childInner;
        this.childHorizontal = childHorizontal;
        this.childVertical = childVertical;

        children = new ArrayList<>(3);
        children.add(childInner);
        children.add(childHorizontal);
        children.add(childVertical);
    }

    @Override
    public void updateSize(int minWidth, int minHeight, int maxWidth, int maxHeight) {

        setSize(maxWidth, maxHeight);

        int frameWidth = (int) (getWidth() * frameSizeFactor);
        int frameHeight = (int) (getHeight() * frameSizeFactor);

        int innerWidth = getWidth() - frameWidth;
        int innerHeight = getHeight() - frameHeight;

        childHorizontal.updateSize(0, 0, getWidth(), frameHeight);
        childVertical.updateSize(0, 0, frameWidth, innerHeight);
        childVertical.dx = innerWidth;
        childVertical.dy = frameHeight;
        childInner.updateSize(0, 0, innerWidth, innerHeight);
        childInner.dy = frameHeight;
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
