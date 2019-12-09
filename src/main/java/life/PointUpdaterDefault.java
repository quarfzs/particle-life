package life;

import pointmanagement.Point;
import pointmanagement.PointUpdater;

import java.util.Random;

public class PointUpdaterDefault implements PointUpdater {

    private Random random = new Random();

    private float rKern;
    private float rMax;
    private float forceFactor;
    private float friction;
    private float heat;
    private float boxWidth;
    private float boxHeight;
    private float currentDeltaT;

    /**
     * call this before calling updateWithRelevant()
     */
    void setValues(float rKern, float rMax, float forceFactor, float friction, float heat, float boxWidth, float boxHeight, float dt) {
        this.rKern = rKern;
        this.rMax = rMax;
        this.forceFactor = forceFactor;
        this.friction = friction;
        this.heat = heat;
        this.boxWidth = boxWidth;
        this.boxHeight = boxHeight;
        this.currentDeltaT = dt;
    }

    @Override
    public float getRelevantRadius(Point point) {
        return rMax;
    }

    @Override
    public void updateWithRelevant(Point point, Iterable<Point> relevantNeighbors) {

        // first, query data to not interfere with other threads
        float interactionRadius2 = rMax * rMax;
        Particle particle = (Particle) point;
        float x = particle.x;
        float y = particle.y;
        float vx = particle.vx;
        float vy = particle.vy;

        for (Point point2 : relevantNeighbors) {
            Particle particle2 = (Particle) point2;
            if (particle != particle2) {

                float x2 = particle2.x;
                float y2 = particle2.y;

                if (x2 > x) {
                    float wrappedX2 = x2 - boxWidth;
                    if (x - wrappedX2 < x2 - x) {
                        x2 = wrappedX2;
                    }
                } else {
                    float wrappedX2 = x2 + boxWidth;
                    if (wrappedX2 - x < x - x2) {
                        x2 = wrappedX2;
                    }
                }
                if (y2 > y) {
                    float wrappedY2 = y2 - boxHeight;
                    if (y - wrappedY2 < y2 - y) {
                        y2 = wrappedY2;
                    }
                } else {
                    float wrappedY2 = y2 + boxHeight;
                    if (wrappedY2 - y < y - y2) {
                        y2 = wrappedY2;
                    }
                }

                // accelerate particle towards particle2
                float dx = x2 - x;
                float dy = y2 - y;
                float d2 = dx * dx + dy * dy;

                if (d2 > 0 && d2 < interactionRadius2) {

                    float d = (float) Math.sqrt(d2);

                    float a = forceFactor * particle.attractionType.getForce(d, rKern, rMax, particle2.attractionType);
                    float factor = a / d * currentDeltaT;
                    vx += dx * factor;
                    vy += dy * factor;
                }
            }
        }

        // friction force = -v * friction
        float oneMinusFrictionDt = 1 - friction * currentDeltaT;
        vx *= oneMinusFrictionDt;
        vy *= oneMinusFrictionDt;

        // add a little energy
        vx += (2 * random.nextFloat() - 1) * heat;
        vy += (2 * random.nextFloat() - 1) * heat;

        // now write the data
        particle.vx = vx;
        particle.vy = vy;
    }
}
