package engine.requests;

import logic.Matrix;

public final class RequestMatrix extends Request {

    public final Matrix matrix;
    public final boolean keepParticleCount;

    public RequestMatrix(Matrix matrix, boolean keepParticleCount) {
        this.matrix = matrix;
        this.keepParticleCount = keepParticleCount;
    }
}
