package engine;

/**
 * Data object that stores all information about the particles.
 */
public class Particles {

    public int nTypes;
    public int[] types;
    public float[] positions;
    public float[] velocities;

    public Particles(int nTypes, int[] types, float[] positions, float[] velocities) {
        this.nTypes = nTypes;
        this.types = types;
        this.positions = positions;
        this.velocities = velocities;
    }
}
