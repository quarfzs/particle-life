package life;

import pointmanagement.Point;
import pointmanagement.PointManager;
import pointmanagement.PointManagerDefault;
import processing.core.PGraphics;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
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

    private AttractionType[] attractionTypes = new AttractionType[6];

    private Random random = new Random();
    private ColorMaker colorMaker;

    private PointManager pm;
    private PointUpdaterDefault pointUpdater = new PointUpdaterDefault();

    private boolean resetRequested = false;
    private boolean respawnRequested = false;
    private float requestedRMax = rMax;
    private int requestedAttractionTypeCount = attractionTypes.length;
    private float requestedParticleDensity = particleDensity;

    private boolean drawParticleManager = false;
    private boolean drawRelevantAroundCurser = false;
    private boolean drawForceDiagram = false;
    private boolean drawRenderingStats = false;
    private Particle mockParticle = new Particle(0, 0, 0, 0, null);

    private float mouseX = 0;
    private float mouseY = 0;
    private float pendingDragX = 0;
    private float pendingDragY = 0;
    private boolean mousePressed = false;
    private Camera camera;
    private float cameraFocusSelectionRadius = 15f;

    private float particleDragSelectionRadius = 25f;

    private ArrayList<AttractionSetter> attractionSetters = new ArrayList<>();
    private ArrayList<String> attractionSetterNames = new ArrayList<>();
    private int currentAttractionSetterIndex = 0;
    private int requestedAttractionSetterIndex = currentAttractionSetterIndex;

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
        return new PointManagerDefault(rMax, particleDensity, 0, boxWidth, 0, boxHeight);
    }

    /**
     * create new point manager, but keep the particles
     */
    private void recreatePointManager() {
        PointManager oldPointManager = pm;
        pm = createNewPointManager();
        for (Point point : oldPointManager.getAll()) {
            pm.add(point);
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

        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setSize(350, 850);

        final Container pane = frame.getContentPane();
        pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));

        class GUIBuilder {

            private void addCheckBox(String label, boolean initialSate, OnStateChangedCallback onStateChangedCallback) {
                JCheckBox checkBox = new JCheckBox(label, initialSate);
                checkBox.addActionListener(e -> onStateChangedCallback.onStateChanged(checkBox.isSelected()));
                pane.add(checkBox);
            }

            private void addButton(String text, SimpleCallback onPressed) {
                JButton button = new JButton(text);
                button.addActionListener(e -> onPressed.call());
                pane.add(button);
            }

            private void addSlider(String label, int initialValue, int minValue, int maxValue,
                                   int minorTickSpacing, int majorTickSpacing,
                                   OnValueChangedCallback onValueChangedCallback) {
                pane.add(Box.createRigidArea(new Dimension(0, 10)));
                pane.add(new JLabel(label));
                JSlider slider = new JSlider(SwingConstants.HORIZONTAL, minValue, maxValue, initialValue);
                slider.setMajorTickSpacing(majorTickSpacing);
                slider.setMinorTickSpacing(minorTickSpacing);
                slider.setPaintTicks(true);
                slider.setPaintLabels(true);
                slider.addChangeListener(e -> onValueChangedCallback.onValueChanged(slider.getValue()));
                pane.add(slider);
            }
        }

        GUIBuilder c = new GUIBuilder();

        c.addSlider("Number of Types", attractionTypes.length, 1, 17, 0, 1, this::requestAttractionTypeCount);
        c.addSlider("Particle Density (in 1/1000s per pixel)", (int) (particleDensity * 1000), 0, 5, 0, 1, value -> requestedParticleDensity = value / 1000f);
        c.addSlider("Attraction Setter (enable diagram for better understanding)", currentAttractionSetterIndex, 0, attractionSetters.size() - 1, 0, 1, this::requestAttractionSetterIndex);
        c.addButton("New World", this::requestReset);
        c.addButton("Stir Up", this::requestRespawn);
        c.addSlider("rKern", (int) rKern, 0, 100, 10, 20, value -> rKern = value);
        c.addSlider("rMax ( > rKern!)", (int) rMax, 10, 100, 10, 20, this::requestNewRMax);
        c.addSlider("Heat", (int) heat, 0, 100, 10, 20, value -> heat = value);
        c.addSlider("Friction", (int) friction, 0, 100, 10, 20, value -> friction = value);
        c.addSlider("Force Factor", (int) forceFactor, 0, 10000, 500, 2000, value -> forceFactor = value);
        c.addSlider("Particle Size on Screen", (int) particleSize, 1, 5, 0, 1, value -> particleSize = value);
        c.addCheckBox("Wrap World", wrapWorld, state -> wrapWorld = state);
        c.addCheckBox("Draw Diagram", drawForceDiagram, state -> drawForceDiagram = state);
        c.addCheckBox("Draw Relevant Around Cursor", drawRelevantAroundCurser, state -> drawRelevantAroundCurser = state);
        c.addCheckBox("Draw Containers", drawParticleManager, state -> drawParticleManager = state);
        c.addCheckBox("Draw Rendering Stats", drawRenderingStats, state -> drawRenderingStats = state);
        c.addButton("Save Screenshot", this::requestScreenshot);

        frame.setVisible(true);
        frame.setLocationRelativeTo(null);  // center window on screen
        settingsJFrame = frame;
    }

    public void stop() {
        if (settingsJFrame != null) {
            settingsJFrame.dispose();
        }
    }

    private void init() {
        initAttractionSetters();
        setAttractionTypes();
        spawnParticles();
    }

    private void requestNewRMax(float newRMax) {
        requestedRMax = newRMax;
    }

    private void requestAttractionTypeCount(int count) {
        requestedAttractionTypeCount = count;
    }

    /**
     * use this if you want to call <code>reset()</code> from another Thread
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
        setAttractionTypes();
        spawnParticles();
        camera.stopFollow();
    }

    private void respawn() {
        pm.clear();
        spawnParticles();
        camera.stopFollow();
    }

    private void addAttractionSetter(String name, AttractionSetter a) {
        attractionSetterNames.add(name);
        attractionSetters.add(a);
    }

    private void initAttractionSetters() {

        addAttractionSetter("random f", (types) -> {
            for (AttractionType type : types) {
                for (AttractionType type2 : types) {
                    type.attractionRules.put(type2, Helper.uniform(-1, 1));
                }
            }
        });

        addAttractionSetter("equal pairs", (types) -> {
            for (AttractionType type : types) {
                for (AttractionType type2 : types) {
                    type.attractionRules.put(type2, Helper.uniform(-1, 1));
                }
            }

            for (AttractionType type : types) {
                // query attraction of every other and copy it
                for (AttractionType type2 : types) {
                    type.attractionRules.put(type2, type2.attractionRules.get(type));
                }
            }
        });

        addAttractionSetter("chains", (types) -> {
            for (int i = 0; i < types.length; i++) {

                AttractionType type = types[i];

                int before = Helper.modulo(i - 1, types.length);
                int after = Helper.modulo(i + 1, types.length);

                for (int j = 0; j < types.length; j++) {

                    float force;

                    if (j == i) {
                        force = 1f;
                    } else if (j == before) {
                        force = 0.0f;
                    } else if (j == after) {
                        force = 0.2f;
                    } else {
                        force = 0f;
                    }

                    type.attractionRules.put(types[j], force);
                }
            }
        });

        addAttractionSetter("random chains", (types) -> {
            for (int i = 0; i < types.length; i++) {

                AttractionType type = types[i];

                int before = Helper.modulo(i - 1, types.length);
                int after = Helper.modulo(i + 1, types.length);

                for (int j = 0; j < types.length; j++) {

                    float force;

                    if (j == i) {
                        force = Helper.uniform(0.2f, 1.0f);
                    } else if (j == before) {
                        force = 0;
                    } else if (j == after) {
                        force = 0.2f;
                    } else {
                        force = Helper.uniform(-0.0f, 0.0f);
                    }

                    type.attractionRules.put(types[j], force);
                }
            }
        });

        addAttractionSetter("medium clusters", (types) -> {
            for (AttractionType type : attractionTypes) {
                for (AttractionType type2 : attractionTypes) {
                    if (type2 == type) {
                        type.attractionRules.put(type2, Helper.uniform(0.1f, 0.2f));
                    } else {
                        if (Math.random() < 0.5f) {
                            type.attractionRules.put(type2, Helper.uniform(0.2f, 0.5f));
                        } else {
                            type.attractionRules.put(type2, -Helper.uniform(0.2f, 0.5f));
                        }
                    }
                }
            }
        });
    }

    private void setAttractionTypes() {

        for (int step = 0; step < attractionTypes.length; step++) {

            attractionTypes[step] = new AttractionType(colorMaker.hsb(
                    step / (float) attractionTypes.length,
                    1,
                    1
            ));
        }

        for (AttractionType type : attractionTypes) {
            for (AttractionType type2 : attractionTypes) {
                type.attractionRules.put(type2, 0f);
            }
        }

        attractionSetters.get(currentAttractionSetterIndex).set(attractionTypes);
    }

    private interface AttractionSetter {
        void set(AttractionType[] attractionTypes);
    }

    private void spawnParticles() {
        for (int i = 0; i < nParticles; i++) {

            float randomX = boxWidth * (float) Math.random();
            float randomY = boxHeight * (float) Math.random();

            AttractionType randomAttractionType = attractionTypes[random.nextInt(attractionTypes.length)];

            pm.add(new Particle(randomX, randomY, randomAttractionType));
        }
    }

    /**
     * use this if you want to call <code>setAttractionSetterIndex()</code> from another Thread
     */
    private void requestAttractionSetterIndex(int index) {
        requestedAttractionSetterIndex = index;
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
                if (camera.isFollowing()) {
                    camera.stopFollow();
                } else {
                    camera.startFollow(pm, mouseX, mouseY, cameraFocusSelectionRadius);
                }
                break;
        }
    }

    public void mouseMoved(float x, float y) {
        setNewMousePos(x, y);
    }

    public void mousePressed() {
        mousePressed = true;
    }

    public void mouseReleased() {
        mousePressed = false;
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

        if (requestedAttractionTypeCount != attractionTypes.length) {
            attractionTypes = new AttractionType[requestedAttractionTypeCount];
            requestReset();
        }

        if (requestedAttractionSetterIndex != currentAttractionSetterIndex) {
            currentAttractionSetterIndex = requestedAttractionSetterIndex;
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


        for (Point point : pm.getAll()) {
            pointUpdater.updateWithRelevant(point, pm.getRelevant(point, rMax, wrapWorld));
        }

        if (mousePressed) {
            // drag all particles in a specific radius

            mockParticle.x = mouseX;
            mockParticle.y = mouseY;

            for (Point point : pm.getRelevant(mockParticle, particleDragSelectionRadius, wrapWorld)) {
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

        for (Point p : pm.getAll()) {
            Particle particle = (Particle) p;

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

        if (drawRelevantAroundCurser) {
            context.pushStyle();
            context.noFill();
            context.stroke(255);
            mockParticle.x = mouseX;
            mockParticle.y = mouseY;
            for (Point p : pm.getRelevant(mockParticle, rMax, wrapWorld)) {
                context.ellipse(p.getX(), p.getY(), 5, 5);
            }
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

        context.pushStyle();
        context.textAlign(context.RIGHT, context.BOTTOM);
        context.fill(255);
        context.text(nParticles, context.width, context.height);
        context.popStyle();
    }

    public void drawParticles(PGraphics context) {
        context.pushStyle();
        context.noStroke();
        for (Point p : pm.getAll()) {
            Particle particle = (Particle) p;
            context.fill(particle.attractionType.color);
            context.rect(particle.x, particle.y, particleSize, particleSize);
        }
        context.popStyle();
    }

    private void drawForces(PGraphics context) {
        context.pushMatrix();

        float size = 20;

        context.translate(context.width - size*(attractionTypes.length+1), 0);

        context.translate(size/2, size/2);

        for (int i = 0; i < attractionTypes.length; i++) {
            context.fill(attractionTypes[i].color);
            context.ellipse(size + i*size, 0, size/2, size/2);
            context.ellipse(0, size + i*size, size/2, size/2);
        }

        context.translate(size/2, size/2);
        context.textAlign(context.CENTER, context.CENTER);
        for (int i = 0; i < attractionTypes.length; i++) {
            for (int j = 0; j < attractionTypes.length; j++) {

                float attraction = attractionTypes[i].attractionRules.get(attractionTypes[j]);

                if (attraction > 0) {
                    float c = 255 * attraction;
                    context.fill(0, c, 0);
                } else {
                    float c = -255 * attraction;
                    context.fill(c, 0, 0);
                }

                context.rect(j*size, i*size, size, size);

                context.fill(255);
                context.text(String.format("%.0f", attraction*10d), (j+0.5f)*size, (i+0.5f)*size);
            }
        }

        context.fill(255);
        context.textAlign(context.RIGHT);
        context.text(String.format("%s [%d]",
                attractionSetterNames.get(currentAttractionSetterIndex),
                currentAttractionSetterIndex),
                size*attractionTypes.length, size*attractionTypes.length + size);

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
