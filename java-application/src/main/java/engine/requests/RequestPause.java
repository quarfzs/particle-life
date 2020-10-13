package engine.requests;

public final class RequestPause extends Request {
    
    public final boolean pause;

    public RequestPause(boolean pause) {
        this.pause = pause;
    }
}
