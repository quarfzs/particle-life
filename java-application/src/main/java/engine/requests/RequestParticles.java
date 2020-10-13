package engine.requests;

import engine.Particles;

public final class RequestParticles extends Request {

    public final Particles particles;

    public RequestParticles(Particles particles) {
        this.particles = particles;
    }
}
