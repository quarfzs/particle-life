package engine;

public class RendererSettings {

    public boolean paused;
    public boolean dtEnabled;
    public int spawnMode;
    public int matrixInitializer;

    public RendererSettings(boolean paused, boolean dtEnabled, int spawnMode, int matrixInitializer) {
        this.paused = paused;
        this.dtEnabled = dtEnabled;
        this.spawnMode = spawnMode;
        this.matrixInitializer = matrixInitializer;
    }
}
