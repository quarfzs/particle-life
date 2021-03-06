package gui;

import engine.Renderer;
import engine.requests.*;
import guilib.constants.MouseButton;
import guilib.widgets.Widget;
import processing.core.PGraphics;

public class CanvasWidget extends Widget {

    private Renderer renderer = null;

    interface OnLoadListener {
        void onLoad();
    }

    private OnLoadListener onLoadListener = null;

    public void onLoad(OnLoadListener listener) {
        this.onLoadListener = listener;
    }

    public Renderer getRenderer() {
        return renderer;
    }

    @Override
    public void updateSize(int minWidth, int minHeight, int maxWidth, int maxHeight) {
        setSize(maxWidth, maxHeight);
    }

    @Override
    public void onMouseDragged(int x1, int y1, int x2, int y2) {
        renderer.mouseMoved(x2, y2);
    }

    @Override
    public void onMouseHovered(int x1, int y1, int x2, int y2) {
        renderer.mouseMoved(x2, y2);
    }

    @Override
    public void onMousePressed(int x, int y, MouseButton button) {
        renderer.mousePressed(switch (button) {
            case LEFT -> 0;
            case WHEEL -> 1;
            case RIGHT -> 2;
        });
    }

    @Override
    public void onMouseReleased(int x, int y, MouseButton button) {
        renderer.mouseReleased(switch (button) {
            case LEFT -> 0;
            case WHEEL -> 1;
            case RIGHT -> 2;
        });
    }

    @Override
    public void onMouseWheelMoved(int delta) {
        renderer.mouseScrolled(delta * 400);
    }

    @Override
    public void onKeyReleased(int keyCode, char key) {
        switch (key) {
            case 'r' -> renderer.request(new RequestRandomMatrix());
            case 's' -> renderer.request(new RequestRespawn());
            case 'q' -> {
                renderer.request(new RequestRandomMatrix());
                renderer.request(new RequestRespawn());
            }
            case ' ' -> renderer.request(new RequestPause(!renderer.isPaused()));
        }
    }

    @Override
    protected void render(PGraphics context) {

        if (renderer == null) {
            renderer = new Renderer(getWidth(), getHeight());
        }

        if (onLoadListener != null) {
            onLoadListener.onLoad();
            onLoadListener = null;
        }

        clear(context);

        renderer.update();
        renderer.handleRequests();
        renderer.updateCamera();
        renderer.draw(context);

        requestRender();
    }
}
