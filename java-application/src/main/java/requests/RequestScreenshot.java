package requests;

import processing.core.PGraphics;

public final class RequestScreenshot extends Request {

    public final PGraphics context;

    public RequestScreenshot(PGraphics context) {
        this.context = context;
    }
}
