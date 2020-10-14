package engine.requests;

public final class RequestAddType extends Request {

    public final int index;
    public final boolean keepParticleCount;

    public RequestAddType(int index, boolean keepParticleCount) {
        this.index = index;
        this.keepParticleCount = keepParticleCount;
    }
}
