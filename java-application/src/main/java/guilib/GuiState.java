package guilib;

import logic.Settings;

/**
 * todo: use this class to store the state of the application.
 */
class GuiState {

    // particles
    public float[] positions;
    public float[] velocities;
    public int[] types;
    public Settings settings;

    // gui
    public boolean fullScreen = false;
    public int width = 1200;
    public int height = 800;
    public float cursorDragRadius = 50f;

    public GuiState() {
    }

    public static GuiState fromString(String src) {
        GuiState state = new GuiState();
        return state;
    }


    @Override
    public String toString() {
        return "";
    }
}
