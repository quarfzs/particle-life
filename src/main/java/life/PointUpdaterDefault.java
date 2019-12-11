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
    public void updateWithRelevant(Point point, Iterable<Point> relevantNeighbors, Matrix matrix) {

        float interactionRadius2 = rMax * rMax;
        Particle particle = (Particle) point;

        for (Point point2 : relevantNeighbors) {
            Particle particle2 = (Particle) point2;
            if (particle != particle2) {

                float x2 = particle2.x;
                float y2 = particle2.y;

                if (x2 > particle.x) {
                    float wrappedX2 = x2 - boxWidth;
                    if (particle.x - wrappedX2 < x2 - particle.x) {
                        x2 = wrappedX2;
                    }
                } else {
                    float wrappedX2 = x2 + boxWidth;
                    if (wrappedX2 - particle.x < particle.x - x2) {
                        x2 = wrappedX2;
                    }
                }
                if (y2 > particle.y) {
                    float wrappedY2 = y2 - boxHeight;
                    if (particle.y - wrappedY2 < y2 - particle.y) {
                        y2 = wrappedY2;
                    }
                } else {
                    float wrappedY2 = y2 + boxHeight;
                    if (wrappedY2 - particle.y < particle.y - y2) {
                        y2 = wrappedY2;
                    }
                }

                // accelerate particle towards particle2
                float dx = x2 - particle.x;
                float dy = y2 - particle.y;
                float d2 = dx * dx + dy * dy;

                if (d2 > 0 && d2 < interactionRadius2) {

//                    float d = (float) Math.sqrt(d2);
                    float d = Helper.sqrt(d2, 5);

                    float a = forceFactor * getForce(d, rKern, rMax, matrix.get(particle.type, particle2.type));
                    float factor = a / d * currentDeltaT;
                    particle.vx += dx * factor;
                    particle.vy += dy * factor;
                }
            }
        }

        // friction force = -v * friction
        float oneMinusFrictionDt = 1 - friction * currentDeltaT;
        particle.vx *= oneMinusFrictionDt;
        particle.vy *= oneMinusFrictionDt;

        // add a little energy
        if (heat > 0) {
            particle.vx += (2 * random.nextFloat() - 1) * heat;
            particle.vy += (2 * random.nextFloat() - 1) * heat;
        }
    }

    private float getForce(float distance, float rKern, float rMax, float attraction) {

        if (distance < rKern) {
            return distance / rKern - 1;
        }

        if (distance < rMax) {
            return attraction * (1 - Math.abs(2 * distance - rKern - rMax) / (rMax - rKern));
        }

        return 0;
    }
}
