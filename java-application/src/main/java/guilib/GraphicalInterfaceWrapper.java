package guilib;

import guilib.constants.MouseButton;
import guilib.widgets.Utility;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

import static java.awt.event.KeyEvent.VK_F11;

public class GraphicalInterfaceWrapper extends PApplet implements GraphicsProvider, Theme.ColorProvider {

    private static String layoutFilePath;
    private static String rootWidgetId;
    private static App app = null;
    private static AppState lastAppState = null;

    public static void open(String layoutFilePath, String rootWidgetId, App app, boolean fullScreen) {
        GraphicalInterfaceWrapper.layoutFilePath = layoutFilePath;
        GraphicalInterfaceWrapper.rootWidgetId = rootWidgetId;
        GraphicalInterfaceWrapper.app = app;
        PApplet.main(GraphicalInterfaceWrapper.class, layoutFilePath, rootWidgetId, Boolean.toString(fullScreen));
    }

    private GraphicalInterface graphicalInterface;

    @Override
    public void settings() {

        String renderer = JAVA2D;
        boolean openInFullScreen = Boolean.parseBoolean(args[2]);

        if (openInFullScreen) {
            fullScreen(renderer);
        } else {
            size(1200, 800, renderer);
        }
    }

    @Override
    public void setup() {
        Theme.init(this);
        Utility.setColorContext(getGraphics());

        surface.setTitle(app.getTitle());

        String iconPath = app.getIconPath();
        if (iconPath != null) {
            surface.setIcon(loadImage(iconPath));
        }

        initGraphicalInterface();
    }

    private void initGraphicalInterface() {

        graphicalInterface = new GraphicalInterface(
                args[0], // layoutFilePath
                args[1], // rootWidgetId
                this);

        app.init(graphicalInterface, lastAppState);
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
        if (event.getKeyCode() == VK_F11) {

            lastAppState = app.createAppState();
            PApplet.main(GraphicalInterfaceWrapper.class, layoutFilePath, rootWidgetId, Boolean.toString(!sketchFullScreen()));

            // todo: this will still keep the window in memory and
            //  create an additional one every time the user toggles fullscreen.
            //  Find a better solution!
            surface.setVisible(false);
            noLoop();

        } else {
            graphicalInterface.keyPressed(event.getKeyCode(), event.getKey());
        }
    }

    @Override
    public void keyReleased(KeyEvent event) {
        graphicalInterface.keyReleased(event.getKeyCode(), event.getKey());
    }
}
