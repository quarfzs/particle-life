package gui;

import engine.Renderer;
import guilib.*;
import guilib.widgets.*;
import processing.core.PImage;
import engine.requests.*;

import javax.swing.*;
import java.io.File;
import java.util.Map;

public class Main implements App<MyAppState> {

    public static void main(String[] args) {
        GraphicalInterfaceWrapper.open("layout.xml", "root", new Main(), true);
    }

    private GraphicsProvider graphicsProvider;

    private CanvasWidget canvas;
    private MatrixWidget matrixWidget;
    private IntSlider typesSlider;
    private FloatSlider heatSlider;
    private FloatSlider densityPerCentSlider;
    private Button randomButton;
    private Button respawnButton;
    private Button randomAndRespawnButton;
    private Button randomTypesButton;
    private Button screenshotButton;
    private Toggle darkModeToggle;
    private Toggle togglePause;
    private Toggle keepDensityToggle;
    private Label statsLabel;
    private Selector initializerSelector;
    private Selector spawnSelector;
    private FloatSlider frictionSlider;
    private Toggle dtToggle;
    private Toggle wrapToggle;
    private FloatSlider dtSlider;
    private FloatSlider forceSlider;
    private FloatSlider rMinSlider;
    private FloatSlider rMaxSlider;
    private FloatSlider particleSizeSlider;
    private FloatSlider camZoomSlider;

    @Override
    public void init(GraphicalInterface g, MyAppState state) {

        graphicsProvider = g.graphicsProvider;

        Map<String, Widget> widgets = g.getWidgetMap();

        Container canvasContainer = (Container) widgets.get("canvas-container");
        canvas = new CanvasWidget();
        canvasContainer.setContent(canvas);
        canvas.onLoad(() -> {
            Renderer renderer = canvas.getRenderer();
            if (renderer != null) {

                if (state != null) {
                    Theme.setDarkMode(state.darkMode);
                    g.requestRenderForAll();

                    renderer.request(new RequestParticles(state.particles));
                    renderer.request(new RequestSettings(state.settings));
                    renderer.request(new RequestRendererSettings(state.rendererSettings));

                    renderer.handleRequests();  // handle requests before copying values to GUI!
                }

                // this should be called every time the Renderer instance changes:
                copyValues(renderer);
                attachListenersToRenderer(renderer);
                attachListenersToWidgets(g);

            } else {
                System.out.println("Warning: Canvas.getRenderer() returned null!");
            }
        });

        matrixWidget = new MatrixWidget();
        ((Container) widgets.get("matrix-container")).setContent(matrixWidget);

        togglePause = (Toggle) widgets.get("toggle-pause");
        keepDensityToggle = (Toggle) widgets.get("toggle-keep-density");
        statsLabel = (Label) widgets.get("stats-label");
        heatSlider = (FloatSlider) widgets.get("heat-slider");
        typesSlider = (IntSlider) widgets.get("types-slider");
        densityPerCentSlider = (FloatSlider) widgets.get("density-slider");
        randomButton = (Button) widgets.get("random-button");
        respawnButton = (Button) widgets.get("respawn-button");
        randomAndRespawnButton = (Button) widgets.get("random-and-respawn-button");
        randomTypesButton = (Button) widgets.get("random-types-button");
        screenshotButton = (Button) widgets.get("screenshot-button");
        darkModeToggle = (Toggle) widgets.get("dark-mode-toggle");
        initializerSelector = (Selector) widgets.get("initializer-selector");
        spawnSelector = (Selector) widgets.get("spawn-selector");
        frictionSlider = (FloatSlider) widgets.get("friction-slider");
        dtToggle = (Toggle) widgets.get("dt-toggle");
        dtSlider = (FloatSlider) widgets.get("dt-slider");
        forceSlider = (FloatSlider) widgets.get("force-slider");
        rMinSlider = (FloatSlider) widgets.get("rmin-slider");
        rMaxSlider = (FloatSlider) widgets.get("rmax-slider");
        wrapToggle = (Toggle) widgets.get("wrap-toggle");
        particleSizeSlider = (FloatSlider) widgets.get("particle-size-slider");
        camZoomSlider = (FloatSlider) widgets.get("cam-zoom-slider");
    }

    @Override
    public MyAppState createAppState() {

        Renderer renderer = canvas.getRenderer();

        return new MyAppState(
                renderer.getParticles(),
                renderer.getSettings().clone(),
                renderer.getRendererSettings(),
                darkModeToggle.getState()
        );
    }

    private void copyValues(Renderer renderer) {
        togglePause.setState(renderer.isPaused());
        matrixWidget.matrixChanged(renderer.getSettings().getMatrix());
        typesSlider.setValue(renderer.getSettings().getMatrix().size());
        densityPerCentSlider.setValue(renderer.getParticleDensity() * 100);
        heatSlider.setValue(renderer.getSettings().getHeat());
        initializerSelector.setSelectedIndex(renderer.getMatrixInitializerIndex());
        spawnSelector.setSelectedIndex(renderer.getSpawnMode());
        frictionSlider.setValue(renderer.getSettings().getFriction());
        dtToggle.setState(renderer.isFixedTimeStepEnabled());
        dtSlider.setValue(renderer.getFixedTimeStepValueMillis());
        forceSlider.setValue(renderer.getSettings().getForceFactor());
        rMinSlider.setValue(renderer.getSettings().getRMin());
        rMaxSlider.setValue(renderer.getSettings().getRMax());
        wrapToggle.setState(renderer.getSettings().isWrap());
        particleSizeSlider.setValue(renderer.getParticleSize());
        camZoomSlider.setValue(renderer.getCameraFollowZoomFactor());
        darkModeToggle.setState(Theme.getTheme().darkMode);
    }

    private void attachListenersToRenderer(Renderer renderer) {
        renderer.addMatrixChangeListener(matrix -> {
            matrixWidget.matrixChanged(matrix);
            typesSlider.setValue(matrix.size());
        });
        renderer.addParticleDensityListener((n, density) -> densityPerCentSlider.setValue(density * 100));
        renderer.addFrameListener(() -> {
            statsLabel.setText(String.format(
                    "fps: %.0f%n" +
                    "update: %.1f ms%n" +
                    "draw: %.1f ms%n" +
                    "n: %d",
                    renderer.getFps(), renderer.getAvgPhysicsCalcTime(), renderer.getAvgRenderingTime(), renderer.getParticleCount()));
        });
        renderer.addScreenshotListener(this::saveScreenshot);
        renderer.addPauseChangeListener(paused -> togglePause.setState(paused));
    }

    private void attachListenersToWidgets(GraphicalInterface g) {

        matrixWidget.addMatrixChangeListener((i, j, value) -> canvas.getRenderer().request(new RequestMatrixValue(i, j, value)));
        matrixWidget.addRemoveTypeListener(index -> canvas.getRenderer().request(new RequestRemoveType(index, keepDensityToggle.getState())));

        typesSlider.addChangeListener(value -> canvas.getRenderer().request(new RequestMatrixSize(value, keepDensityToggle.getState())));
        heatSlider.addChangeListener(value -> canvas.getRenderer().request(new RequestHeat((float) value)));

        togglePause.setChangeListener(state -> canvas.getRenderer().request(new RequestPause(state)));
        densityPerCentSlider.addChangeListener(value -> canvas.getRenderer().request(new RequestParticleDensity((float) value / 100)));

        randomButton.setOnClickListener(() -> canvas.getRenderer().request(new RequestRandomMatrix()));
        respawnButton.setOnClickListener(() -> canvas.getRenderer().request(new RequestRespawn()));
        randomAndRespawnButton.setOnClickListener(() -> {
            canvas.getRenderer().request(new RequestRandomMatrix());
            canvas.getRenderer().request(new RequestRespawn());
        });
        randomTypesButton.setOnClickListener(() -> canvas.getRenderer().request(new RequestRandomTypes()));

        screenshotButton.setOnClickListener(() -> canvas.getRenderer().request(new RequestScreenshot(
                graphicsProvider.createGraphics(canvas.getWidth(), canvas.getHeight())
        )));

        initializerSelector.addSelectionChangeListener((index, entry) -> canvas.getRenderer().request(new RequestMatrixInitializerIndex(index)));

        spawnSelector.addSelectionChangeListener((index, entry) -> canvas.getRenderer().request(new RequestSpawnMode(index)));

        frictionSlider.addChangeListener(value -> canvas.getRenderer().request(new RequestFriction((float) value)));
        dtToggle.setChangeListener(state -> canvas.getRenderer().request(new RequestDtEnabled(state)));
        dtSlider.addChangeListener(value -> canvas.getRenderer().request(new RequestDt((float) value)));
        forceSlider.addChangeListener(value -> canvas.getRenderer().request(new RequestForce((float) value)));
        rMinSlider.addChangeListener(value -> canvas.getRenderer().request(new RequestRMin((float) value)));
        rMaxSlider.addChangeListener(value -> canvas.getRenderer().request(new RequestRMax((float) value)));
        particleSizeSlider.addChangeListener(value -> canvas.getRenderer().request(new RequestParticleSize((float) value)));
        camZoomSlider.addChangeListener(value -> canvas.getRenderer().request(new RequestCameraFollowZoomFactor((float) value)));
        wrapToggle.setChangeListener(state -> canvas.getRenderer().request(new RequestWrap(state)));

        darkModeToggle.setChangeListener(darkMode -> {
            Theme.setDarkMode(darkMode);
            g.requestRenderForAll();
        });
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