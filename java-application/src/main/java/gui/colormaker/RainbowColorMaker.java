package gui.colormaker;

import engine.Helper;
import processing.core.PGraphics;

public class RainbowColorMaker extends ColorMaker {

    private float[][] rainbow = new float[][]{
            new float[]{255, 0, 0},//red
            new float[]{255, 127, 0},//orange
            new float[]{255, 255, 0},//yellow
            new float[]{0, 255, 0},//green
            new float[]{0, 0, 255},//blue
            new float[]{75, 0, 130},//indigo
            new float[]{148, 0, 211},//violet
    };

    public RainbowColorMaker(PGraphics context) {
        super(context);
    }

    @Override
    public int getColor(float hue) {
        hue *= rainbow.length;
        int j1 = (int) Math.floor(hue);
        int j2 = (int) Math.ceil(hue);

        int i1 = Helper.modulo(j1, rainbow.length);
        int i2 = Helper.modulo(j2, rainbow.length);

        int c;
        if (i1 == i2) {
            c = rgb(rainbow[i1][0], rainbow[i1][1], rainbow[i1][2]);

        } else {

            c = lerpColor(
                    rgb(rainbow[i1][0], rainbow[i1][1], rainbow[i1][2]),
                    rgb(rainbow[i2][0], rainbow[i2][1], rainbow[i2][2]),
                    (hue - j1) / (j2 - j1));
        }

        return c;
    }
}
