package engine;

public class Matrix implements logic.Matrix {

    private final int n;
    private final float[][] m;

    /**
     * Use an initializer to generate a new matrix based on the values of another matrix.
     * @param from        this matrix will be passed to the {@link Initializer initializer}'s {@link Initializer#init(Matrix) init()} method.
     * @param initializer this will set the initial values of the matrix.
     */
    Matrix(logic.Matrix from, Initializer initializer) {
        this.n = from.size();
        initializer.init(from);
        m = new float[n][];
        for (int i = 0; i < n; i++) {
            m[i] = new float[n];
            for (int j = 0; j < n; j++) {
                m[i][j] = initializer.getValue(i, j);
            }
        }
    }

    /**
     * Works like {@link Matrix#Matrix(Matrix, Initializer)}, using an empty matrix of size n.
     * @param n           the size of the empty matrix that will be used.
     * @param initializer this will set the initial values of the matrix.
     */
    Matrix(int n, Initializer initializer) {
        this(new Matrix(n), initializer);
    }

    /**
     * Returns a matrix with 0 as values.
     * @param n size of the matrix.
     */
    Matrix(int n) {
        this.n = n;
        m = new float[n][];
        for (int i = 0; i < n; i++) {
            m[i] = new float[n];
            for (int j = 0; j < n; j++) {
                m[i][j] = 0;
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
        default void init(logic.Matrix from) {}
        float getValue(int i, int j);
    }
}
