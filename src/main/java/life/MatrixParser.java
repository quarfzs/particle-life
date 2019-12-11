package life;

import java.util.ArrayList;

public final class MatrixParser {

    /**
     * Expects input to look like this:<br><br>
     * <code>
     *     0.1 0.2 -0.3<br>
     *     -0.1 0.4 0.1<br>
     *     1.0 -1.0 0.0
     * </code>
     * @return the parsed matrix, or null.
     */
    public static Matrix parseMatrix(String s) {
        String[] parts = s.split("\\s");
        ArrayList<Float> numbers = new ArrayList<>(parts.length);
        for (String p: parts) {
            float f;
            try {
                f = Float.parseFloat(p);
            } catch (NumberFormatException e) {
                continue;
            }
            numbers.add(f);
        }

        int n = (int) Math.sqrt(numbers.size());
        if (n > 0) {
            return new Matrix(n, (i, j) -> numbers.get(i * n + j));
        }
        return null;
    }

    public static String matrixToString(Matrix matrix) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < matrix.n; i++) {
            for (int j = 0; j < matrix.n - 1; j++) {
                sb.append(matrix.get(i, j));
                sb.append(" ");
            }
            sb.append(matrix.get(i, matrix.n - 1));
            sb.append("\n");
        }
        return sb.toString();
    }
}