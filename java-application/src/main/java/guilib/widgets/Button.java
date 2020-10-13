package guilib.widgets;

import guilib.Theme;
import guilib.constants.MouseButton;
import processing.core.PConstants;
import processing.core.PGraphics;

public class Button extends Widget {

    public interface OnClickListener {
        void onClick();
    }

    private final int padding = 5;

    private String title;

    private boolean pressed = false;
    private boolean highlighted = false;

    private OnClickListener onClickListener = null;

    public Button(String title) {
        this.title = title;
    }

    @Override
    public void updateSize(int minWidth, int minHeight, int maxWidth, int maxHeight) {
        int prefWidth = title.length() * 10;
        int prefHeight = 20;
        setSize(
                Utility.constrainDimension(minWidth, prefWidth, maxWidth),
                Math.min(Utility.constrainDimension(minHeight, prefHeight, maxHeight), prefHeight)
        );
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        this.sizeChanged();
        requestRender();
    }

    public void setOnClickListener(OnClickListener listener) {
        onClickListener = listener;
    }

    @Override
    public void onMousePressed(int x, int y, MouseButton button) {
        pressed = true;
        requestRender();
    }

    @Override
    public void onMouseReleased(int x, int y, MouseButton button) {
        pressed = false;
        highlighted = Utility.rectIntersect(x, y, 0, 0, width, height);

        requestRender();

        if (onClickListener != null && Utility.rectIntersect(x, y, 0, 0, width, height)) {
            onClickListener.onClick();
        }
    }

    @Override
    public void onMouseHovered(int x1, int y1, int x2, int y2) {
        highlighted = Utility.rectIntersect(x2, y2, 0, 0, width, height);
        requestRender();
    }

    @Override
    public void render(PGraphics context) {

        // background

        if (pressed || highlighted) {
            context.fill(Theme.getTheme().onColor);
        } else {
            context.fill(Theme.getTheme().offColor);
        }

        context.noStroke();
        context.rect(0, 0, width, height);

        // text

        context.noStroke();
        context.fill(Theme.getTheme().offColorContrast);

        if (context.textWidth(title) > width - 2 * padding) {
            context.textAlign(PConstants.LEFT, PConstants.CENTER);
            context.text(title, padding, height / 2f - 1);
        } else {
            context.textAlign(PConstants.CENTER, PConstants.CENTER);
            context.text(title, width / 2f, height / 2f - 1);
        }

        Utility.drawShadowOutline(context, 0, 0, width - 1, height - 1);
    }
}
