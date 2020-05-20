package logic;

public interface Updater {

    void setTypes(int[] types);
    void setPositions(float[] positions);
    void setVelocities(float[] velocities);

    int[] getTypes();
    float[] getPositions();
    float[] getVelocities();

    /**
     * The returned array must at least contain all particles that lie inside the specified circle.
     * It may contain more (or even all) particles.
     * If wrap==true, wrapping must be considered.
     * @return array of relevant particle indices
     */
    int[] getRelevant(float x, float y, float radius, boolean wrap);

    void updateVelocities(Settings s, UpdaterLogic updaterLogic);
    void updatePositions(Settings s, UpdaterLogic updaterLogic);
}
