package frontend;

import logic.Updater;
import processing.core.PGraphics;

import java.util.ArrayList;

public class Camera {

    private float scaleLerp = 10;
    private float focusLerp = 10;

    private float scale = 1;
    private float nextScale = 1;

    private boolean following = false;
    private static final int MAX_FOCUS_POOL_SIZE = 50;
    private static final int MIN_FOCUS_POOL_SIZE = 5;
    private static final float MAX_FOCUS_DEVIATION = 150;
    private ArrayList<Integer> focusPool = new ArrayList<>(MAX_FOCUS_POOL_SIZE);  // indices
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

    public void update(Updater updater, float dt) {
        float[] positions = updater.getPositions();

        if (following && focusPool.size() > 0) {
            nextFocusX = 0;
            nextFocusY = 0;
            for (int index : focusPool) {
                nextFocusX += positions[index * 2];
                nextFocusY += positions[index * 2 + 1];
            }

            nextFocusX /= focusPool.size();
            nextFocusY /= focusPool.size();

            // cancel following if focus pool is too spread out
            float xdev = 0;
            for (int index : focusPool) {
                float dx = positions[index * 2] - nextFocusX;
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

    float getScale() {
        return scale;
    }

    float getFocusX() {
        return focusX;
    }

    float getFocusY() {
        return focusY;
    }

    boolean isFollowing() {
        return following;
    }

    void startFollow(Updater updater, float x, float y, float radius, boolean wrap) {

        float[] positions = updater.getPositions();

        float r_2 = radius*radius;

        focusPool.clear();
        for (int index : updater.getRelevant(x, y, radius, wrap)) {

            if (focusPool.size() > MAX_FOCUS_POOL_SIZE) {
                break;
            }

            float dx = positions[index * 2] - x;
            float dy = positions[index * 2 + 1] - y;
            if (dx * dx + dy * dy < r_2) {
                focusPool.add(index);
            }
        }

        if (focusPool.size() >= MIN_FOCUS_POOL_SIZE) {
            following = true;
            nextScale = 2f;
        } else {
            focusPool.clear();
        }
    }

    void stopFollow() {
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
