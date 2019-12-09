package life;

import pointmanagement.Point;

public class Particle implements Point {
    float x;
    float y;
    float vx;
    float vy;
    AttractionType attractionType;

    public Particle(float x, float y, AttractionType attractionType) {
        this(x, y, 0, 0, attractionType);
    }

    public Particle(float x, float y, float vx, float vy, AttractionType attractionType) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.attractionType = attractionType;
    }

    @Override
    public float getX() {
        return x;
    }

    @Override
    public float getY() {
        return y;
    }
}
