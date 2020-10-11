package guilib;

import guilib.constants.MouseButton;
import guilib.widgets.Utility;
import guilib.widgets.Widget;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

import java.util.Map;

public class GraphicalInterfaceWrapper extends PApplet implements GraphicsProvider, Theme.ColorProvider {

    public interface OnInitListener {
        void init(GraphicalInterface g);
    }

    public static GraphicalInterfaceWrapper instance;
    private static GuiState staticState = new GuiState();
    private static OnInitListener onInitListener = null;

    public static GraphicalInterfaceWrapper getInstance() {
        return instance;
    }

    public static void open(String layoutFilePath, String rootWidgetId, OnInitListener onInitListener) {
        GraphicalInterfaceWrapper.onInitListener = onInitListener;
        PApplet.main(GraphicalInterfaceWrapper.class, layoutFilePath, rootWidgetId, staticState.toString());
    }

    private GuiState state;
    private GraphicalInterface graphicalInterface;

    public Map<String, Widget> getWidgetMap() {
        return graphicalInterface.getWidgetMap();
    }

    @Override
    public void settings() {

        state = GuiState.fromString(args[1]);

        String renderer = JAVA2D;

        if (state.fullScreen) {
            fullScreen(renderer);
        } else {
            size(state.width, state.height, renderer);
        }
    }

    @Override
    public void setup() {
        Theme.init(this);
        Utility.setColorContext(getGraphics());

        initGraphicalInterface();
    }

    private void initGraphicalInterface() {

        graphicalInterface = new GraphicalInterface(
                args[0], // layoutFilePath
                args[1], // rootWidgetId
                this);

        instance = this;
        if (onInitListener != null) {
            onInitListener.init(graphicalInterface);
            onInitListener = null;
        }
    }

    @Override
    public void draw() {
        PGraphics context = getGraphics();
        graphicalInterface.render(context, 0, 0, width, height);
    }

    private MouseButton mouseButton(MouseEvent event) {
        return switch (event.getButton()) {
            case 37 -> MouseButton.LEFT;
            case 3 -> MouseButton.WHEEL;
            case 39 -> MouseButton.RIGHT;
            default -> MouseButton.LEFT;
        };
    }

    @Override
    public void mouseClicked(MouseEvent event) {
        graphicalInterface.mouseClicked(mouseX, mouseY, mouseButton(event));
    }

    @Override
    public void mousePressed(MouseEvent event) {
        graphicalInterface.mousePressed(mouseX, mouseY, mouseButton(event));
    }

    @Override
    public void mouseReleased(MouseEvent event) {
        graphicalInterface.mouseReleased(mouseX, mouseY, mouseButton(event));
    }

    @Override
    public void mouseMoved() {
        graphicalInterface.mouseHovered(pmouseX, pmouseY, mouseX, mouseY);
    }

    @Override
    public void mouseDragged() {
        graphicalInterface.mouseDragged(pmouseX, pmouseY, mouseX, mouseY);
    }

    @Override
    public void mouseWheel(processing.event.MouseEvent event) {
        graphicalInterface.mouseWheelMoved(event.getCount(), mouseX, mouseY);
    }

    @Override
    public void keyPressed(KeyEvent event) {
        graphicalInterface.keyPressed(event.getKeyCode(), event.getKey());
    }

    @Override
    public void keyReleased(KeyEvent event) {
        graphicalInterface.keyReleased(event.getKeyCode(), event.getKey());
    }
}
