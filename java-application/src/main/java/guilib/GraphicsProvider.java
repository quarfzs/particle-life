package guilib;

import processing.core.PGraphics;

public interface GraphicsProvider {
    PGraphics createGraphics(int w, int h);
}
