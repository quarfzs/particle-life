package engine.requests;

import logic.Settings;

public final class RequestSettings extends Request {

    public final Settings settings;

    public RequestSettings(Settings settings) {
        this.settings = settings;
    }
}
