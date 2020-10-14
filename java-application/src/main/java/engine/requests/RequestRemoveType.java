package engine.requests;

public final class RequestRemoveType extends Request {

    public final int index;
    public final boolean keepParticleCount;

    public RequestRemoveType(int index, boolean keepParticleCount) {
        this.index = index;
        this.keepParticleCount = keepParticleCount;
    }
}
