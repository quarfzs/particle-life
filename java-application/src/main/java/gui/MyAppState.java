package gui;

import engine.MatrixParser;
import engine.Particles;
import engine.RendererSettings;
import guilib.AppState;
import logic.Settings;
import processing.data.JSONObject;

public class MyAppState implements AppState {

    public Particles particles;
    public Settings settings;
    public RendererSettings rendererSettings;
    public boolean darkMode;

    public MyAppState(Particles particles, Settings settings, RendererSettings rendererSettings, boolean darkMode) {
        this.particles = particles;
        this.settings = settings;
        this.rendererSettings = rendererSettings;
        this.darkMode = darkMode;
    }

    public MyAppState() {
    }

    @Override
    public void loadFromString(String src) {
        JSONObject data = JSONObject.parse(src);

        readParticlesJson((JSONObject) data.get("particles"));
        readSettingsJson((JSONObject) data.get("settings"));
        readRendererSettingsJson((JSONObject) data.get("rendering"));
        darkMode = data.getBoolean("darkmode");
    }

    @Override
    public String storeToString() {

        JSONObject data = new JSONObject();

        data.put("settings", createSettingsJson());
        data.put("rendering", createRendererSettingsJson());
        data.put("darkmode", darkMode);
        data.put("particles", createParticlesJson());

        return data.toString();
    }

    private JSONObject createParticlesJson() {

        JSONObject data = new JSONObject();

        data.put("ntypes", particles.nTypes);
        data.put("types", particles.types);
        data.put("positions", particles.positions);
        data.put("velocities", particles.velocities);

        return data;
    }

    private void readParticlesJson(JSONObject particlesData) {

        // requires all values to be present

        particles.nTypes = (int) particlesData.get("ntypes");
        particles.types = (int[]) particlesData.get("types");
        particles.positions = (float[]) particlesData.get("positions");
        particles.velocities = (float[]) particlesData.get("velocities");
    }

    private JSONObject createSettingsJson() {

        JSONObject data = new JSONObject();

        data.put("matrix", MatrixParser.matrixToString(settings.getMatrix()));
        data.put("force", settings.getForceFactor());
        data.put("friction", settings.getFriction());
        data.put("rmin", settings.getRMin());
        data.put("rmax", settings.getRMax());
        data.put("wrap", settings.isWrap());
        data.put("dt", settings.getDt());
        data.put("heat", settings.getHeat());

        return data;
    }

    private void readSettingsJson(JSONObject data) {

        // if a value is not present, the current value will be kept.

        if (data.hasKey("matrix")) settings.setMatrix(MatrixParser.parseMatrix(data.getString("matrix")));
        settings.setForceFactor(data.getFloat("force", settings.getForceFactor()));
        settings.setFriction(data.getFloat("friction", settings.getFriction()));
        settings.setRMin(data.getFloat("rmin", settings.getRMin()));
        settings.setRMax(data.getFloat("rmax", settings.getRMax()));
        settings.setWrap(data.getBoolean("wrap", settings.isWrap()));
        settings.setDt(data.getFloat("dt", settings.getDt()));
        settings.setHeat(data.getFloat("heat", settings.getHeat()));
    }

    private JSONObject createRendererSettingsJson() {

        JSONObject data = new JSONObject();

        data.put("paused", rendererSettings.paused);
        data.put("dtenabled", rendererSettings.dtEnabled);
        data.put("spawnmode", rendererSettings.spawnMode);
        data.put("matrixinitializer", rendererSettings.matrixInitializer);
        data.put("particlesize", rendererSettings.particleSize);

        return data;
    }

    private void readRendererSettingsJson(JSONObject data) {

        // if a value is not present, the current value will be kept.

        rendererSettings.paused = data.getBoolean("paused", rendererSettings.paused);
        rendererSettings.dtEnabled = data.getBoolean("dtenabled", rendererSettings.dtEnabled);
        rendererSettings.spawnMode = data.getInt("spawnmode", rendererSettings.spawnMode);
        rendererSettings.matrixInitializer = data.getInt("matrixinitializer", rendererSettings.matrixInitializer);
        rendererSettings.particleSize = data.getFloat("particlesize", rendererSettings.particleSize);
    }
}
