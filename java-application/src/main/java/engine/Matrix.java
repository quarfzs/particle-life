package engine;

public class Matrix implements logic.Matrix {
    private final int n;
    private final float[][] m;

    Matrix(int n, Initializer initializer) {
        this.n = n;
        initializer.init(n);
        m = new float[n][];
        for (int i = 0; i < n; i++) {
            m[i] = new float[n];
            for (int j = 0; j < n; j++) {
                m[i][j] = initializer.getValue(i, j);
            }
        }
    }

    @Override
    public int size() {
        return n;
    }

    public float get(int i, int j) {
        return m[i][j];
    }

    public interface Initializer {
        default void init(int n) {};
        float getValue(int i, int j);
    }
}
