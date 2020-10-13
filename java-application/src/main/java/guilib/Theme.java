package guilib;

public class Theme {

    interface ColorProvider {
        int color(int r, int g, int b);
        int color(int brightness);
    }

    private static Theme activeTheme;
    private static Theme darkTheme;
    private static Theme lightTheme;

    public static void init(ColorProvider colorProvider) {
        darkTheme = new Theme(colorProvider, true);
        lightTheme = new Theme(colorProvider, false);
        setDarkMode(true);
    }

    public static Theme getTheme() {
        return activeTheme;
    }

    public static void setDarkMode(boolean darkMode) {
        activeTheme = darkMode ? darkTheme : lightTheme;
    }

    public final boolean darkMode;

    public final int background;
    public final int backgroundContrast;
    public final int primary;
    public final int primaryContrast;
    public final int secondary;
    public final int secondaryContrast;
    public final int onColor;
    public final int offColor;
    public final int offColorContrast;
    public final int shadowColor;

    public Theme(ColorProvider c, boolean darkMode) {
        this.darkMode = darkMode;
        if (darkMode) {
            background = c.color(0);
            backgroundContrast = c.color(255);
            primary = c.color(246, 119, 62);
            primaryContrast = c.color(255);
            secondary = c.color(194, 48, 19);
            secondaryContrast = c.color(255);
            onColor = c.color(103, 152, 255);
            offColor = c.color(110);
            offColorContrast = c.color(255);
            shadowColor = c.color(255);
        } else {
            background = c.color(255);
            backgroundContrast = c.color(0);
            primary = c.color(255, 100, 90);
            primaryContrast = c.color(255);
            secondary = c.color(250, 170, 100);
            secondaryContrast = c.color(0);
            onColor = c.color(120, 150, 255);
            offColor = c.color(200);
            offColorContrast = c.color(0);
            shadowColor = c.color(0);
        }
    }
}
