package engine.requests;

import engine.Particles;

public class RequestParticles extends Request {

    public Particles particles;

    public RequestParticles(Particles particles) {
        this.particles = particles;
    }
}
