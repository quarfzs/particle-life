package engine.requests;

public final class RequestWrap extends Request {

    public final boolean wrap;

    public RequestWrap(boolean wrap) {
        this.wrap = wrap;
    }
}
