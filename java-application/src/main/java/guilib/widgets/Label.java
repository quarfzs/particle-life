package guilib.widgets;

import guilib.Theme;
import processing.core.PConstants;
import processing.core.PGraphics;

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
        setSize(Utility.constrainDimension(minWidth, 8 * text.length() + 2 * 3, maxWidth), Utility.constrainDimension(minHeight, 16, maxHeight));
    }

    @Override
    protected void render(PGraphics context) {

        clear(context);

        context.noFill();
        context.noStroke();
        context.fill(Theme.getInstance().backgroundContrast);
        context.textAlign(PConstants.LEFT, PConstants.CENTER);
        context.text(text, padding, height / 2);

        context.noFill();
        context.stroke(Theme.getInstance().background);
        context.strokeWeight(padding);
        context.rect(0, 0, width, height);
    }
}
