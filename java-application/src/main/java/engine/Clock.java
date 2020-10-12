package engine;

class Clock {

    private long inTime = -1;
    private final float[] lastTimes;
    private int lastTimeIndex = 0;
    private float avgDt;

    /**
     * @param n over how many values should the average be calculated? (default is 20)
     */
    public Clock(int n) {
        lastTimes = new float[n];
    }

    public Clock() {
        this(20);
    }

    public void in() {
        inTime = System.nanoTime();
    }

    public void out() {

        if (inTime != -1) {

            float dt = (System.nanoTime() - inTime) / 1000000.f;

            // put value in array
            lastTimes[lastTimeIndex] = dt;

            // step to next index in array
            lastTimeIndex = (lastTimeIndex += 1) % lastTimes.length;

            // calc average of array
            float sum = 0;
            for (float t : lastTimes) {
                sum += t;
            }
            this.avgDt = sum / lastTimes.length;
        }
    }

    public double getTime() {
        return avgDt;
    }
}
