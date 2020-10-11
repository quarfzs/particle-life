package gui;

import engine.Renderer;
import gui.colormaker.ColorMaker;
import gui.colormaker.RainbowColorMaker;
import guilib.constants.MouseButton;
import guilib.widgets.Widget;
import processing.core.PGraphics;

public class CanvasWidget extends Widget {

    private boolean pause = false;
    private Renderer renderer = null;
    private ColorMaker colorMaker = null;

    interface OnLoadListener {
        void onLoad();
    }

    private OnLoadListener onLoadListener = null;

    public void onLoad(OnLoadListener listener) {
        this.onLoadListener = listener;
    }

    private void testInit(PGraphics context) {
        if (colorMaker == null) {
            colorMaker = new RainbowColorMaker(context);
        }
        if (renderer == null) {
            renderer = new Renderer(getWidth(), getHeight(), colorMaker);
        }
    }

    public Renderer getRenderer() {
        return renderer;
    }

    @Override
    public void updateSize(int minWidth, int minHeight, int maxWidth, int maxHeight) {
        setSize(maxWidth, maxHeight);
    }

    public void setPause(boolean pause) {
        this.pause = pause;
        if (!pause) {
            requestRender();
        }
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
        renderer.mouseReleased(0);
    }

    @Override
    public void onMouseWheelMoved(int delta) {
        renderer.mouseScrolled(delta * 400);
    }

    @Override
    public void onKeyReleased(int keyCode, char key) {
        renderer.keyReleased(key);
    }

    @Override
    protected void render(PGraphics context) {
        testInit(context);

        if (onLoadListener != null) {
            onLoadListener.onLoad();
            onLoadListener = null;
        }

        clear(context);

        if (!pause) {
            renderer.update(0.02f);
        }
        renderer.updateUI(0.02f);
        renderer.draw(context);

        requestRender();
    }
}
