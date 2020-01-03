package life;

import pointmanagement.Point;
import pointmanagement.PointManager;
import processing.core.PGraphics;

import java.util.ArrayList;

public class Camera {

    private float scaleLerp = 10;
    private float focusLerp = 10;

    private float scale = 1;
    private float nextScale = 1;

    private boolean following = false;
    private final int MAX_FOCUS_POOL_SIZE = 50;
    private final int MIN_FOCUS_POOL_SIZE = 5;
    private final float MAX_FOCUS_DEVIATION = 150;
    private ArrayList<Point> focusPool = new ArrayList<>(MAX_FOCUS_POOL_SIZE);
    private float focusX;
    private float focusY;
    private float nextFocusX;
    private float nextFocusY;

    private float centerX;
    private float centerY;

    public Camera(float centerX, float centerY) {
        this.centerX = centerX;
        this.centerY = centerY;
        nextFocusX = centerX;
        nextFocusY = centerY;
        focusX = nextFocusX;
        focusY = nextFocusY;
    }

    public void update(float dt) {
        if (following && focusPool.size() > 0) {
            nextFocusX = 0;
            nextFocusY = 0;
            for (Point p : focusPool) {
                nextFocusX += p.getX();
                nextFocusY += p.getY();
            }

            nextFocusX /= focusPool.size();
            nextFocusY /= focusPool.size();

            // cancel following if focus pool is too spread out
            float xdev = 0;
            for (Point p : focusPool) {
                float dx = p.getX() - nextFocusX;
                xdev += dx*dx;
            }
            if (Math.sqrt(xdev / focusPool.size()) > MAX_FOCUS_DEVIATION) {
                stopFollow();
            }
        }

        lerp(dt);
    }

    private void lerp(float dt) {
        scale = nextScale + (scale - nextScale) * (float) Math.exp(-scaleLerp * dt);
        float f = (float) Math.exp(-focusLerp * dt);
        focusX = nextFocusX + (focusX - nextFocusX) * f;
        focusY = nextFocusY + (focusY - nextFocusY) * f;
    }

    public float getScale() {
        return scale;
    }

    public float getFocusX() {
        return focusX;
    }

    public float getFocusY() {
        return focusY;
    }

    public boolean isFollowing() {
        return following;
    }

    public void startFollow(PointManager pm, float x, float y, float radius, boolean wrapWorld) {

        float r_2 = radius*radius;

        focusPool.clear();
        for (Object o: pm.getRelevant(x, y, wrapWorld)) {
            if (focusPool.size() > MAX_FOCUS_POOL_SIZE) {
                break;
            }
            Point p = (Point) o;
            float dx = p.getX() - x;
            float dy = p.getY() - y;
            if (dx*dx + dy*dy < r_2) {
                focusPool.add(p);
            }
        }

        if (focusPool.size() >= MIN_FOCUS_POOL_SIZE) {
            following = true;
            nextScale = 2f;
        } else {
            focusPool.clear();
        }
    }

    public void stopFollow() {
        following = false;
        nextFocusX = centerX;
        nextFocusY = centerY;
        nextScale = 1f;
    }

    public void apply(PGraphics context) {
        context.translate(context.width/2f, context.height/2f);
        context.scale(getScale());
        context.translate(-getFocusX(), -getFocusY());
    }
}
