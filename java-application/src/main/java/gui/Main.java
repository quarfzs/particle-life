package gui;

import engine.Renderer;
import guilib.GraphicalInterface;
import guilib.GraphicalInterfaceWrapper;
import guilib.widgets.*;
import requests.*;

import java.util.Map;

public class Main {

    public static void main(String[] args) {
        GraphicalInterfaceWrapper.open("layout.xml", "root", Main::new);
    }

    private CanvasWidget canvas;
    private MatrixWidget matrixWidget;
    private IntSlider typesSlider;
    private FloatSlider heatSlider;
    private FloatSlider densitySlider;
    private Button randomButton;
    private Button respawnButton;
    private Button randomAndRespawnButton;
    private Button randomTypesButton;
    private Toggle toggleRunning;
    private Toggle toggleReplaceRemoved;
    private Selector initializerSelector;
    private Selector spawnSelector;
    private FloatSlider frictionSlider;
    private Toggle dtToggle;
    private FloatSlider dtSlider;
    private FloatSlider forceSlider;
    private FloatSlider rMinSlider;
    private FloatSlider rMaxSlider;

    public Main(GraphicalInterface g) {

        Map<String, Widget> widgets = g.getWidgetMap();

        Container canvasContainer = (Container) widgets.get("canvas-container");
        canvas = new CanvasWidget();
        canvasContainer.setContent(canvas);
        canvas.onLoad(() -> {
            Renderer renderer = canvas.getRenderer();
            if (renderer != null) {

                // this should be called every time the render instance changes:
                copyValues(renderer);
                attachRenderListeners(renderer);

                attachListeners();

            } else {
                System.out.println("Warning: Canvas.getRenderer() returned null!");
            }
        });

        matrixWidget = new MatrixWidget();
        ((Container) widgets.get("matrix-container")).setContent(matrixWidget);

        toggleRunning = (Toggle) widgets.get("toggle-running");
        toggleReplaceRemoved = (Toggle) widgets.get("toggle-replace-removed");
        heatSlider = (FloatSlider) widgets.get("heat-slider");
        typesSlider = (IntSlider) widgets.get("types-slider");
        densitySlider = (FloatSlider) widgets.get("density-slider");
        randomButton = (Button) widgets.get("random-button");
        respawnButton = (Button) widgets.get("respawn-button");
        randomAndRespawnButton = (Button) widgets.get("random-and-respawn-button");
        randomTypesButton = (Button) widgets.get("random-types-button");
        initializerSelector = (Selector) widgets.get("initializer-selector");
        spawnSelector = (Selector) widgets.get("spawn-selector");
        frictionSlider = (FloatSlider) widgets.get("friction-slider");
        dtToggle = (Toggle) widgets.get("dt-toggle");
        dtSlider = (FloatSlider) widgets.get("dt-slider");
        forceSlider = (FloatSlider) widgets.get("force-slider");
        rMinSlider = (FloatSlider) widgets.get("rmin-slider");
        rMaxSlider = (FloatSlider) widgets.get("rmax-slider");
    }

    private void copyValues(Renderer renderer) {
        toggleRunning.setState(!renderer.isPaused());
        matrixWidget.matrixChanged(renderer.getSettings().getMatrix());
        typesSlider.setValue(renderer.getSettings().getMatrix().size());
        densitySlider.setValue(renderer.getParticleDensity());
        heatSlider.setValue(renderer.getSettings().getHeat());
        initializerSelector.setSelectedIndex(renderer.getMatrixInitializerIndex());
        spawnSelector.setSelectedIndex(renderer.getSpawnMode());
        frictionSlider.setValue(renderer.getSettings().getFriction());
        dtToggle.setState(renderer.isFixedTimeStepEnabled());
        dtSlider.setValue(renderer.getFixedTimeStepValueMillis());
        forceSlider.setValue(renderer.getSettings().getForceFactor());
        rMinSlider.setValue(renderer.getSettings().getRMin());
        rMaxSlider.setValue(renderer.getSettings().getRMax());
    }

    private void attachRenderListeners(Renderer renderer) {
        renderer.addMatrixChangeListener(matrix -> {
            matrixWidget.matrixChanged(matrix);
            typesSlider.setValue(matrix.size());
        });
        renderer.addParticleDensityListener((n, density) -> densitySlider.setValue(density));
    }

    private void attachListeners() {

        matrixWidget.addMatrixChangeListener((i, j, value) -> canvas.getRenderer().request(new RequestMatrixValue(i, j, value)));
        matrixWidget.addRemoveTypeListener(index -> canvas.getRenderer().request(
                toggleReplaceRemoved.getState() ?
                        new RequestRemoveType(index, false)
                        : new RequestRemoveType(index))
        );

        typesSlider.addChangeListener(value -> canvas.getRenderer().request(new RequestMatrixSize(value)));
        heatSlider.addChangeListener(value -> canvas.getRenderer().getSettings().setHeat((float) value));//todo: do request

        toggleRunning.setChangeListener(state -> canvas.getRenderer().request(new RequestPause(!state)));
        densitySlider.addChangeListener(value -> canvas.getRenderer().request(new RequestParticleDensity((float) value)));

        randomButton.setOnClickListener(() -> canvas.getRenderer().request(new RequestRandomMatrix()));
        respawnButton.setOnClickListener(() -> canvas.getRenderer().request(new RequestRespawn()));
        randomAndRespawnButton.setOnClickListener(() -> {
            canvas.getRenderer().request(new RequestRandomMatrix());
            canvas.getRenderer().request(new RequestRespawn());
        });
        randomTypesButton.setOnClickListener(() -> canvas.getRenderer().request(new RequestRandomTypes()));

        initializerSelector.addSelectionChangeListener((index, entry) -> canvas.getRenderer().request(new RequestMatrixInitializerIndex(index)));

        spawnSelector.addSelectionChangeListener((index, entry) -> canvas.getRenderer().request(new RequestSpawnMode(index)));

        frictionSlider.addChangeListener(value -> canvas.getRenderer().request(new RequestFriction((float) value)));
        dtToggle.setChangeListener(state -> canvas.getRenderer().request(new RequestDtEnabled(state)));
        dtSlider.addChangeListener(value -> canvas.getRenderer().request(new RequestDt((float) value)));
        forceSlider.addChangeListener(value -> canvas.getRenderer().request(new RequestForce((float) value)));
        rMinSlider.addChangeListener(value -> canvas.getRenderer().request(new RequestRMin((float) value)));
        rMaxSlider.addChangeListener(value -> canvas.getRenderer().request(new RequestRMax((float) value)));
    }
}