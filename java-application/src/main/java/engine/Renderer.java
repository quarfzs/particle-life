package engine;

import logic.DefaultUpdaterLogic;
import logic.Settings;
import logic.Updater;
import logic.UpdaterLogic;
import gui.colormaker.ColorMaker;
import processing.core.PGraphics;
import requests.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

public class Renderer {

    private boolean paused = false;
    private boolean useFixedTimeStep = false;
    private float fixedTimeStepValueMillis = 16;

    private float particleDensity = 0.002f;
    private int nParticles;
    private float particleSize = 2;
    private int spawnMode = 0;
    
    private Settings settings = new Settings();

    private ArrayList<Matrix.Initializer> matrixInitializers = new ArrayList<>();
    private ArrayList<String> matrixInitializerNames = new ArrayList<>();
    private int currentMatrixInitializerIndex = 0;

    private Random random = new Random();
    private ColorMaker colorMaker;
    private HashMap<Integer, Integer> typeColorMap;

    private boolean drawForceDiagram = false;
    private boolean drawRenderingStats = false;

    private float mouseX = 0;
    private float mouseY = 0;
    private float lastMouseX = 0;
    private float lastMouseY = 0;
    private boolean mousePressed = false;
    private Camera camera;

    private float particleDragSelectionRadius = 25;

    private boolean screenshotRequested = false;

    private Updater updater;
    private UpdaterLogic updaterLogic;

    private float windowWidth;
    private float windowHeight;

    private JFrame settingsJFrame = null;

    private final LinkedList<Request> requests = new LinkedList<>();

    public interface MatrixChangeListener {
        void matrixChanged(logic.Matrix matrix);
    }

    public interface ParticleDensityListener {
        void onChange(int n, float density);
    }

    public ArrayList<MatrixChangeListener> matrixChangeListeners = new ArrayList<>();
    public ArrayList<ParticleDensityListener> particleDensityListeners = new ArrayList<>();

    public void addMatrixChangeListener(MatrixChangeListener listener) {
        matrixChangeListeners.add(listener);
    }

    private void notifyMatrixChangeListeners() {
        matrixChangeListeners.forEach(listener -> listener.matrixChanged(settings.getMatrix()));
    }

    public void addParticleDensityListener(ParticleDensityListener listener) {
        particleDensityListeners.add(listener);
    }

    private void notifyParticleDensityChangeListeners() {
        particleDensityListeners.forEach(listener -> listener.onChange(nParticles, particleDensity));
    }

    public Renderer(float width, float height, ColorMaker colorMaker) {

        this.windowWidth = width;
        this.windowHeight = height;
        this.colorMaker = colorMaker;

        this.camera = new Camera(width / 2f, height / 2f);

        initAttractionSetters();
        settings.setMatrix(new Matrix(6, (i, j) -> 0));
        makeMatrix();
        calcColors();

        this.updater = new MultithreadedUpdater();
        this.updaterLogic = new DefaultUpdaterLogic();
        settings.setRange(width, height);
        makeMatrix();
        resetUpdaterSettings();

        nParticles = calcParticleCount();

        spawnParticles();

        notifyMatrixChangeListeners();
    }

    public Settings getSettings() {
        return settings;
    }

    private int calcParticleCount() {
        return (int) (particleDensity * settings.getRangeX() * settings.getRangeY());
    }

    private float calcParticleDensity() {
        return nParticles / (settings.getRangeX() * settings.getRangeY());
    }

    private void resetUpdaterSettings() {
        settings.setForceFactor(950);
        settings.setFriction(9);
        settings.setHeat(0);
        settings.setRMin(10);
        settings.setRMax(40);
        settings.setWrap(true);
    }

    private interface OnStateChangedCallback {
        void onStateChanged(boolean state);
    }

    private interface SimpleCallback {
        void call();
    }

    private interface OnValueChangedCallback {
        void onValueChanged(int value);
    }

    private void openSettingsGUI() {
        if (settingsJFrame != null) {
            settingsJFrame.dispose();
            settingsJFrame = null;
        }

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        JFrame frame = new JFrame("Settings");
        Dimension dimen = new Dimension(460, 520);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setSize(dimen);
        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));
        panel.setSize(dimen);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);  // faster scrolling
        frame.getContentPane().add(scrollPane);

        class GUIBuilder {

            private void addCheckBox(String label, boolean initialSate, OnStateChangedCallback onStateChangedCallback) {
                JCheckBox checkBox = new JCheckBox(label, initialSate);
                checkBox.addActionListener(e -> onStateChangedCallback.onStateChanged(checkBox.isSelected()));
                panel.add(checkBox);
            }

            private void addButton(String text, SimpleCallback onPressed) {
                JButton button = new JButton(text);
                button.addActionListener(e -> onPressed.call());
                panel.add(button);
            }

            private void addSlider(String label, int initialValue, OnValueChangedCallback onValueChangedCallback,
                                   int minValue, int maxValue, int minorTickSpacing, int majorTickSpacing) {
                panel.add(Box.createRigidArea(new Dimension(0, 10)));
                panel.add(new JLabel(label));
                JSlider slider = new JSlider(SwingConstants.HORIZONTAL, minValue, maxValue, initialValue);
                slider.setMajorTickSpacing(majorTickSpacing);
                slider.setMinorTickSpacing(minorTickSpacing);
                slider.setPaintTicks(true);
                slider.setPaintLabels(true);
                slider.addChangeListener(e -> onValueChangedCallback.onValueChanged(slider.getValue()));
                panel.add(slider);
            }
        }

        GUIBuilder c = new GUIBuilder();

        c.addSlider("Number of Types (Colors)", settings.getMatrix().size(), this::requestMatrixSize, 1, 17, 0, 1);
        c.addSlider("Particle Density (in 1/1000s per pixel)", (int) (particleDensity * 4000), value -> request(new RequestParticleDensity(value / 4000f)), 0, 50, 1, 5);
        c.addSlider("Matrix Initializer (enable diagram for better understanding)", currentMatrixInitializerIndex, this::requestMatrixInitializerIndex, 0, matrixInitializers.size() - 1, 0, 1);
        c.addSlider("Spawn Mode", spawnMode, value -> spawnMode = value, 0, 4, 1, 1);
        c.addButton("New World", this::requestReset);
        c.addButton("Stir Up", this::requestRespawn);
        {
            final JTextArea matrixTextArea = new JTextArea(6, 24);
            JScrollPane matrixScrollPane = new JScrollPane(matrixTextArea);
            panel.add(matrixScrollPane);

            c.addButton("Apply Matrix", () -> {
                Matrix m = MatrixParser.parseMatrix(matrixTextArea.getText());
                if (m != null) {
                    requestMatrix(m);
                }
            });
            c.addButton("Get Matrix", () -> matrixTextArea.setText(MatrixParser.matrixToString(settings.getMatrix())));
            c.addButton("Round & Format", () -> {
                Matrix m = MatrixParser.parseMatrix(matrixTextArea.getText());
                if (m != null) {
                    matrixTextArea.setText(MatrixParser.matrixToStringRoundAndFormat(m));
                }
            });
        }
        c.addSlider("rKern", (int) settings.getRMin(), settings::setRMin, 0, 100, 10, 20);
        c.addSlider("rMax ( > rKern!)", (int) settings.getRMax(), settings::setRMax, 10, 100, 10, 20);
        c.addSlider("Heat", (int) settings.getHeat(), settings::setHeat, 0, 200, 10, 50);
        c.addSlider("Friction", (int) settings.getFriction(), settings::setFriction, 0, 60, 5, 10);
        c.addSlider("Force Factor", (int) settings.getForceFactor(), settings::setForceFactor, 0, 1500, 50, 250);
        c.addSlider("Particle Size on Screen", (int) particleSize, value -> particleSize = value, 1, 5, 0, 1);
        c.addCheckBox("Wrap World", settings.isWrap(), settings::setWrap);
        c.addCheckBox("Draw Matrix", drawForceDiagram, state -> drawForceDiagram = state);
        c.addCheckBox("Draw Rendering Stats", drawRenderingStats, state -> drawRenderingStats = state);
        c.addButton("Save Screenshot", this::requestScreenshot);
        c.addCheckBox("Use Fixed Timestep", useFixedTimeStep, state -> useFixedTimeStep = state);
        c.addSlider("Fixed TimeStep (ms)", (int) fixedTimeStepValueMillis, value -> fixedTimeStepValueMillis = value,
                1, 100, 1, 10);

        frame.setVisible(true);
        //frame.setLocationRelativeTo(null);  // center window on screen
        frame.setLocation(
                Math.min(Math.max(0, (int) mouseX - frame.getWidth() / 2), (int) windowWidth - frame.getWidth()),
                Math.min(Math.max(0, (int) mouseY - frame.getHeight() / 2), (int) windowHeight - frame.getHeight())
        );
        settingsJFrame = frame;
    }

    public void stop() {
        if (settingsJFrame != null) {
            settingsJFrame.dispose();
        }
    }

    private void reset() {
        makeMatrix();
        calcColors();
        spawnParticles();
        camera.stopFollow();

        notifyMatrixChangeListeners();
    }

    private void respawn() {
        spawnParticles();
        camera.stopFollow();
    }

    private void addAttractionSetter(String name, Matrix.Initializer initializer) {
        matrixInitializerNames.add(name);
        matrixInitializers.add(initializer);
    }

    private void initAttractionSetters() {

        final Matrix.Initializer randomInitializer = (i, j) -> Helper.uniform(-1, 1);
        addAttractionSetter("random f", randomInitializer);

        addAttractionSetter("chains", new Matrix.Initializer() {
            private int n;

            @Override
            public void init(int n) {
                this.n = n;
            }

            @Override
            public float getValue(int i, int j) {
                if (j == i) {
                    return 1f;
                } else if (j == Helper.modulo(i - 1, n)) {
                    return 0.0f;
                } else if (j == Helper.modulo(i + 1, n)) {
                    return 0.2f;
                }
                return 0f;
            }
        });

        addAttractionSetter("random chains", new Matrix.Initializer() {
            private int n;

            @Override
            public void init(int n) {
                this.n = n;
            }

            @Override
            public float getValue(int i, int j) {
                if (j == i) {
                    return Helper.uniform(0.2f, 1.0f);
                } else if (j == Helper.modulo(i - 1, n)) {
                    return 0.0f;
                } else if (j == Helper.modulo(i + 1, n)) {
                    return 0.2f;
                }
                return Helper.uniform(-0.0f, 0.0f);
            }
        });

        addAttractionSetter("equal pairs", new Matrix.Initializer() {
            private Matrix m;

            @Override
            public void init(int n) {
                m = new Matrix(n, randomInitializer);
            }

            @Override
            public float getValue(int i, int j) {
                return m.get(Math.min(i, j), Math.max(i, j));
            }
        });
    }

    /**
     * Creates the matrix with the current initializer.
     * Also correctly sets the colors.
     */
    private void makeMatrix() {
        settings.setMatrix(
                new Matrix(settings.getMatrix().size(),
                matrixInitializers.get(currentMatrixInitializerIndex))
        );
    }

    private void calcColors() {
        final int matrixSize = settings.getMatrix().size();
        typeColorMap = new HashMap<>(matrixSize);
        for (int type = 0; type < matrixSize; type++) {
            typeColorMap.put(type, colorMaker.getColor(type / (float) matrixSize));
        }
    }


    private void spawnParticles() {

        int[] types = new int[nParticles];
        float[] positions = new float[nParticles * 2];
        float[] velocities = new float[nParticles * 2];

        float rangeX = settings.getRangeX();
        float rangeY = settings.getRangeY();

        int nTypes = settings.getMatrix().size();

        int typeIndex = 0;
        int positionIndex = 0;

        while (positionIndex < positions.length) {

            float randomX;
            float randomY;

            float radius = Math.min(rangeX, rangeY) / 3;
            switch (spawnMode) {
                case 1: {  // sphere
                    double angle = 2 * Math.PI * Math.random();
                    float r = radius * (float) random.nextGaussian();
                    randomX = rangeX / 2 + r * (float) Math.cos(angle);
                    randomY = rangeY / 2 + r * (float) Math.sin(angle);
                    break;
                } case 2: {  // centered
                    double angle = 2 * Math.PI * Math.random();
                    float r = radius * (float) Math.sqrt(Math.random());
                    randomX = rangeX / 2 + r * (float) Math.cos(angle);
                    randomY = rangeY / 2 + r * (float) Math.sin(angle);
                    break;
                } case 3: {  // centered sphere
                    double angle = 2 * Math.PI * Math.random();
                    float r = radius * (float) Math.random();
                    randomX = rangeX / 2 + r * (float) Math.cos(angle);
                    randomY = rangeY / 2 + r * (float) Math.sin(angle);
                    break;
                } case 4: {  // spiral
                    double f = Math.random();
                    double angle = 2 * Math.PI * f;
                    float r = radius * (float) Math.sqrt(f) + radius * 0.1f * (float) Math.random();
                    randomX = rangeX / 2 + r * (float) Math.cos(angle);
                    randomY = rangeY / 2 + r * (float) Math.sin(angle);
                    break;
                } default: {  // uniform
                    randomX = rangeX * (float) Math.random();
                    randomY = rangeY * (float) Math.random();
                    break;
                }
            }

            types[typeIndex] = random.nextInt(nTypes);
            positions[positionIndex] = randomX;
            positions[positionIndex + 1] = randomY;
            velocities[positionIndex] = 0;
            velocities[positionIndex + 1] = 0;

            typeIndex += 1;
            positionIndex += 2;
        }

        updater.setTypes(types);
        updater.setPositions(positions);
        updater.setVelocities(velocities);
    }

    public void keyReleased(char key) {
        switch (key) {
            case 'r' -> reset();
            case 's' -> respawn();
            case 'o' -> openSettingsGUI();
            case 'f' -> toggleCameraFollow();
            case ' ' -> paused ^= true;
        }
    }

    public void mouseScrolled(float pixels) {
        particleDragSelectionRadius *= (float) Math.pow(2, pixels / 2000);
        particleDragSelectionRadius = Math.max(particleDragSelectionRadius, 0.1f);
        particleDragSelectionRadius = Math.min(particleDragSelectionRadius, 2 * Math.max(windowWidth, windowHeight));
    }

    public void mouseMoved(float x, float y) {
        setNewMousePos(x, y);
    }

    public void mousePressed(int button) {
        if (button == 0) {
            mousePressed = true;
        } else if (button == 1) {
            toggleCameraFollow();
        } else if (button == 2) {
            openSettingsGUI();
        }
    }

    public void mouseReleased(int button) {
        if (button == 0) {
            mousePressed = false;
        }
    }

    private void setNewMousePos(float x, float y) {
        mouseX = x;
        mouseY = y;
    }

    private void toggleCameraFollow() {
        if (camera.isFollowing()) {
            camera.stopFollow();
        } else {
            camera.startFollow(updater, mouseX, mouseY, particleDragSelectionRadius, settings.isWrap());
        }
    }

    public void updateUI(float dt) {

        // handle requests

        while (!requests.isEmpty()) {
            handleRequest(requests.remove());
        }

        /*
        if (screenshotRequested) {
            screenshotRequested = false;
        }

        if (requestedParticleDensity != particleDensity) {
            particleDensity = requestedParticleDensity;
            nParticles = calcParticleCount();
        }

        if (requestedMatrix != null) {
            boolean matrixSizeChanged = requestedMatrix.size() != matrix.size();
            matrix = requestedMatrix;
            requestedMatrix = null;
            nextMatrixSize = matrix.size();
            settings.setMatrix(matrix);
            if (matrixSizeChanged) {
                calcColors();
                respawn();
            }

            notifyMatrixChangeListeners();
        }

        if (nextMatrixSize != matrix.size()) {
            requestReset();
        }

        if (requestedMatrixInitializerIndex != currentMatrixInitializerIndex) {
            currentMatrixInitializerIndex = requestedMatrixInitializerIndex;
            requestReset();
        }

        if (resetRequested) {
            resetRequested = false;
            reset();
        } else if (respawnRequested) {
            respawnRequested = false;
            respawn();
        }*/

        camera.update(updater, dt);
    }

    private void handleRequest(Request r) {

        if (r instanceof RequestMatrixValue) {

            RequestMatrixValue req = (RequestMatrixValue) r;
            logic.Matrix oldMatrix = settings.getMatrix();
            settings.setMatrix(new Matrix(
                    oldMatrix.size(),
                    (i, j) -> (i == req.i && j == req.j) ? req.value : oldMatrix.get(i, j)
            ));

            notifyMatrixChangeListeners();

        } else if (r instanceof RequestMatrix) {

            RequestMatrix req = (RequestMatrix) r;
            boolean matrixSizeChanged = req.matrix.size() != settings.getMatrix().size();

            settings.setMatrix(req.matrix);
            if (matrixSizeChanged) {
                calcColors();
            }

            notifyMatrixChangeListeners();

        } else if (r instanceof RequestRandomMatrix) {

            makeMatrix();
            notifyMatrixChangeListeners();

        } else if (r instanceof RequestRespawn) {

            respawn();

        } else if (r instanceof RequestRandomTypes) {

            int[] types = updater.getTypes();

            int nTypes = settings.getMatrix().size();
            for (int i = 0; i < types.length; i++) {
                types[i] = random.nextInt(nTypes);
            }

            updater.setTypes(types);

        } else if (r instanceof RequestParticleDensity) {

            float requestedParticleDensity = ((RequestParticleDensity) r).density;
            if (requestedParticleDensity != particleDensity) {

                int[] types = updater.getTypes();
                float[] positions = updater.getPositions();
                float[] velocities = updater.getVelocities();

                particleDensity = requestedParticleDensity;
                nParticles = calcParticleCount();

                int[] newTypes = new int[nParticles];
                float[] newPositions = new float[nParticles * 2];
                float[] newVelocities = new float[nParticles * 2];

                if (newTypes.length < types.length) {
                    // kick some random points out

                    int nKick = types.length - newTypes.length;
                    double kickProb = nKick / (double) types.length;

                    int i = 0;
                    int newTypeIndex = 0;
                    while (i < types.length && newTypeIndex < newTypes.length) {

                        // only skip if there are more points left than needed

                        if (nKick > 0 && random.nextDouble() < kickProb) {
                            nKick--;
                        } else {
                            newTypes[newTypeIndex] = types[i];
                            newPositions[newTypeIndex * 2] = positions[i * 2];
                            newPositions[newTypeIndex * 2 + 1] = positions[i * 2 + 1];
                            newVelocities[newTypeIndex * 2] = velocities[i * 2];
                            newVelocities[newTypeIndex * 2 + 1] = velocities[i * 2 + 1];
                            newTypeIndex++;
                        }

                        i++;
                    }

                } else {
                    // add some random points

                    // first, copy the whole array
                    for (int i = 0; i < types.length; i++) {
                        newTypes[i] = types[i];
                        newPositions[i * 2] = positions[i * 2];
                        newPositions[i * 2 + 1] = positions[i * 2 + 1];
                        newVelocities[i * 2] = velocities[i * 2];
                        newVelocities[i * 2 + 1] = velocities[i * 2 + 1];
                    }

                    // add new random points to the end of the array
                    int nTypes = settings.getMatrix().size();
                    for (int i = types.length; i < newTypes.length; i++) {
                        newTypes[i] = random.nextInt(nTypes);
                        newPositions[i * 2] = random.nextFloat() * settings.getRangeX();
                        newPositions[i * 2 + 1] = random.nextFloat() * settings.getRangeY();
                        newVelocities[i * 2] = 0;
                        newVelocities[i * 2 + 1] = 0;
                    }
                }

                updater.setTypes(newTypes);
                updater.setPositions(newPositions);
                updater.setVelocities(newVelocities);

                notifyParticleDensityChangeListeners();
            }

        } else if (r instanceof RequestMatrixInitializerIndex) {

            currentMatrixInitializerIndex = ((RequestMatrixInitializerIndex) r).index;

        } else if (r instanceof RequestMatrixSize) {

            int requestedMatrixSize = ((RequestMatrixSize) r).size;
            int oldMatrixSize = settings.getMatrix().size();

            if (requestedMatrixSize != oldMatrixSize) {

                if (requestedMatrixSize < oldMatrixSize) {
                    // remove points of types that now no longer exist
                    for (int i = requestedMatrixSize; i < oldMatrixSize; i++) {
                        request(new RequestRemoveType(i));
                    }
                } else {
                    logic.Matrix oldMatrix = settings.getMatrix();
                    settings.setMatrix(new Matrix(
                            requestedMatrixSize,
                            (i, j) -> (i < oldMatrixSize && j < oldMatrixSize) ? oldMatrix.get(i, j) : 0
                    ));
                    calcColors();
                    notifyMatrixChangeListeners();
                }
            }

        } else if (r instanceof RequestScreenshot) {

            //todo

        } else if (r instanceof RequestRemoveType) {

            RequestRemoveType req = (RequestRemoveType) r;

            if (req.index >= 0 && req.index < settings.getMatrix().size() && settings.getMatrix().size() >= 2) {

                // make matrix without the given type

                logic.Matrix oldMatrix = settings.getMatrix();

                settings.setMatrix(new Matrix(
                        oldMatrix.size() - 1,
                        (i, j) -> oldMatrix.get(i < req.index ? i : i + 1, j < req.index ? j : j + 1)
                ));

                calcColors();

                // remove all particles of the given type and decrease type of all larger types

                int[] types = updater.getTypes();
                float[] positions = updater.getPositions();
                float[] velocities = updater.getVelocities();

                if (req.replaceRemovedPoints) {

                    int nTypes = settings.getMatrix().size();

                    for (int i = 0; i < types.length; i++) {
                        if (types[i] == req.index) {
                            if (req.replaceRandom) {
                                types[i] = random.nextInt(nTypes);
                            } else {
                                types[i] = req.newType;
                            }
                        } else if (types[i] > req.index) {
                            types[i]--;
                        }
                    }

                    updater.setTypes(types);
                    updater.setPositions(positions);
                    updater.setVelocities(velocities);

                } else {

                    for (int value : types) {
                        if (value == req.index) {
                            nParticles--;
                        }
                    }

                    particleDensity = calcParticleDensity();

                    int[] newTypes = new int[nParticles];
                    float[] newPositions = new float[nParticles * 2];
                    float[] newVelocities = new float[nParticles * 2];

                    int newTypesIndex = 0;

                    for (int i = 0; i < types.length; i++) {

                        int type = types[i];
                        if (type != req.index) {

                            newTypes[newTypesIndex] = type < req.index ? type : type - 1;
                            newPositions[newTypesIndex * 2] = positions[i * 2];
                            newPositions[newTypesIndex * 2 + 1] = positions[i * 2 + 1];
                            newVelocities[newTypesIndex * 2] = velocities[i * 2];
                            newVelocities[newTypesIndex * 2 + 1] = velocities[i * 2 + 1];

                            newTypesIndex++;
                        }
                    }

                    updater.setTypes(newTypes);
                    updater.setPositions(newPositions);
                    updater.setVelocities(newVelocities);

                    notifyParticleDensityChangeListeners();
                }

                calcColors();
                notifyMatrixChangeListeners();
            }

        } else if (r instanceof RequestSpawnMode) {

            spawnMode = ((RequestSpawnMode) r).spawnMode;
        } else if (r instanceof RequestFriction) {
            settings.setFriction(((RequestFriction) r).friction);
        } else if (r instanceof RequestDtEnabled) {
            useFixedTimeStep = ((RequestDtEnabled) r).dtEnabled;
        } else if (r instanceof RequestDt) {
            fixedTimeStepValueMillis = ((RequestDt) r).dt;
        } else if (r instanceof RequestForce) {
            settings.setForceFactor(((RequestForce) r).force);
        } else if (r instanceof RequestRMin) {
            settings.setRMin(((RequestRMin) r).rMin);
        } else if (r instanceof RequestRMax) {
            settings.setRMax(((RequestRMax) r).rMax);
        } else if (r instanceof RequestPause) {
            paused = ((RequestPause) r).pause;
        }
    }

    public void update(float dt) {

        if (!paused) {
            if (useFixedTimeStep) {
                settings.setDt(fixedTimeStepValueMillis / 1000f);
            } else {
                settings.setDt(dt);
            }
            updater.updateVelocities(settings, this.updaterLogic);
        }

        applyDrag();

        if (!paused) {
            updater.updatePositions(settings, this.updaterLogic);
        }
    }

    private void applyDrag() {
        if (mousePressed) {
            // drag all particles in a specific radius

            float[] positions = updater.getPositions();
            float[] velocities = updater.getVelocities();

            float dragX = mouseX - lastMouseX;
            float dragY = mouseY - lastMouseY;

            for (int index : updater.getRelevant(lastMouseX, lastMouseY, particleDragSelectionRadius, settings.isWrap())) {

                int positionIndex = index * 2;

                float x = positions[positionIndex];
                float y = positions[positionIndex + 1];

                if (settings.isWrap()) {

                    if (x > lastMouseX) {
                        float wrappedX2 = x - settings.getRangeX();
                        if (lastMouseX - wrappedX2 < x - lastMouseX) {
                            x = wrappedX2;
                        }
                    } else {
                        float wrappedX2 = x + settings.getRangeX();
                        if (wrappedX2 - lastMouseX < lastMouseX - x) {
                            x = wrappedX2;
                        }
                    }
                    if (y > lastMouseY) {
                        float wrappedY2 = y - settings.getRangeY();
                        if (lastMouseY - wrappedY2 < y - lastMouseY) {
                            y = wrappedY2;
                        }
                    } else {
                        float wrappedY2 = y + settings.getRangeY();
                        if (wrappedY2 - lastMouseY < lastMouseY - y) {
                            y = wrappedY2;
                        }
                    }
                }

                float dx = x - lastMouseX;
                float dy = y - lastMouseY;

                if (dx * dx + dy * dy < particleDragSelectionRadius * particleDragSelectionRadius) {

                    velocities[positionIndex] = 0;
                    velocities[positionIndex + 1] = 0;

                    x += dragX;
                    y += dragY;

                    positions[positionIndex] = x;
                    positions[positionIndex + 1] = y;
                }
            }
        }

        lastMouseX = mouseX;
        lastMouseY = mouseY;
    }

    public void draw(PGraphics context) {

        context.pushStyle();
        context.pushMatrix();

        camera.apply(context);

        context.ellipseMode(context.RADIUS);

        drawParticles(context);

        if (!camera.isFollowing() && Math.abs(camera.getScale() - 1) < 0.1f) {
            context.pushStyle();
            context.noFill();
            context.stroke(128);
            context.ellipse(mouseX, mouseY, particleDragSelectionRadius, particleDragSelectionRadius);
            context.popStyle();
        }

        context.popMatrix();
        context.popStyle();

        if (drawForceDiagram) {
            drawForces(context, settings.getMatrix());
        }

        if (drawRenderingStats) {
            context.pushStyle();
            context.textAlign(context.RIGHT, context.BOTTOM);
            context.fill(255);
            context.text(nParticles, context.width, context.height);
            context.popStyle();
        }
    }

    public void drawParticles(PGraphics context) {

        context.pushStyle();

        context.noStroke();

        int[] types = updater.getTypes();
        float[] positions = updater.getPositions();

        int typeIndex = 0;
        int positionIndex = 0;

        while (positionIndex < positions.length) {

            int type = types[typeIndex];
            float x = positions[positionIndex];
            float y = positions[positionIndex + 1];

            context.fill(getColor(type));
            context.rect(x, y, particleSize, particleSize);

            typeIndex += 1;
            positionIndex += 2;
        }

        context.popStyle();
    }

    private int getColor(int type) {
        return typeColorMap.get(type);
    }

    private void drawForces(PGraphics context, logic.Matrix matrix) {
        context.pushStyle();
        context.pushMatrix();

        float size = 20;

        context.translate(context.width - size * (matrix.size() + 1), 0);

        context.translate(size/2, size/2);

        for (int type = 0; type < matrix.size(); type++) {
            context.fill(typeColorMap.get(type));
            context.ellipse(size + type * size, 0, size / 2, size / 2);
            context.ellipse(0, size + type * size, size / 2, size / 2);
        }

        context.translate(size / 2, size / 2);
        context.textAlign(context.CENTER, context.CENTER);
        for (int i = 0; i < matrix.size(); i++) {
            for (int j = 0; j < matrix.size(); j++) {

                float attraction = matrix.get(i, j);

                if (attraction > 0) {
                    float c = 255 * attraction;
                    context.fill(0, c, 0);
                } else {
                    float c = -255 * attraction;
                    context.fill(c, 0, 0);
                }

                context.rect(j * size, i * size, size, size);

                context.fill(255);
                context.text(String.format("%.0f", attraction * 10), (j + 0.5f) * size, (i + 0.5f) * size);
            }
        }

        context.fill(255);
        context.textAlign(context.RIGHT);
        context.text(String.format("%s [%d]",
                matrixInitializerNames.get(currentMatrixInitializerIndex),
                currentMatrixInitializerIndex
        ), size * matrix.size(), size * matrix.size() + size);

        context.popMatrix();
        context.popStyle();
    }

    public boolean shouldDrawRenderingStats() {
        return drawRenderingStats;
    }

    public int getParticleCount() {
        return nParticles;
    }

    public float getParticleDensity() {
        return particleDensity;
    }

    public boolean isScreenshotRequested() {
        return screenshotRequested;
    }

    public int getMatrixInitializerIndex() {
        return currentMatrixInitializerIndex;
    }

    public int getSpawnMode() {
        return spawnMode;
    }

    public boolean isFixedTimeStepEnabled() {
        return useFixedTimeStep;
    }

    public float getFixedTimeStepValueMillis() {
        return fixedTimeStepValueMillis;
    }

    public boolean isPaused() {
        return paused;
    }

    public void request(Request r) {
        requests.add(r);
    }

    // REQUESTS (OLD)

    private void requestScreenshot() {
        screenshotRequested = true;
    }

    public void requestMatrixSize(int matrixSize) {
        //todo: remove
        // nextMatrixSize = matrixSize;
    }

    private void requestMatrix(Matrix matrix) {
        //todo: remove
        //requestedMatrix = matrix;
    }

    public void requestParticleDensity(float density) {
        //todo: remove
        // requestedParticleDensity = density;
    }

    /**
     * use this if you want to call {@link #reset()} from another Thread
     */
    public void requestReset() {
        //todo: remove
        // resetRequested = true;
    }

    /**
     * use this if you want to call <code>respawn()</code> from another Thread
     */
    public void requestRespawn() {
        //todo: remove
        // respawnRequested = true;
    }

    private void requestMatrixInitializerIndex(int index) {
        //todo: remove
        //requestedMatrixInitializerIndex = index;
    }
}
