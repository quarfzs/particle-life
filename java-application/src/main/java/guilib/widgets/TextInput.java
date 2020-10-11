package guilib.widgets;

import guilib.Theme;
import processing.core.PConstants;
import processing.core.PGraphics;

import static processing.core.PConstants.*;

public class TextInput extends Widget {

    public interface ChangeListener {
        void onChange(String text);
    }

    public interface SubmitListener {
        void onSubmit(String text);
    }

    private String text;

    private int padding = 3;
    private int cursorPos;
    private ChangeListener changeListener = null;
    private SubmitListener submitListener = null;

    public TextInput(String text) {
        this.text = text;
        cursorPos = text.length();
    }

    @Override
    public void onKeyPressed(int keyCode, char key) {
        switch (keyCode) {
            case BACKSPACE:
            case DELETE:
                if (text.length() > 0) {
                    String beforeCursor = text.substring(0, cursorPos);
                    String afterCursor = text.substring(cursorPos);
                    if (keyCode == BACKSPACE && beforeCursor.length() > 0) {
                        String newText = beforeCursor.substring(0, beforeCursor.length() - 1) + afterCursor;
                        cursorPos--;
                        setTextInternal(newText);
                    } else if (keyCode == DELETE && afterCursor.length() > 0) {
                        String newText = beforeCursor + afterCursor.substring(1);
                        setTextInternal(newText);
                    }
                }
                break;
            case ENTER:
                onSubmit();
                break;
            case LEFT:
                moveCursorBy(-1);
                break;
            case RIGHT:
                moveCursorBy(1);
                break;
            case 36:
                moveCursorTo(0);
                break;
            case 35:
                moveCursorTo(text.length());
                break;
            default:
                if (isPrintableChar(key)) {
                    String beforeCursor = text.substring(0, cursorPos);
                    String afterCursor = text.substring(cursorPos);
                    String newText = beforeCursor + key + afterCursor;
                    cursorPos++;
                    setTextInternal(newText);
                } else {
                    System.out.println(keyCode);
                }
                break;
        }
    }

    private void moveCursorBy(int delta) {
        moveCursorTo(cursorPos + delta);
    }

    private void moveCursorTo(int pos) {
        int newCursorPos = Utility.constrain(0, pos, text.length());
        if (newCursorPos != cursorPos) {
            cursorPos = newCursorPos;
            requestRender();
        }
    }

    public boolean isPrintableChar(char c) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of( c );
        return (!Character.isISOControl(c)) && block != null && block != Character.UnicodeBlock.SPECIALS;
    }

    public void setText(String text) {
        if (!text.equals(this.text)) {
            this.text = text;
            requestRender();
        }
    }

    private void setTextInternal(String text) {
        if (!text.equals(this.text)) {
            this.text = text;
            requestRender();
            if (changeListener != null) {
                changeListener.onChange(text);
            }
        }
    }

    private void onSubmit() {
        if (submitListener != null) {
            submitListener.onSubmit(text);
        }
    }

    public void setChangeListener(ChangeListener listener) {
        changeListener = listener;
    }

    public void setSubmitListener(SubmitListener listener) {
        submitListener = listener;
    }

    @Override
    public void activeChanged() {
        requestRender();
    }

    @Override
    public void updateSize(int minWidth, int minHeight, int maxWidth, int maxHeight) {
        int prefWidth = text.length() * 10;
        int prefHeight = 15;
        setSize(Utility.constrainDimension(minWidth, prefWidth, maxWidth), Utility.constrainDimension(minHeight, prefHeight, maxHeight));
    }

    @Override
    protected void render(PGraphics context) {
        clear(context);

        // text
        context.noFill();
        context.noStroke();
        context.fill(0, 0, 0);
        context.textAlign(PConstants.LEFT, PConstants.CENTER);
        context.text(text, padding, height / 2);

        // cursor
        if (active) {
            String beforeCursor = text.substring(0, cursorPos);
            int cursorX = padding + (int) context.textWidth(beforeCursor);
            context.stroke(0, 0, 0);
            context.strokeWeight(1);
            context.line(cursorX, padding, cursorX, height - padding);
        }

        // frame (padding)
        context.noFill();
        context.stroke(255, 255, 255);
        context.strokeWeight(padding);
        context.rect(0, 0, width, height);

        // frame
        if (active) {
            context.noFill();
            context.stroke(Theme.getInstance().primary);
            context.strokeWeight(1);
            context.rect(0, 0, width - 1, height - 1);
        }
    }
}
