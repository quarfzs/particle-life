package engine.requests;

import engine.RendererSettings;

public final class RequestRendererSettings extends Request {

    public final RendererSettings rendererSettings;

    public RequestRendererSettings(RendererSettings rendererSettings) {
        this.rendererSettings = rendererSettings;
    }
}
