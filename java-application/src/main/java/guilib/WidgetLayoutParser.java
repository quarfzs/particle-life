package guilib;

import guilib.widgets.*;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class WidgetLayoutParser {

    private static final WidgetLayoutParser instance = new WidgetLayoutParser();

    public static WidgetLayoutParser getInstance() {
        return instance;
    }

    public Map<String, Widget> parseRecursively(Node node) {
        HashMap<String, Widget> map = new HashMap<>();
        parseRecursivelyWithMapping(node, map);
        return map;
    }

    private Widget parseRecursivelyWithMapping(Node node, Map<String, Widget> map) {

        Widget widget = parseInternal(node, map);

        NamedNodeMap attributes = node.getAttributes();
        if (attributes != null) {
            Node id = attributes.getNamedItem("id");
            if (id != null) {
                map.put(id.getNodeValue(), widget);
            }
        }

        return widget;
    }

    private Widget parseInternal(Node node, Map<String, Widget> map) {
        switch (node.getNodeName()) {
            case "AppFrame":
                return parseAppFrame(node, map);
            case "Label":
                return parseLabel(node);
            case "Button":
                return parseButton(node);
            case "ScrollView":
                return parseScrollView(node, map);
            case "ListView":
                return parseListView(node, map);
            case "SliderBase":
                return parseSliderBase(node);
            case "FloatSlider":
                return parseFloatSlider(node);
            case "IntSlider":
                return parseIntSlider(node);
            case "TextInput":
                return parseTextInput(node);
            case "Toggle":
                return parseToggle(node);
            case "Container":
                return parseContainer(node);
            case "Selector":
                return parseSelector(node);
            default:
                System.out.printf("Warning: Could not parse unknown node \"%s\"%n", node.getNodeName());
                return null;
        }
    }

    private Selector parseSelector(Node node) {
        return new Selector(parseStringAttribute(node, "title", ""), getInnerText(node).split(","));
    }

    private Toggle parseToggle(Node node) {
        return new Toggle(getInnerText(node), parseBooleanAttribute(node, "state", false));
    }

    private Container parseContainer(Node node) {
        return new Container(
                parseIntegerAttribute(node, "width", 40),
                parseIntegerAttribute(node, "height", 40)
        );
    }

    private TextInput parseTextInput(Node node) {
        return new TextInput(getInnerText(node));
    }

    private Label parseLabel(Node node) {
        return new Label(getInnerText(node));
    }

    private FloatSlider parseFloatSlider(Node node) {
        return new FloatSlider(
                getInnerText(node),
                parseDoubleAttribute(node, "min", 0.0),
                parseDoubleAttribute(node, "max", 1.0),
                parseIntegerAttribute(node, "digits", 2)
        );
    }

    private IntSlider parseIntSlider(Node node) {
        return new IntSlider(
                getInnerText(node),
                parseIntegerAttribute(node, "min", 0),
                parseIntegerAttribute(node, "max", 1),
                parseIntegerAttribute(node, "step", 1)
        );
    }

    private String getInnerText(Node node) {
        return node.getFirstChild().getNodeValue().trim();
    }

    private Button parseButton(Node node) {
        return new Button(getInnerText(node));
    }

    private AppFrame parseAppFrame(Node node, Map<String, Widget> map) {

        Widget childInner = null;
        Widget childHorizontal = null;
        Widget childVertical = null;

        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            Node childNode = node.getChildNodes().item(i);
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {

                Widget content = parseRecursivelyWithMapping(getFirstChildElement(childNode), map);

                switch (childNode.getNodeName()) {
                    case "inner":
                        childInner = content;
                        break;
                    case "horizontal":
                        childHorizontal = content;
                        break;
                    case "vertical":
                        childVertical = content;
                        break;
                }
            }
        }

        return new AppFrame(childInner, childHorizontal, childVertical);
    }

    private ScrollView parseScrollView(Node node, Map<String, Widget> map) {

        return new ScrollView(
                parseOrientationAttribute(node, Orientation.VERTICAL),
                parseRecursivelyWithMapping(getFirstChildElement(node), map)
        );
    }

    private ListView parseListView(Node node, Map<String, Widget> map) {

        List<Widget> children = new ArrayList<>();

        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            Node childNode = node.getChildNodes().item(i);
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                Widget child = parseRecursivelyWithMapping(childNode, map);
                children.add(child);
            }
        }

        return new ListView(
                parseOrientationAttribute(node, Orientation.VERTICAL),
                parseAlignAttribute(node, Align.CENTER),
                children,
                parseIntegerAttribute(node, "margin", 0)
        );
    }

    private IntSlider parseSliderBase(Node node) {
        return new IntSlider(
                parseStringAttribute(node, "title", ""),
                parseIntegerAttribute(node, "min", 0),
                parseIntegerAttribute(node, "max", 100),
                parseIntegerAttribute(node, "step", 1)
        );
    }

    private Align parseAlignAttribute(Node node, Align defaultValue) {
        Node attribute = node.getAttributes().getNamedItem("align");
        if (attribute != null) {
            switch (attribute.getNodeValue().toLowerCase()) {
                case "start":
                    return Align.START;
                case "end":
                    return Align.END;
                case "center":
                    return Align.CENTER;
                default:
                    return defaultValue;
            }
        }
        return defaultValue;
    }

    private Orientation parseOrientationAttribute(Node node, Orientation defaultValue) {
        Node attribute = node.getAttributes().getNamedItem("orientation");
        if (attribute != null) {
            switch (attribute.getNodeValue().toLowerCase()) {
                case "horizontal":
                    return Orientation.HORIZONTAL;
                case "vertical":
                    return Orientation.VERTICAL;
                default:
                    return defaultValue;
            }
        }
        return defaultValue;
    }

    private int parseIntegerAttribute(Node node, String attributeName, int defaultValue) {
        Node attribute = node.getAttributes().getNamedItem(attributeName);
        if (attribute != null) {
            return Integer.parseInt(attribute.getNodeValue());
        }
        return defaultValue;
    }

    private double parseDoubleAttribute(Node node, String attributeName, double defaultValue) {
        Node attribute = node.getAttributes().getNamedItem(attributeName);
        if (attribute != null) {
            return Double.parseDouble(attribute.getNodeValue());
        }
        return defaultValue;
    }

    private boolean parseBooleanAttribute(Node node, String attributeName, boolean defaultValue) {
        Node attribute = node.getAttributes().getNamedItem(attributeName);
        if (attribute != null) {
            return Boolean.parseBoolean(attribute.getNodeValue());
        }
        return defaultValue;
    }

    private String parseStringAttribute(Node node, String attributeName, String defaultValue) {
        Node attribute = node.getAttributes().getNamedItem(attributeName);
        if (attribute != null) {
            return attribute.getNodeValue();
        }
        return defaultValue;
    }

    private Node getFirstChildElement(Node node) {
        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            Node childNode = node.getChildNodes().item(i);
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                return childNode;
            }
        }
        return null;
    }
}
