package logic;

public final class Settings {

    float rangeX = 400;
    float rangeY = 400;
    float friction = 9;
    float heat = 0;
    float rMin = 10;
    float rMax = 40;
    float forceFactor = 950;
    boolean wrap = true;
    float dt = 0.02f;
    Matrix matrix;

    // variables for optimization:
    float rMaxSquared;
    float oneMinusFrictionDt;

    public Settings() {
        // calculate variables for optimization
        calcOneMinusFrictionDt();
        calcRMaxSquared();
    }

    private void calcOneMinusFrictionDt() {
        this.oneMinusFrictionDt = Math.max(0, 1 - this.friction * this.dt);
    }

    private void calcRMaxSquared() {
        this.rMaxSquared = this.rMax * this.rMax;
    }

    public void setRange(float rangeX, float rangeY) {
        this.rangeX = rangeX;
        this.rangeY = rangeY;
    }

    public void setFriction(float friction) {
        this.friction = friction;
        calcOneMinusFrictionDt();
    }

    public void setHeat(float heat) {
        this.heat = heat;
    }

    public void setRMin(float rMin) {
        this.rMin = rMin;
    }

    public void setRMax(float rMax) {
        this.rMax = rMax;
        calcRMaxSquared();
    }

    public void setForceFactor(float forceFactor) {
        this.forceFactor = forceFactor;
    }

    public void setWrap(boolean wrap) {
        this.wrap = wrap;
    }

    public void setDt(float dt) {
        this.dt = dt;
        calcOneMinusFrictionDt();
    }

    public void setMatrix(Matrix matrix) {
        this.matrix = matrix;
    }

    public float getRangeX() {
        return rangeX;
    }

    public float getRangeY() {
        return rangeY;
    }

    public float getFriction() {
        return friction;
    }

    public float getHeat() {
        return heat;
    }

    public float getRMin() {
        return rMin;
    }

    public float getRMax() {
        return rMax;
    }

    public float getForceFactor() {
        return forceFactor;
    }

    public boolean isWrap() {
        return wrap;
    }

    public float getDt() {
        return dt;
    }
    
    public Matrix getMatrix() {
        return matrix;
    }

    @Override
    public Settings clone() {
        Settings s = new Settings();

        s.rangeX = rangeX;
        s.rangeY = rangeY;
        s.friction = friction;
        s.heat = heat;
        s.rMin = rMin;
        s.rMax = rMax;
        s.forceFactor = forceFactor;
        s.wrap = wrap;
        s.dt = dt;
        s.matrix = matrix;
        s.rMaxSquared = rMaxSquared;
        s.oneMinusFrictionDt = oneMinusFrictionDt;

        return s;
    }
}
