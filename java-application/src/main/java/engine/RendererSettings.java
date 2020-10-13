package engine;

public class RendererSettings {

    public boolean paused;
    public boolean dtEnabled;
    public int spawnMode;
    public int matrixInitializer;
    public float particleSize;
    public float cameraFollowZoomFactor;

    public RendererSettings(boolean paused, boolean dtEnabled, int spawnMode, int matrixInitializer, float particleSize, float cameraFollowZoomFactor) {
        this.paused = paused;
        this.dtEnabled = dtEnabled;
        this.spawnMode = spawnMode;
        this.matrixInitializer = matrixInitializer;
        this.particleSize = particleSize;
        this.cameraFollowZoomFactor = cameraFollowZoomFactor;
    }
}
