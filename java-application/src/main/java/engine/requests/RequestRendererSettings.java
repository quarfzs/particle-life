package engine.requests;

import engine.RendererSettings;

public class RequestRendererSettings extends Request {

    public RendererSettings rendererSettings;

    public RequestRendererSettings(RendererSettings rendererSettings) {
        this.rendererSettings = rendererSettings;
    }
}
