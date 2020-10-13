package engine.requests;

import logic.Settings;

public class RequestSettings extends Request {

    public Settings settings;

    public RequestSettings(Settings settings) {
        this.settings = settings;
    }
}
