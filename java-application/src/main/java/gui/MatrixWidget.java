package gui;

import guilib.constants.MouseButton;
import guilib.widgets.Widget;
import logic.Matrix;
import processing.core.PGraphics;

import java.util.ArrayList;

public class MatrixWidget extends Widget {

    private Matrix matrix = null;
    private int hoveredBoxRow = -1;
    private int hoveredBoxCol = -1;

    private ArrayList<MatrixChangeListener> matrixChangeListeners = new ArrayList<>();
    private ArrayList<RemoveTypeListener> removeTypeListeners = new ArrayList<>();

    public MatrixWidget() {
    }

    public void matrixChanged(Matrix matrix) {
        this.matrix = matrix;
        requestRender();
    }

    public void addMatrixChangeListener(MatrixChangeListener listener) {
        matrixChangeListeners.add(listener);
    }

    public void addRemoveTypeListener(RemoveTypeListener listener) {
        removeTypeListeners.add(listener);
    }

    @Override
    public void updateSize(int minWidth, int minHeight, int maxWidth, int maxHeight) {
        if (maxWidth == -1 && maxHeight == -1) {
            setSize(100, 100);
        } else if (maxWidth == -1) {
            setSize(maxHeight, maxHeight);
        } else if (maxHeight == -1) {
            setSize(maxWidth, maxWidth);
        } else {
            int s = Math.min(maxWidth, maxHeight);
            setSize(s, s);
        }
    }

    @Override
    protected void render(PGraphics context) {
        clear(context);

        if (matrix != null) {

            context.noStroke();

            int n = matrix.size();
            int size = Math.min(getWidth(), getHeight()) / n;

            int w = n * size;
            int h = n * size;
            int x0 = (getWidth() - w) / 2;
            int y0 = (getHeight() - h) / 2;

            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    float value = matrix.get(i, j);
                    if (value >= 0) {
                        context.fill(context.color(0, 255 * value, 0));
                    } else {
                        context.fill(context.color(255 * Math.abs(value), 0, 0));
                    }
                    context.rect(x0 + size * j, y0 + size * i, size, size);
                }
            }

            context.noFill();
            context.stroke(context.color(255, 60));
            context.strokeWeight(1);
            context.rect(x0, y0, w - 1, h - 1);

            if (hoveredBoxInRange()) {
                context.noFill();
                context.strokeWeight(1);
                context.stroke(255, 255, 255);
                context.rect(x0 + size * hoveredBoxCol - 1, y0 + size * hoveredBoxRow - 1, size + 1, size + 1);
            }

        } else {
            context.noFill();
            context.stroke(255, 255, 255);
            context.strokeWeight(1);
            context.rect(0, 0, getWidth() - 1, getHeight() - 1);
        }
    }

    @Override
    public void onMouseHovered(int x1, int y1, int x2, int y2) {
        calcHoveredBox(x2, y2);
    }

    @Override
    public void onMouseDragged(int x1, int y1, int x2, int y2) {
        int dy = y2 - y1;
        changeHovered(-dy * 0.1f);
    }

    @Override
    public void onMouseReleased(int x, int y, MouseButton button) {
        calcHoveredBox(x, y);
    }

    @Override
    public void onMouseWheelMoved(int delta) {
        changeHovered(-delta * 0.2f);
    }

    @Override
    protected void onMouseClicked(int x, int y, MouseButton button) {
        if (button == MouseButton.RIGHT) {
            if (hoveredBoxInRange()) {
                if (hoveredBoxCol == 0) {
                    removeType(hoveredBoxRow);
                } else if (hoveredBoxRow == 0) {
                    removeType(hoveredBoxCol);
                }
            }
        }
    }

    private void calcHoveredBox(int x, int y) {
        int row = getBoxRow(y);
        int col = getBoxCol(x);
        if (row != hoveredBoxRow || col != hoveredBoxCol) {
            hoveredBoxRow = row;
            hoveredBoxCol = col;
            requestRender();
        }
    }

    private void removeType(int index) {
        removeTypeListeners.forEach(listener -> listener.onTypeRemoved(index));
    }

    private void changeHovered(float delta) {
        if (hoveredBoxInRange()) {
            float newValue = matrix.get(hoveredBoxRow, hoveredBoxCol) + delta;
            if (newValue < -1) {
                newValue = -1;
            } else if (newValue > 1) {
                newValue = 1;
            }
            for (MatrixChangeListener listener : matrixChangeListeners) {
                listener.onMatrixChanged(hoveredBoxRow, hoveredBoxCol, newValue);
            }
        }
    }

    private boolean hoveredBoxInRange() {
        int n = matrix.size();
        return hoveredBoxCol >= 0 && hoveredBoxCol < n && hoveredBoxRow >= 0 && hoveredBoxRow < n;
    }

    private int getBoxRow(int mouseY) {
        int n = matrix.size();
        int size = Math.min(getWidth(), getHeight()) / n;
        int h = n * size;
        int y0 = (getHeight() - h) / 2;
        return Math.floorDiv(mouseY - y0, size);
    }

    private int getBoxCol(int mouseX) {
        int n = matrix.size();
        int size = Math.min(getWidth(), getHeight()) / n;
        int w = n * size;
        int x0 = (getWidth() - w) / 2;
        return Math.floorDiv(mouseX - x0, size);
    }

    public interface MatrixChangeListener {
        void onMatrixChanged(int i, int j, float value);
    }

    public interface RemoveTypeListener {
        void onTypeRemoved(int index);
    }
}
