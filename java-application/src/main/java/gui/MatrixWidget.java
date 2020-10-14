package gui;

import engine.colormaker.ColorMaker;
import engine.colormaker.RainbowColorMaker;
import guilib.Theme;
import guilib.constants.MouseButton;
import guilib.widgets.Utility;
import guilib.widgets.Widget;
import logic.Matrix;
import processing.core.PConstants;
import processing.core.PGraphics;

import java.util.ArrayList;

public class MatrixWidget extends Widget {

    private Matrix matrix = null;
    private int hoveredBoxRow = -1;
    private int hoveredBoxCol = -1;
    private int lastMouseX = -1;
    private int lastMouseY = -1;

    private ArrayList<MatrixChangeListener> matrixChangeListeners = new ArrayList<>();
    private ArrayList<RemoveTypeListener> removeTypeListeners = new ArrayList<>();

    public MatrixWidget() {
    }

    public void matrixChanged(Matrix matrix) {
        this.matrix = matrix;
        calcHoveredBox(lastMouseX, lastMouseY);
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
        int prefWidth = 100;
        int prefHeight = 100;
        int s = Math.min(
                Utility.constrainDimension(minWidth, prefWidth, maxWidth),
                Utility.constrainDimension(minHeight, prefHeight, maxHeight)
        );
        setSize(s, s);
    }

    @Override
    protected void render(PGraphics context) {
        clear(context);

        if (matrix != null) {

            context.noStroke();

            int n = matrix.size() + 1;  // 1 extra space for colors
            int size = Math.min(getWidth(), getHeight()) / n;

            int w = n * size;
            int h = n * size;
            int x0 = (getWidth() - w) / 2;
            int y0 = (getHeight() - h) / 2;

            int[] typeColors = new int[matrix.size()];
            ColorMaker colorMaker = new RainbowColorMaker(context);
            for (int i = 0; i < typeColors.length; i++) {
                typeColors[i] = colorMaker.getColor(i / (float) typeColors.length);
            }

            context.ellipseMode(PConstants.CORNER);
            for (int i = 1; i < n; i++) {
                context.fill(typeColors[i - 1]);
                context.ellipse(x0 + size * 0, y0 + size * i, size, size);
            }
            for (int j = 1; j < n; j++) {
                context.fill(typeColors[j - 1]);
                context.ellipse(x0 + size * j, y0 + size * 0, size, size);
            }

            for (int i = 1; i < n; i++) {
                for (int j = 1; j < n; j++) {
                    float value = matrix.get(i - 1, j - 1);
                    if (value >= 0) {
                        context.fill(context.color(0, 255 * value, 0));
                    } else {
                        context.fill(context.color(255 * Math.abs(value), 0, 0));
                    }
                    context.rect(x0 + size * j, y0 + size * i, size, size);
                }
            }

            Utility.drawShadowOutline(context, x0 + size, y0 + size, w - size - 1, h - size - 1);

            if (hoveredBoxInRange(0, matrix.size() + 1)) {
                context.noFill();
                context.strokeWeight(1);
                if (hoveredBoxInRange(1, matrix.size() + 1)) {
                    context.stroke(255);
                    context.rect(x0 + size * hoveredBoxCol - 1, y0 + size * hoveredBoxRow - 1, size + 1, size + 1);

                    // draw value text in hovered box
                    float val = matrix.get(hoveredBoxRow - 1, hoveredBoxCol - 1);
                    context.textSize(8);

                    // choose best contrast
                    if (val <= 0.7 && val >= -0.8) {
                        context.fill(255);
                    } else {
                        context.fill(0);
                    }

                    context.noStroke();
                    context.textAlign(PConstants.CENTER, PConstants.CENTER);
                    context.text(String.format("%.0f", Math.abs(val) * 10), x0 + size * hoveredBoxCol + size/2, y0 + size * hoveredBoxRow + size/2);

                } else if (hoveredBoxCol != 0 || hoveredBoxRow != 0) {
                    context.stroke(Theme.getTheme().backgroundContrast);
                    context.ellipse(x0 + size * hoveredBoxCol - 1, y0 + size * hoveredBoxRow - 1, size + 1, size + 1);
                }
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
        lastMouseX = x2;
        lastMouseY = y2;
        calcHoveredBox(x2, y2);
    }

    @Override
    public void onMouseDragged(int x1, int y1, int x2, int y2) {
        int dy = y2 - y1;
        changeHovered(-dy * 0.1f);
    }

    @Override
    public void onMouseReleased(int x, int y, MouseButton button) {
        lastMouseX = x;
        lastMouseY = y;
        calcHoveredBox(x, y);
    }

    @Override
    public void onMouseWheelMoved(int delta) {
        changeHovered(-delta * 0.2f);
    }

    @Override
    protected void onMouseClicked(int x, int y, MouseButton button) {
        if (button == MouseButton.RIGHT) {
            // remove clicked type
            if (hoveredBoxCol == 0) {
                if (hoveredBoxRow >= 1 && hoveredBoxRow < matrix.size() + 1) {
                    removeType(hoveredBoxRow - 1);
                }
            } else if (hoveredBoxRow == 0) {
                if (hoveredBoxCol >= 1 && hoveredBoxCol < matrix.size() + 1) {
                    removeType(hoveredBoxCol - 1);
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
        if (hoveredBoxInRange(1, matrix.size() + 1)) {

            float newValue = matrix.get(hoveredBoxRow - 1, hoveredBoxCol - 1) + delta;

            // constrain to [-1, 1]
            if (newValue < -1) {
                newValue = -1;
            } else if (newValue > 1) {
                newValue = 1;
            }

            for (MatrixChangeListener listener : matrixChangeListeners) {
                listener.onMatrixChanged(hoveredBoxRow - 1, hoveredBoxCol - 1, newValue);
            }
        }
    }

    private boolean hoveredBoxInRange(int start, int stop) {
        return hoveredBoxCol >= start && hoveredBoxCol < stop && hoveredBoxRow >= start && hoveredBoxRow < stop;
    }

    private int getBoxRow(int mouseY) {
        int n = matrix.size() + 1;
        int size = Math.min(getWidth(), getHeight()) / n;
        int h = n * size;
        int y0 = (getHeight() - h) / 2;
        return Math.floorDiv(mouseY - y0, size);
    }

    private int getBoxCol(int mouseX) {
        int n = matrix.size() + 1;
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
