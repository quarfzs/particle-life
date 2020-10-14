package engine.requests;

public final class RequestMatrixSize extends Request {

    public final int size;
    public final boolean keepParticleCount;

    public RequestMatrixSize(int size, boolean keepParticleCount) {
        this.size = size;
        this.keepParticleCount = keepParticleCount;
    }
}
