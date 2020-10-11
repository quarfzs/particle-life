package engine;

import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PGraphics;

import java.awt.*;
import java.util.ArrayList;

public class InfoDisplay {

    private final ArrayList<String> names = new ArrayList<>();
    private final ArrayList<Object> values = new ArrayList<>();
    private final ArrayList<Integer> nDigits = new ArrayList<>();
    private final ArrayList<Integer> nDecimal = new ArrayList<>();
    private final ArrayList<String> units = new ArrayList<>();

    private final int fontSize;
    private final PFont font;

    private int nDigitsDefault;
    private int nDecimalDefault;
    private String unitDefault;

    public InfoDisplay() {
        this(14, false, false);
    }

    public InfoDisplay(int fontSize, boolean bold, boolean smooth) {
        this.fontSize = fontSize;
        int style = bold ? Font.BOLD : Font.PLAIN;
        this.font = new PFont(new Font("Consolas", style, this.fontSize), smooth);
    }

    public void begin() {
        names.clear();
        values.clear();
        nDigits.clear();
        nDecimal.clear();
        units.clear();
        reset();
    }

    public void reset() {
        set(3, 1, "");
    }

    public void set(int nDigits, int nDecimal, String unit) {
        nDigitsDefault = nDigits;
        nDecimalDefault = nDecimal;
        unitDefault = unit;
    }

    public void text(String name, float value) {
        text(name, value, nDigitsDefault, nDecimalDefault, unitDefault);
    }

    public void text(String name, float value, int nDigits, int nDecimal) {
        text(name, value, nDigits, nDecimal, "");
    }

    public void text(String name, float value, int nDigits, int nDecimal, String unit) {
        this.names.add(name);
        this.values.add(value);
        this.nDigits.add(nDigits);
        this.nDecimal.add(nDecimal);
        this.units.add(unit);
    }

    public void end(PGraphics context, float x, float y) {

        context.pushStyle();

        context.textFont(font);
        context.textAlign(PConstants.LEFT, PConstants.TOP);

        // find max name length
        int maxLength = 0;
        for (String name : names) {
            maxLength = Math.max(maxLength, name.length());
        }

        for (int i = 0; i < names.size(); i++) {

            String format = String.format("%%%ds: %%%d.%df",
                maxLength,
                nDigits.get(i),
                nDecimal.get(i)
            );

            String text = String.format(format,
                names.get(i),
                values.get(i)
            );

            String unit = units.get(i);
            if (!unit.isEmpty()) {
                text += " " + unit;
            }

            context.text(text, x, y);

            y += fontSize;
        }

        context.popStyle();
    }
}
