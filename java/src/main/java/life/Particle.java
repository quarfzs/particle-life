package life;

import pointmanagement.Point;

public class Particle implements Point {
    float x;
    float y;
    float vx;
    float vy;
    int type;

    public Particle(float x, float y, int type) {
        this(x, y, 0, 0, type);
    }

    public Particle(float x, float y, float vx, float vy, int type) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.type = type;
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
