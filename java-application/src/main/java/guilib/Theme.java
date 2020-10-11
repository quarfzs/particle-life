package guilib;

public class Theme {

    interface ColorProvider {
        int color(int r, int g, int b);
        int color(int brightness);
    }

    private static Theme instance;

    public static void init(ColorProvider colorProvider) {
        instance = new Theme(colorProvider);
    }

    public static Theme getInstance() {
        return instance;
    }

    public Theme(ColorProvider c) {
        background = c.color(0);
        backgroundContrast = c.color(255);
        primary = c.color(255, 100, 90);
        primaryContrast = c.color(255);
        secondary = c.color(250, 170, 100);
        secondaryContrast = c.color(0);
    }

    public int background;
    public int backgroundContrast;
    public int primary;
    public int primaryContrast;
    public int secondary;
    public int secondaryContrast;
}
