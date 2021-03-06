package guilib.widgets;

import guilib.Theme;
import processing.core.PConstants;
import processing.core.PGraphics;

import java.util.Arrays;

public class Label extends Widget {

    private String text;
    private int padding = 3;

    public Label(String text) {
        this.text = text;
    }

    public void setText(String text) {
        this.text = text;
        requestRender();
    }

    @Override
    public void updateSize(int minWidth, int minHeight, int maxWidth, int maxHeight) {

        // account for multiline texts
        String[] lines = text.split("\n");
        int nLines = lines.length;
        int maxLineLength = Arrays.stream(lines).mapToInt(String::length).max().getAsInt();

        setSize(
                Utility.constrainDimension(minWidth, 12 * maxLineLength, maxWidth),
                Utility.constrainDimension(minHeight, nLines * 18, maxHeight)
        );
    }

    @Override
    protected void render(PGraphics context) {

        clear(context);

        context.noFill();
        context.noStroke();
        context.fill(Theme.getTheme().backgroundContrast);
        context.textAlign(PConstants.LEFT, PConstants.CENTER);
        context.text(text, padding, height / 2 - 1);

        context.noFill();
        context.stroke(Theme.getTheme().background);
        context.strokeWeight(padding);
        context.rect(0, 0, width, height);
    }
}
