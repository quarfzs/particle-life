package guilib.widgets;

import guilib.constants.MouseButton;
import processing.core.PGraphics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Selector extends Widget {

    public interface SelectionChangeListener {
        void onChanged(int index, String entry);
    }

    private final Label titleLabel;
    private final Label entryLabel;
    private final Button button1;
    private final Button button2;
    private final ArrayList<String> entries;
    private int selectedIndex = 0;
    private final List<Widget> children;

    private final ArrayList<SelectionChangeListener> selectionChangeListeners = new ArrayList<>();

    public Selector(String title, String[] entries) {
        titleLabel = new Label(title);
        this.entries = new ArrayList<>(Arrays.asList(entries));
        entryLabel = new Label(getSelectedEntry());
        button1 = new Button("-");
        button2 = new Button("+");

        entryLabel.addOnMouseWheelListener(this::onMouseWheelMoved);
        button1.setOnClickListener(() -> {
            setSelectedIndex(selectedIndex - 1);
            notifySelectionChangeListeners();
        });
        button2.setOnClickListener(() -> {
            setSelectedIndex(selectedIndex + 1);
            notifySelectionChangeListeners();
        });

        children = List.of(titleLabel, entryLabel, button1, button2);
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

        // get pref. width of title label
        titleLabel.updateSize(0, 0, 0, -1);
        int prefWidth = titleLabel.getWidth();
        int actualWidth = Utility.constrainDimension(minWidth, prefWidth, maxWidth);

        // set & get actual heights of labels
        titleLabel.updateSize(actualWidth, 0, actualWidth, -1);
        entryLabel.updateSize(actualWidth, 0, actualWidth, -1);
        int prefHeight = titleLabel.getHeight() +
                Math.max(entryLabel.getHeight(), Math.max(button1.getHeight(), button2.getHeight()));

        setSize(actualWidth, Utility.constrainDimension(minHeight, prefHeight, maxHeight));

        int entryLabelWidth = getWidth() - (button1.getWidth() + button2.getWidth());

        int upperPartSize = titleLabel.getHeight();
        int lowerPartSize = getHeight() - upperPartSize;

        entryLabel.updateSize(entryLabelWidth, lowerPartSize, entryLabelWidth, lowerPartSize);

        button1.dy = upperPartSize + (lowerPartSize - button1.getHeight()) / 2;
        button2.dy = upperPartSize  + (lowerPartSize - button2.getHeight()) / 2;
        entryLabel.dy = upperPartSize + (lowerPartSize - entryLabel.getHeight()) / 2;
        button2.dx = getWidth() - button2.getWidth();
        entryLabel.dx = button1.getWidth();
    }

    @Override
    protected void render(PGraphics context) {
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
}
