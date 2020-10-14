package guilib;

import guilib.constants.MouseButton;
import guilib.widgets.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import processing.core.PGraphics;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;

public class GraphicalInterface {

    private class WidgetAndPos {
        public Widget widget;
        public int x;
        public int y;

        public WidgetAndPos(Widget widget, int x, int y) {
            this.widget = widget;
            this.x = x;
            this.y = y;
        }
    }

    public final GraphicsProvider graphicsProvider;
    private final Widget rootWidget;
    private WidgetAndPos activeWidpos;
    private Map<String, Widget> widgetMap;

    public GraphicalInterface(String layoutFilePath, String rootWidgetId, GraphicsProvider graphicsProvider) {

        this.graphicsProvider = graphicsProvider;

        try {
            widgetMap = build(layoutFilePath);
        } catch (IOException | SAXException | ParserConfigurationException e) {
            throw new RuntimeException("Could not parse layout file", e);
        }

        rootWidget = widgetMap.get(rootWidgetId);

        setActiveWidpos(new WidgetAndPos(rootWidget, rootWidget.getDx(), rootWidget.getDy()));
    }

    private Map<String, Widget> build(String layoutFilePath) throws IOException, SAXException, ParserConfigurationException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(layoutFilePath);
        Document doc = dBuilder.parse(inputStream);

        Element root = doc.getDocumentElement();
        root.normalize();

        return WidgetLayoutParser.getInstance().parseRecursively(root);
    }

    public Map<String, Widget> getWidgetMap() {
        return widgetMap;
    }

    private void setActiveWidpos(WidgetAndPos widpos) {
        if (activeWidpos != null) {
            activeWidpos.widget.setActive(false);
        }
        if (widpos != null) {
            widpos.widget.setActive(true);
        }
        activeWidpos = widpos;
    }

    void render(PGraphics context, int left, int top, int width, int height) {

        rootWidget.updateSize(0, 0, width, height);

        context.beginDraw();
        context.image(renderWidgetRecursively(rootWidget), left, top, width, height);
        context.endDraw();
    }

    private PGraphics renderWidgetRecursively(Widget widget) {

        if (haveAnyRequestedRender(widget)) {

            PGraphics baseImage;
            if (widget.isRenderRequested()) {
                baseImage = widget.renderImage(graphicsProvider);
            } else {
                baseImage = widget.getImage();
            }

            Iterable<Widget> children = widget.getChildren();
            if (children != null) {
                baseImage.beginDraw();
                for (Widget child : children) {
                    baseImage.image(renderWidgetRecursively(child), child.getDx(), child.getDy());
                }
                baseImage.endDraw();
            }

            return baseImage;

        } else {
            return widget.getImage();
        }
    }

    private boolean haveAnyRequestedRender(Widget widget) {

        if (widget.isRenderRequested()) {
            return true;
        }

        Iterable<Widget> children = widget.getChildren();
        if (children != null) {
            for (Widget child : children) {
                if (haveAnyRequestedRender(child)) {
                    return true;
                }
            }
        }

        return false;
    }

    public void requestRenderForAll() {
        requestRenderRecursively(rootWidget);
    }

    public void requestRenderRecursively(Widget widget) {
        widget.requestRender();
        Iterable<Widget> children = widget.getChildren();
        if (children != null) children.forEach(this::requestRenderRecursively);
    }

    void mouseClicked(int x, int y, MouseButton button) {
        WidgetAndPos widpos = getWidgetAndPos(x, y);
        setActiveWidpos(widpos);
        if (widpos != null) {
            widpos.widget.mouseClicked(x - widpos.x, y - widpos.y, button);
        }
    }

    void mousePressed(int x, int y, MouseButton button) {
        WidgetAndPos widpos = getWidgetAndPos(x, y);
        setActiveWidpos(widpos);
        if (widpos != null) {
            widpos.widget.onMousePressed(x - widpos.x, y - widpos.y, button);
        }
    }

    void mouseReleased(int x, int y, MouseButton button) {
        if (activeWidpos != null) {
            activeWidpos.widget.onMouseReleased(x - activeWidpos.x, y - activeWidpos.y, button);
        }
    }

    void mouseHovered(int x1, int y1, int x2, int y2) {
        WidgetAndPos widpos = getWidgetAndPos(x1, y1);
        if (widpos != null) {
            widpos.widget.onMouseHovered(x1 - widpos.x, y1 - widpos.y, x2 - widpos.x, y2 - widpos.y);
        }

        WidgetAndPos widpos2 = getWidgetAndPos(x2, y2);

        if (widpos2 != null && widpos2 != widpos) {
            widpos2.widget.onMouseHovered(x1 - widpos2.x, y1 - widpos2.y, x2 - widpos2.x, y2 - widpos2.y);
        }
    }

    void mouseDragged(int x1, int y1, int x2, int y2) {
        if (activeWidpos != null) {
            activeWidpos.widget.onMouseDragged(x1 - activeWidpos.x, y1 - activeWidpos.y, x2 - activeWidpos.x, y2 - activeWidpos.y);
        }
    }

    void mouseWheelMoved(int delta, int mouseX, int mouseY) {
        WidgetAndPos widpos = getWidgetAndPos(mouseX, mouseY);
        if (widpos != null) {
            widpos.widget.mouseWheelMoved(delta);
        }
    }

    void keyPressed(int keyCode, char key) {
        if (activeWidpos != null) {
            activeWidpos.widget.onKeyPressed(keyCode, key);
        }
    }

    void keyReleased(int keyCode, char key) {
        if (activeWidpos != null) {
            activeWidpos.widget.onKeyReleased(keyCode, key);
        }
    }

    private Widget getWidget(int x, int y) {
        return getWidget(rootWidget, x - rootWidget.getDx(), y - rootWidget.getDy());
    }

    /**
     * Returns null if the position is outside the widget's rect,
     * returns the widget if it's not any of the children,
     * otherwise returns a child (recursively).
     */
    private Widget getWidget(Widget widget, int dx, int dy) {

        if (rectIntersect(dx, dy, 0, 0, widget.getWidth(), widget.getHeight())) {

            Iterable<Widget> children = widget.getChildren();
            if (children != null) {
                for (Widget child: children) {
                    Widget w = getWidget(child, dx - child.getDx(), dy - child.getDy());
                    if (w != null) {
                        return w;
                    }
                }
            }

            return widget;
        }

        return null;
    }

    private WidgetAndPos getWidgetAndPos(int x, int y) {
        return getWidgetAndPos(new WidgetAndPos(rootWidget, rootWidget.getDx(), rootWidget.getDy()), x, y);
    }

    private WidgetAndPos getWidgetAndPos(WidgetAndPos widget, int x, int y) {

        if (rectIntersect(x, y, widget.x, widget.y, widget.widget.getWidth(), widget.widget.getHeight())) {

            Iterable<Widget> children = widget.widget.getChildren();
            if (children != null) {
                for (Widget child: children) {
                    WidgetAndPos w = getWidgetAndPos(new WidgetAndPos(child, widget.x + child.getDx(), widget.y + child.getDy()), x, y);
                    if (w != null) {
                        return w;
                    }
                }
            }

            return widget;
        }

        return null;
    }

    private boolean rectIntersect(int x, int y, int left, int top, int width, int height) {
        return x > left && x < left + width && y > top && y < top + height;
    }
}
