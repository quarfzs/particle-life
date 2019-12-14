package life;

import pointmanagement.Point;
import pointmanagement.PointManager;
import processing.core.PGraphics;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class World {

    private static final float boxMargin = 10f;
    private float particleDensity = 0.002f;
    private float boxWidth;
    private float boxHeight;
    private float friction = 9f;
    private float heat = 0f;
    private float forceFactor = 950f;
    private int nParticles;
    private float rKern = 10;
    private float rMax = 40;
    private float particleSize = 2;
    private boolean wrapWorld = true;
    private int spawnMode = 0;

    private Matrix matrix;
    private Matrix requestedMatrix = null;
    private int nextMatrixSize = 6;
    private HashMap<Integer, Integer> typeColorMap;
    private ArrayList<Matrix.Initializer> matrixInitializers = new ArrayList<>();
    private ArrayList<String> matrixInitializerNames = new ArrayList<>();
    private int currentMatrixInitializerIndex = 0;
    private int requestedMatrixInitializerIndex = currentMatrixInitializerIndex;

    private Random random = new Random();
    private ColorMaker colorMaker;

    private PointManager pm;
    private PointUpdaterDefault pointUpdater = new PointUpdaterDefault();

    private boolean resetRequested = false;
    private boolean respawnRequested = false;
    private float requestedRMax = rMax;
    private float requestedParticleDensity = particleDensity;

    private boolean drawParticleManager = false;
    private boolean drawForceDiagram = false;
    private boolean drawRenderingStats = false;

    private float mouseX = 0;
    private float mouseY = 0;
    private float pendingDragX = 0;
    private float pendingDragY = 0;
    private boolean mousePressed = false;
    private Camera camera;
    private float cameraFocusSelectionRadius = 25f;

    private float particleDragSelectionRadius = 25f;



    private boolean screenshotRequested = false;

    private JFrame settingsJFrame = null;

    public World(float width, float height, Camera camera, ColorMaker colorMaker) {

        this.camera = camera;
        boxWidth = width;
        boxHeight = height;
        this.colorMaker = colorMaker;

        nParticles = calcParticleCount();
        System.out.printf("number of particles: %d%n", nParticles);
        System.out.printf("Radius of interaction: %.1f%n", rMax);

        pm = createNewPointManager();

        init();
    }

    private int calcParticleCount() {
        return (int) (particleDensity * boxWidth * boxHeight);
    }

    private PointManager createNewPointManager() {
        return new PointManager(rMax, particleDensity, 0, boxWidth, 0, boxHeight);
    }

    /**
     * create new point manager, but keep the particles
     */
    private void recreatePointManager() {
        PointManager oldPointManager = pm;
        pm = createNewPointManager();
        PointManager.AllIterator all = oldPointManager.getAllWithRelevant(wrapWorld);
        while (all.hasNext()) {
            pm.add(all.next());
        }
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

            private void addSlider(String label, int initialValue, int minValue, int maxValue,
                                   int minorTickSpacing, int majorTickSpacing,
                                   OnValueChangedCallback onValueChangedCallback) {
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

        c.addSlider("Number of Types (Colors)", nextMatrixSize, 1, 17, 0, 1, this::requestMatrixSize);
        c.addSlider("Particle Density (in 1/1000s per pixel)", (int) (particleDensity * 1000), 0, 5, 0, 1, value -> requestedParticleDensity = value / 1000f);
        c.addSlider("Matrix Initializer (enable diagram for better understanding)", currentMatrixInitializerIndex, 0, matrixInitializers.size() - 1, 0, 1, this::requestMatrixInitializerIndex);
        c.addSlider("Spawn Mode", spawnMode, 0, 4, 1, 1, value -> spawnMode = value);
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
            c.addButton("Get Matrix", () -> matrixTextArea.setText(MatrixParser.matrixToString(matrix)));
            c.addButton("Round & Format", () -> {
                Matrix m = MatrixParser.parseMatrix(matrixTextArea.getText());
                if (m != null) {
                    matrixTextArea.setText(MatrixParser.matrixToStringRoundAndFormat(m));
                }
            });
        }
        c.addSlider("rKern", (int) rKern, 0, 100, 10, 20, value -> rKern = value);
        c.addSlider("rMax ( > rKern!)", (int) rMax, 10, 100, 10, 20, this::requestNewRMax);
        c.addSlider("Heat", (int) heat, 0, 100, 10, 20, value -> heat = value);
        c.addSlider("Friction", (int) friction, 0, 100, 10, 20, value -> friction = value);
        c.addSlider("Force Factor", (int) forceFactor, 0, 10000, 500, 2000, value -> forceFactor = value);
        c.addSlider("Particle Size on Screen", (int) particleSize, 1, 5, 0, 1, value -> particleSize = value);
        c.addCheckBox("Wrap World", wrapWorld, state -> wrapWorld = state);
        c.addCheckBox("Draw Matrix", drawForceDiagram, state -> drawForceDiagram = state);
        c.addCheckBox("Draw Containers", drawParticleManager, state -> drawParticleManager = state);
        c.addCheckBox("Draw Rendering Stats", drawRenderingStats, state -> drawRenderingStats = state);
        c.addButton("Save Screenshot", this::requestScreenshot);

        frame.setVisible(true);
        //frame.setLocationRelativeTo(null);  // center window on screen
        frame.setLocation(
                Math.min(Math.max(0, (int) mouseX - frame.getWidth() / 2), (int) boxWidth - frame.getWidth()),
                Math.min(Math.max(0, (int) mouseY - frame.getHeight() / 2), (int) boxHeight - frame.getHeight())
        );
        settingsJFrame = frame;
    }

    public void stop() {
        if (settingsJFrame != null) {
            settingsJFrame.dispose();
        }
    }

    private void init() {
        initAttractionSetters();
        makeMatrix();
        calcColors();
        spawnParticles();
    }

    private void requestNewRMax(float newRMax) {
        requestedRMax = newRMax;
    }

    private void requestMatrixSize(int matrixSize) {
        nextMatrixSize = matrixSize;
    }

    private void requestMatrix(Matrix matrix) {
        requestedMatrix = matrix;
    }

    /**
     * use this if you want to call {@link #reset()} from another Thread
     */
    private void requestReset() {
        resetRequested = true;
    }

    /**
     * use this if you want to call <code>respawn()</code> from another Thread
     */
    private void requestRespawn() {
        respawnRequested = true;
    }

    private void reset() {
        pm.clear();
        makeMatrix();
        calcColors();
        spawnParticles();
        camera.stopFollow();
    }

    private void respawn() {
        pm.clear();
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
        matrix = new Matrix(nextMatrixSize, matrixInitializers.get(currentMatrixInitializerIndex));
    }

    private void calcColors() {
        typeColorMap = new HashMap<>(matrix.n);
        for (int type = 0; type < matrix.n; type++) {
            typeColorMap.put(type, colorMaker.hsb(
                    type / (float) matrix.n,
                    1,
                    1
            ));
        }
    }


    private void spawnParticles() {
        for (int i = 0; i < nParticles; i++) {

            float randomX;
            float randomY;

            float radius = Math.min(boxWidth, boxHeight) / 3;
            switch (spawnMode) {
                case 1: {
                    double angle = 2 * Math.PI * Math.random();
                    float r = radius * (float) random.nextGaussian();
                    randomX = boxWidth / 2 + r * (float) Math.cos(angle);
                    randomY = boxHeight / 2 + r * (float) Math.sin(angle);
                    break;
                } case 2: {
                    double angle = 2 * Math.PI * Math.random();
                    float r = radius * (float) Math.sqrt(Math.random());
                    randomX = boxWidth / 2 + r * (float) Math.cos(angle);
                    randomY = boxHeight / 2 + r * (float) Math.sin(angle);
                    break;
                } case 3: {
                    double angle = 2 * Math.PI * Math.random();
                    float r = radius * (float) Math.random();
                    randomX = boxWidth / 2 + r * (float) Math.cos(angle);
                    randomY = boxHeight / 2 + r * (float) Math.sin(angle);
                    break;
                } case 4: {
                    double f = Math.random();
                    double angle = 2 * Math.PI * f;
                    float r = radius * (float) Math.sqrt(f) + radius * 0.1f * (float) Math.random();
                    randomX = boxWidth / 2 + r * (float) Math.cos(angle);
                    randomY = boxHeight / 2 + r * (float) Math.sin(angle);
                    break;
                } default: {
                    randomX = boxWidth * (float) Math.random();
                    randomY = boxHeight * (float) Math.random();
                    break;
                }
            }

            pm.add(new Particle(randomX, randomY, random.nextInt(matrix.n)));
        }
    }

    private void requestMatrixInitializerIndex(int index) {
        requestedMatrixInitializerIndex = index;
    }

    public void keyReleased(char key) {
        switch (key) {
            case 'r':
                reset();
                break;
            case 's':
                respawn();
                break;
            case 'o':
                openSettingsGUI();
                break;
            case 'f':
                toggleCameraFollow();
                break;
        }
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
        float mouseXBefore = mouseX;
        float mouseYBefore = mouseY;
        mouseX = x;
        mouseY = y;

        if (mousePressed) {
            // accumulate drag if mouse position events are called multiple times between two update calls
            pendingDragX += mouseX - mouseXBefore;
            pendingDragY += mouseY - mouseYBefore;
        }
    }

    private void toggleCameraFollow() {
        if (camera.isFollowing()) {
            camera.stopFollow();
        } else {
            camera.startFollow(pm, mouseX, mouseY, cameraFocusSelectionRadius, wrapWorld);
        }
    }

    public void update(float dt) {

        if (screenshotRequested) {
            screenshotRequested = false;
        }

        if (requestedRMax != rMax || requestedParticleDensity != particleDensity) {
            rMax = requestedRMax;
            particleDensity = requestedParticleDensity;
            nParticles = calcParticleCount();
            recreatePointManager();
        }

        if (requestedMatrix != null) {
            boolean matrixSizeChanged = requestedMatrix.n != matrix.n;
            matrix = requestedMatrix;
            requestedMatrix = null;
            nextMatrixSize = matrix.n;
            if (matrixSizeChanged) {
                calcColors();
                respawn();
            }
        }

        if (nextMatrixSize != matrix.n) {
            requestReset();
        }

        if (requestedMatrixInitializerIndex != currentMatrixInitializerIndex) {
            currentMatrixInitializerIndex= requestedMatrixInitializerIndex;
            requestReset();
        }

        if (resetRequested) {
            resetRequested = false;
            reset();
        } else if (respawnRequested) {
            respawnRequested = false;
            respawn();
        }

        pm.recalculate();

        pointUpdater.setValues(rKern, rMax, forceFactor, friction, heat, boxWidth, boxHeight, dt);

        PointManager.AllIterator all = pm.getAllWithRelevant(wrapWorld);
        while (all.hasNext()) {
            pointUpdater.updateWithRelevant(all.next(), all.getRelevant(), matrix);
        }

        if (mousePressed) {
            // drag all particles in a specific radius

            for (Point point : pm.getRelevant(mouseX, mouseY, wrapWorld)) {
                Particle particle = (Particle) point;

                float dx = particle.x - mouseX;
                float dy = particle.y - mouseY;

                if (dx*dx + dy*dy < particleDragSelectionRadius*particleDragSelectionRadius) {

                    particle.vx = 0;
                    particle.vy = 0;

                    particle.x += pendingDragX;
                    particle.y += pendingDragY;
                }
            }

            pendingDragX = 0;
            pendingDragY = 0;
        }


        all = pm.getAll();
        while (all.hasNext()) {
            Particle particle = (Particle) all.next();

            particle.x += particle.vx * dt;
            particle.y += particle.vy * dt;

            if (wrapWorld) {

                particle.x = Helper.modulo(particle.x, boxWidth);
                particle.y = Helper.modulo(particle.y, boxHeight);

            } else {

                // stop particles at the boundaries

                if (particle.x < boxMargin) {
                    particle.x = boxMargin;
                    particle.vx = -particle.vx;
                } else if (particle.x > boxWidth - boxMargin) {
                    particle.x = boxWidth - boxMargin;
                    particle.vx = -particle.vx;
                }
                if (particle.y < boxMargin) {
                    particle.y = boxMargin;
                    particle.vy = -particle.vy;
                } else if (particle.y > boxHeight - boxMargin) {
                    particle.y = boxHeight - boxMargin;
                    particle.vy = -particle.vy;
                }
            }
        }

        setNewMousePos(mouseX, mouseY);// avoid former mouse position never being updated
    }

    public void draw(PGraphics context) {
        context.ellipseMode(context.RADIUS);

        drawParticles(context);

        if (drawParticleManager) {
            context.pushStyle();
            pm.draw(context);
            context.popStyle();
        }

        if (!camera.isFollowing() && Math.abs(camera.getScale() - 1) < 0.1f) {
            context.pushStyle();
            context.noFill();
            context.stroke(128);
            context.ellipse(mouseX, mouseY, particleDragSelectionRadius, particleDragSelectionRadius);
            if (!mousePressed) {
                context.stroke(64);
                context.ellipse(mouseX, mouseY, cameraFocusSelectionRadius, cameraFocusSelectionRadius);
            }
            context.popStyle();
        }

        if (drawForceDiagram) {
            context.pushStyle();
            drawForces(context);
            context.popStyle();
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
        PointManager.AllIterator all = pm.getAll();
        while (all.hasNext()) {
            Particle particle = (Particle) all.next();
            context.fill(getColor(particle.type));
            context.rect(particle.x, particle.y, particleSize, particleSize);
        }
        context.popStyle();
    }

    private int getColor(int type) {
        return typeColorMap.get(type);
    }

    private void drawForces(PGraphics context) {
        context.pushMatrix();

        float size = 20;

        context.translate(context.width - size * (matrix.n + 1), 0);

        context.translate(size/2, size/2);

        for (int type = 0; type < matrix.n; type++) {
            context.fill(typeColorMap.get(type));
            context.ellipse(size + type * size, 0, size / 2, size / 2);
            context.ellipse(0, size + type * size, size / 2, size / 2);
        }

        context.translate(size / 2, size / 2);
        context.textAlign(context.CENTER, context.CENTER);
        for (int i = 0; i < matrix.n; i++) {
            for (int j = 0; j < matrix.n; j++) {

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
        ), size * matrix.n, size * matrix.n + size);

        context.popMatrix();
    }

    public boolean shouldDrawRenderingStats() {
        return drawRenderingStats;
    }

    private void requestScreenshot() {
        screenshotRequested = true;
    }

    public boolean isScreenshotRequested() {
        return screenshotRequested;
    }
}
