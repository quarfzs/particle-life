package life;

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
}
