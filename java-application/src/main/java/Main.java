import engine.InfoDisplay;
import engine.Renderer;
import gui.colormaker.RainbowColorMaker;
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

    private InfoDisplay infoDisplay = new InfoDisplay();

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

        renderer = new Renderer(width, height, new RainbowColorMaker(g));
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

            this.infoDisplay.begin();
            this.infoDisplay.text("n", renderer.getParticleCount(), 5, 0);
            this.infoDisplay.text("fps", frameRate, 3, 0);
            this.infoDisplay.set(4, 1, "ms");
            this.infoDisplay.text("update", updateTimeAvg);
            this.infoDisplay.text("draw", drawTimeAvg);
            this.infoDisplay.text("total", updateTimeAvg + drawTimeAvg);
            this.infoDisplay.end(getGraphics(), 0, 0);
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
