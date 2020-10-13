package engine.requests;

public final class RequestDtEnabled extends Request {
    
    public final boolean dtEnabled;

    public RequestDtEnabled(boolean dtEnabled) {
        this.dtEnabled = dtEnabled;
    }
}
