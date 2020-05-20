package logic;

public interface UpdaterLogic {

    float[] updateVelocity(Settings s, float[] positions, int[] types, int index, float vx, float vy);

    float[] updatePosition(Settings s, float x, float y, float vx, float vy);
}
