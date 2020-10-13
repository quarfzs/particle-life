package guilib.widgets;

import guilib.GraphicsProvider;
import guilib.Theme;
import guilib.constants.MouseButton;
import processing.core.PGraphics;

import java.util.ArrayList;
import java.util.List;

public abstract class Widget {

    interface OnClickListener {
        void onClick();
    }

    interface OnMouseWheelListener {
        void onMouseWheelMoved(int delta);
    }

    private PGraphics canvas = null;

    protected int dx;
    protected int dy;
    protected int width = 16;
    protected int height = 16;

    private boolean renderRequested = true;
    protected boolean active = false;

    private final List<OnClickListener> onClickListeners = new ArrayList<>();
    private final List<OnMouseWheelListener> onMouseWheelListeners = new ArrayList<>();

    public final void requestRender() {
        renderRequested = true;
    }

    public final boolean isRenderRequested() {
        return renderRequested;
    }

    public final void setActive(boolean active) {
        if (active != this.active) {
            this.active = active;
            activeChanged();
        }
    }

    protected void activeChanged() {
    }

    public final void addOnClickListener(OnClickListener listener) {
        onClickListeners.add(listener);
    }

    public final void addOnMouseWheelListener(OnMouseWheelListener listener) {
        onMouseWheelListeners.add(listener);
    }

    /**
     * Will be called by the parent.
     * The Parameters are the amount of space that the parent is willing to give to the child.
     * If a max value is -1, that means that the size is not limited in that case. (For min values, 0 is used.)
     * In this method, the widget should set its size to something reasonable inside the the given range.
     * The child can however choose to ignore these limits and set its size smaller/larger anyway.
     */
    public abstract void updateSize(int minWidth, int minHeight, int maxWidth, int maxHeight);

    protected final void setSize(int width, int height) {
        if (width != this.width || height != this.height) {
            this.width = width;
            this.height = height;
            requestRender();
            this.sizeChanged();
        }
    }

    public final void setWidth(int width) {
        if (width != this.width) {
            this.width = width;
            requestRender();
            this.sizeChanged();
        }
    }

    public final void setHeight(int height) {
        if (height != this.height) {
            this.height = height;
            requestRender();
            this.sizeChanged();
        }
    }

    protected void sizeChanged() {
    }

    public final int getDx() {
        return dx;
    }

    public final int getDy() {
        return dy;
    }

    public final int getWidth() {
        return width;
    }

    public final int getHeight() {
        return height;
    }

    public Iterable<Widget> getChildren() {
        return null;
    }

    public PGraphics getImage() {
        return canvas;
    }

    public PGraphics renderImage(GraphicsProvider graphicsProvider) {
        renderRequested = false;
        if (canvas == null || canvas.width != width || canvas.height != height) {
            canvas = graphicsProvider.createGraphics(width, height);
        }
        canvas.beginDraw();
        render(canvas);
        if (!(this instanceof AppFrame)) {
            //renderDebug(canvas);
        }
        canvas.endDraw();
        return canvas;
    }

    protected final void renderDebug(PGraphics context) {
        context.pushStyle();
        context.noFill();
        context.stroke(context.color(255,0,255));
        context.line(0,0,width,height);
        context.line(0,height,width,0);
        context.popStyle();
    }

    protected final void clear(PGraphics context, int color) {
        context.pushStyle();
        context.fill(color);
        context.noStroke();
        context.rect(0, 0, width, height);
        context.popStyle();
    }

    protected final void clear(PGraphics context) {
        clear(context, Theme.getInstance().background);
    }

    protected abstract void render(PGraphics context);

    public final void mouseClicked(int x, int y, MouseButton button) {
        onClickListeners.forEach(OnClickListener::onClick);
        onMouseClicked(x, y, button);
    }

    public final void mouseWheelMoved(int delta) {
        onMouseWheelListeners.forEach(listener -> listener.onMouseWheelMoved(delta));
        onMouseWheelMoved(delta);
    }

    protected void onMouseClicked(int x, int y, MouseButton button) {
    }

    public void onMousePressed(int x, int y, MouseButton button) {
    }

    public void onMouseReleased(int x, int y, MouseButton button) {
    }

    public void onMouseHovered(int x1, int y1, int x2, int y2) {
    }

    public void onMouseDragged(int x1, int y1, int x2, int y2) {
    }

    public void onMouseWheelMoved(int delta) {
    }

    public void onKeyPressed(int keyCode, char key) {
    }

    public void onKeyReleased(int keyCode, char key) {
    }
}
