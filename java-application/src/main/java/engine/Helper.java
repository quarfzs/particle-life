package engine;

public class Helper {
    public static float modulo(float a, float b) {
        return ((a % b) + b) % b;
    }

    public static int modulo(int a, int b) {
        return ((a % b) + b) % b;
    }

    public static float uniform(float a, float b) {
        return a + (b - a) * (float) Math.random();
    }

    /**
     * Approximates the square root of a with Heron's method.
     * @return Approximation of square root of a.
     */
    public static float sqrt(float a, int iterations) {
        float x = 0.5f * (a + 1);
        for (int i = 0; i < iterations; i++) {
            x = 0.5f * (x + a / x);
        }
        return x;
    }
}
