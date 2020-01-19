import frontend.ColorMaker;
import frontend.Helper;
import frontend.Renderer;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PImage;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

import javax.swing.*;
import java.io.File;

public class Main extends PApplet {

    private static final boolean START_IN_FULLSCREEN = true;

    // track FPS
    private float[] lastUpdateTimes = new float[50];
    private int lastUpdateTimeIndex = 0;
    private float[] lastDrawTimes = new float[50];
    private int lastDrawTimeIndex = 0;

    private Renderer renderer;

    public static void main(String[] args) {
        PApplet.main(Main.class.getName());
    }

    @Override
    public void settings() {
        if (START_IN_FULLSCREEN) {
            fullScreen();
        } else {
            size(1280, 960);
        }
        noSmooth();
    }

    @Override
    public void setup() {

        background(0);

        PFont mono = createFont("Consolas", 18, false);
        textFont(mono);

        ColorMaker subtractiveColorMaker = new ColorMaker() {

            private float[][] rainbow = new float[][]{
                    new float[]{255, 0, 0},//red
                    new float[]{255, 127, 0},//orange
                    new float[]{255, 255, 0},//yellow
                    new float[]{0, 255, 0},//green
                    new float[]{0, 0, 255},//blue
                    new float[]{75, 0, 130},//indigo
                    new float[]{148, 0, 211},//violet
            };

            @Override
            public int rgb(float r, float g, float b) {
                pushStyle();
                colorMode(RGB, 255, 255, 255);
                int c = color(r, g, b);
                popStyle();
                return c;
            }

            @Override
            public int hsb(float h, float s, float b) {
                h *= rainbow.length;
                int j1 = floor(h);
                int j2 = ceil(h);

                int i1 = Helper.modulo(j1, rainbow.length);
                int i2 = Helper.modulo(j2, rainbow.length);

                pushStyle();
                colorMode(RGB, 255);

                int c;
                if (i1 == i2) {
                    c = color(rainbow[i1][0], rainbow[i1][1], rainbow[i1][2]);

                } else {

                    c = lerpColor(
                            color(rainbow[i1][0], rainbow[i1][1], rainbow[i1][2]),
                            color(rainbow[i2][0], rainbow[i2][1], rainbow[i2][2]),
                            (h - j1) / (j2 - j1));
                }

                popStyle();
                return c;
            }
        };

        renderer = new Renderer(width, height, subtractiveColorMaker);
    }

    @Override
    public void stop() {
        renderer.stop();
    }

    @Override
    public void keyReleased(KeyEvent event) {
        renderer.keyReleased(event.getKey());
    }

    @Override
    public void mousePressed(MouseEvent event) {
        renderer.mousePressed(translateMouseButton(event.getButton()));
    }

    @Override
    public void mouseWheel(MouseEvent event) {
        renderer.mouseScrolled(Math.signum(event.getCount()) * event.getY());
    }

    @Override
    public void mouseReleased(MouseEvent event) {
        renderer.mouseReleased(translateMouseButton(event.getButton()));
    }

    private int translateMouseButton(int button) {
        switch (button) {
            case 37:
                return 0;
            case 3:
                return 1;
            case 39:
                return 2;
        }
        return -1;
    }

    @Override
    public void mouseMoved(MouseEvent event) {
        renderer.mouseMoved(event.getX(), event.getY());
    }

    @Override
    public void mouseDragged(MouseEvent event) {
        renderer.mouseMoved(event.getX(), event.getY());
    }

    @Override
    public void draw() {

        if (renderer.isScreenshotRequested()) {
            background(0);
            renderer.drawParticles(g);
            saveScreenshot(g.copy());
        }

        long t1 = System.nanoTime();

        renderer.update(1f / frameRate);

        long t2 = System.nanoTime();

        // rendering starts here
        renderer.updateUI(1f / frameRate);
        background(0);
        renderer.draw(g);
        // rendering ends here

        long t3 = System.nanoTime();

        float timeUpdate = (t2 - t1) / 1000000.f;
        float timeDraw = (t3 - t2) / 1000000.f;

        if (renderer.shouldDrawRenderingStats()) {

            lastUpdateTimes[lastUpdateTimeIndex] = timeUpdate;
            lastUpdateTimeIndex = (lastUpdateTimeIndex+=1) % lastUpdateTimes.length;
            float updateTimeAvg = 0;
            for (float t : lastUpdateTimes) {
                updateTimeAvg += t;
            }
            updateTimeAvg /= lastUpdateTimes.length;

            lastDrawTimes[lastDrawTimeIndex] = timeDraw;
            lastDrawTimeIndex = (lastDrawTimeIndex+=1) % lastDrawTimes.length;
            float drawTimeAvg = 0;
            for (float t : lastDrawTimes) {
                drawTimeAvg += t;
            }
            drawTimeAvg /= lastDrawTimes.length;

            text(String.format("    fps: %3.0f", frameRate), 20, 20);
            text(String.format(" update: %4.1f ms", updateTimeAvg), 20, 40);
            text(String.format("   draw: %4.1f ms", drawTimeAvg), 20, 60);
        }
    }

    private void saveScreenshot(PImage image) {
        JFrame frame = new JFrame("Save Screenshot");
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            image.save(file.getAbsolutePath());
        }
    }
}
