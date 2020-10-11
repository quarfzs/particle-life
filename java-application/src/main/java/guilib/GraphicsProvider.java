package guilib;

import processing.core.PGraphics;

public interface GraphicsProvider {
    public PGraphics createGraphics(int w, int h);
}
