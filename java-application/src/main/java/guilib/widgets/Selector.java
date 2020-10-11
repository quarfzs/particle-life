package guilib.widgets;

import guilib.constants.MouseButton;
import processing.core.PGraphics;

import java.util.ArrayList;
import java.util.Arrays;

public class Selector extends Widget {

    public interface SelectionChangeListener {
        void onChanged(int index, String entry);
    }

    private final Label titleLabel;
    private final Label entryLabel;
    private final ArrayList<String> entries;
    private int selectedIndex = 0;
    private int hoveredButton = 0;
    private final ArrayList<Widget> children = new ArrayList<>();

    private final ArrayList<SelectionChangeListener> selectionChangeListeners = new ArrayList<>();

    public Selector(String title, String[] entries) {
        titleLabel = new Label(title);
        this.entries = new ArrayList<>(Arrays.asList(entries));
        entryLabel = new Label(getSelectedEntry());

        entryLabel.addOnMouseWheelListener(this::onMouseWheelMoved);

        children.add(titleLabel);
        children.add(entryLabel);
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public String getSelectedEntry() {
        if (entries.isEmpty()) {
            return "";
        } else {
            return entries.get(selectedIndex);
        }
    }

    public void setSelectedIndex(int index) {
        index = Math.floorMod(index, entries.size());
        if (index != selectedIndex) {
            selectedIndex = index;
            entryLabel.setText(getSelectedEntry());
            requestRender();
        }
    }

    public void addSelectionChangeListener(SelectionChangeListener listener) {
        selectionChangeListeners.add(listener);
    }

    private void notifySelectionChangeListeners() {
        selectionChangeListeners.forEach(listener -> listener.onChanged(getSelectedIndex(), getSelectedEntry()));
    }

    @Override
    public void updateSize(int minWidth, int minHeight, int maxWidth, int maxHeight) {
        int prefWidth = 100;//todo
        int prefHeight = 48;//todo
        setSize(Utility.constrainDimension(minWidth, prefWidth, maxWidth), Utility.constrainDimension(minHeight, prefHeight, maxHeight));

        titleLabel.updateSize(getWidth(), 0, getWidth(), -1);

        int boxSize = getHeight() - titleLabel.getHeight();
        int entryLabelWidth = getWidth() - boxSize;
        entryLabel.updateSize(entryLabelWidth, boxSize, entryLabelWidth, boxSize);
        entryLabel.dx = boxSize;
        entryLabel.dy = getHeight() - boxSize;
    }

    @Override
    protected void render(PGraphics context) {
        clear(context);

        int h = entryLabel.getHeight();
        int w = h;

        context.fill(context.color(80));
        context.noStroke();
        if (hoveredButton == 1) {
            context.rect(0, getHeight() - h, w, h/2);
        } else if (hoveredButton == 2) {
            context.rect(0, getHeight() - h/2, w, h/2);
        }

        context.fill(context.color(180));
        context.stroke(context.color(255));
        context.noStroke();

        int p = 2;

        context.triangle(
                p, getHeight() - h/2 - p,
                w/2, getHeight() - h + p,
                w - p, getHeight() - h/2 - p
        );

        context.triangle(
                p, getHeight() - h/2 + p,
                w/2, getHeight() - p,
                w - p, getHeight() - h/2 + p
        );
    }

    @Override
    public Iterable<Widget> getChildren() {
        return children;
    }

    @Override
    public void onMouseWheelMoved(int delta) {
        selectedIndex = Math.floorMod(selectedIndex + delta, entries.size());
        entryLabel.setText(getSelectedEntry());
        requestRender();
        notifySelectionChangeListeners();
    }

    @Override
    public void onMousePressed(int x, int y, MouseButton button) {
        if (hoveredButton == 1) {
            setSelectedIndex(selectedIndex - 1);
            notifySelectionChangeListeners();
        } else if (hoveredButton == 2) {
            setSelectedIndex(selectedIndex + 1);
            notifySelectionChangeListeners();
        }
    }

    @Override
    public void onMouseHovered(int x1, int y1, int x2, int y2) {
        handleMousePos(x2, y2);
    }

    @Override
    public void onMouseDragged(int x1, int y1, int x2, int y2) {
        handleMousePos(x2, y2);
    }

    private void handleMousePos(int x, int y) {
        int b = getHoveredButton(x, y);
        if (b != hoveredButton) {
            hoveredButton = b;
            requestRender();
        }
    }

    private int getHoveredButton(int x, int y) {
        int h = entryLabel.getHeight();
        int w = h;

        if (x >= 0 && x < w) {
            if (y >= getHeight() - h && y < getHeight() - h/2) {
                return 1;
            } else if (y >= getHeight() - h/2 && y < getHeight()) {
                return 2;
            }
        }
        return 0;
    }
}
