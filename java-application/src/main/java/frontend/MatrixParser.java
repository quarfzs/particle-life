package frontend;

import java.util.ArrayList;
import java.util.Locale;

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
        return matrixToString(matrix, floatEncoderDefault);
    }

    public static String matrixToStringRoundAndFormat(Matrix matrix) {
        return matrixToString(matrix, floatEncoderRoundAndFormat);
    }

    private static String matrixToString(Matrix matrix, FloatEncoder floatEncoder) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < matrix.size(); i++) {
            for (int j = 0; j < matrix.size() - 1; j++) {
                sb.append(floatEncoder.encode(matrix.get(i, j)));
                sb.append(" ");
            }
            sb.append(floatEncoder.encode(matrix.get(i, matrix.size() - 1)));
            sb.append("\n");
        }
        return sb.toString();
    }

    private interface FloatEncoder {
        String encode(float f);
    }

    private static FloatEncoder floatEncoderDefault = f -> String.format(Locale.US, "%f", f);

    private static FloatEncoder floatEncoderRoundAndFormat = f -> String.format(Locale.US, "%4.1f", f);
}
