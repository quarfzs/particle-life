package pointmanagement;

import processing.core.PGraphics;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * This class can be used for systems with many locally distributed objects (e.g. particles)
 * of which every object <b>only interacts with other objects in a small radius</b> ("radius of interaction").<br>
 * It internally organizes the objects in a grid of clusters.<br>
 */
public class PointManagerDefault implements PointManager {

    private static final float INITIAL_CAPACITY_FACTOR = 10;

    private ArrayList<Point>[] clusters;
    private int nx;
    private int ny;
    private float minX;
    private float maxX;
    private float minY;
    private float maxY;

    private ArrayList<Integer> relevantClusterIndicesBuffer = new ArrayList<>(9);

    private AllIterator allIterator = new AllIterator();
    private Iterable<Point> allIterable = () -> allIterator;
    private RelevantIterator relevantIterator = new RelevantIterator();
    private Iterable<Point> relevantIterable = () -> relevantIterator;

    /**
     * This constructor chooses a realistic number of columns and rows.<br>
     * More specifically, it chooses the number of columns and rows so
     * that their size is twice as large as the given radius:<br>
     * {@code nx = floor( (maxX - minX) / radius )}<br>
     * {@code ny = floor( (maxY - minY) / radius )}
     * @param gridSpacing width and height of each square in the grid
     *                   (in reality, the squares are rectangles but they won't be larger than that)
     * @param particleDensity average number of points per unit area
     * @param minX
     * @param maxX
     * @param minY
     * @param maxY
     */
    public PointManagerDefault(float gridSpacing, float particleDensity, float minX, float maxX, float minY, float maxY) {
        this(
                (int) Math.ceil((maxX - minX) / gridSpacing),
                (int) Math.ceil((maxY - minY) / gridSpacing),
                (int) Math.ceil(INITIAL_CAPACITY_FACTOR * particleDensity * gridSpacing * gridSpacing),
                minX, maxX, minY, maxY);
    }

    /**
     *
     * @param nx number of columns
     * @param ny number of rows
     * @param minX
     * @param maxX
     * @param minY
     * @param maxY
     */
    public PointManagerDefault(int nx, int ny, int initialCapacity, float minX, float maxX, float minY, float maxY) {
        this.nx = nx;
        this.ny = ny;
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;

        initCluster(initialCapacity);
    }

    private void initCluster(int initialCapacity) {
        clusters = new ArrayList[nx * ny];

        for (int i = 0; i < nx * ny; i++) {
            clusters[i] = new ArrayList<>(initialCapacity);
        }
    }

    public void add(Point p) {
        clusters[getClusterIndex(p)].add(p);
    }

    private int getClusterIndex(Point p) {
        return getClusterIndex(p.getX(), p.getY());
    }

    private int getClusterIndex(float x, float y) {
        return getClusterIndexFromClusterPos(getClusterX(x, true), getClusterY(y, true));
    }

    /**
     * if constrain is false, a cluster position may be returned that has no actual cluster
     */
    private int getClusterX(float x, boolean constrain) {
        int floor = (int) Math.floor(nx * (x - minX) / (maxX - minX));
        return constrain ? constrain(floor, 0, nx - 1) : floor;

    }

    private int getClusterY(float y, boolean constrain) {
        int floor = (int) Math.floor(ny * (y - minY) / (maxY - minY));
        return constrain ? constrain(floor, 0, ny - 1) : floor;
    }

    private int constrain(int a, int min, int max) {
        if (a < min) {
            return min;
        }
        if (a > max) {
            return max;
        }
        return a;
    }

    private int getClusterIndexFromClusterPos(int cx, int cy) {
        return cy * nx + cx;
    }

    public Iterable<Point> getAll() {
        allIterator.reset();
        return allIterable;
    }

    /**
     * This method returns all particles that can possibly lie inside the specified radius of interaction.
     * @param p
     * @param radius the radius of interaction (largest distance at which two particles interact)
     * @return iterable with all particles that could lie inside the specified radius of interaction
     */
    @Override
    public Iterable<Point> getRelevant(Point p, float radius, boolean wrapWorld) {
        loadRelevantClusters(p, radius, wrapWorld);

        relevantIterator.reset();// causes trouble with multi-threading
        return relevantIterable;
    }

    private void loadRelevantClusters(Point p, float radius, boolean wrapWorld) {
        int minClusterX = getClusterX(p.getX() - radius, !wrapWorld);
        int minClusterY = getClusterY(p.getY() - radius, !wrapWorld);
        int maxClusterX = getClusterX(p.getX() + radius, !wrapWorld);
        int maxClusterY = getClusterY(p.getY() + radius, !wrapWorld);

        relevantClusterIndicesBuffer.clear();
        for (int cx = minClusterX; cx <= maxClusterX; cx++) {
            for (int cy = minClusterY; cy <= maxClusterY; cy++) {
                int actualX = wrapWorld ? modulo(cx, nx) : cx;
                int actualY = wrapWorld ? modulo(cy, ny) : cy;
                relevantClusterIndicesBuffer.add(getClusterIndexFromClusterPos(actualX, actualY));
            }
        }
    }

    private int modulo(int a, int b) {
        return ((a % b) + b) % b;
    }

    private class RelevantIterator implements Iterator<Point> {
        private int clusterCounter;
        private int particleIndex;

        void reset() {
            clusterCounter = 0;
            particleIndex = 0;

            skipEmptyClusters();
        }

        @Override
        public boolean hasNext() {
            return clusterCounter < relevantClusterIndicesBuffer.size();
        }

        @Override
        public Point next() {
            ArrayList<Point> cluster = clusters[relevantClusterIndicesBuffer.get(clusterCounter)];
            Point p = cluster.get(particleIndex);

            particleIndex++;
            if (particleIndex >= cluster.size()) {
                particleIndex = 0;
                clusterCounter++;

                skipEmptyClusters();
            }

            return p;
        }

        private void skipEmptyClusters() {
            while (clusterCounter < relevantClusterIndicesBuffer.size() &&
                    clusters[relevantClusterIndicesBuffer.get(clusterCounter)].size() == 0) {
                clusterCounter++;
            }
        }
    }

    private class AllIterator implements Iterator<Point> {
        int currentClusterIndex;
        int currentParticleIndex;

        void reset() {
            currentClusterIndex = 0;
            currentParticleIndex = 0;
            skipEmptyClusters();
        }

        @Override
        public boolean hasNext() {
            return currentClusterIndex < clusters.length;
        }

        @Override
        public Point next() {
            ArrayList<Point> cluster = clusters[currentClusterIndex];
            Point p = cluster.get(currentParticleIndex);

            currentParticleIndex++;
            if (currentParticleIndex >= cluster.size()) {
                currentParticleIndex = 0;
                currentClusterIndex++;

                skipEmptyClusters();
            }

            return p;
        }

        private void skipEmptyClusters() {
            while (currentClusterIndex < clusters.length &&
                    clusters[currentClusterIndex].size() == 0) {
                currentClusterIndex++;
            }
        }
    }

    /**
     * You need to call this before every update step in order to get correct data from {@link #getRelevant(Point, float, boolean) getRelevant()}.
     */
    public void recalculate() {
        for (int clusterIndex = 0; clusterIndex < clusters.length; clusterIndex++) {

            ArrayList<Point> cluster = clusters[clusterIndex];

            for (int i = 0; i < cluster.size(); i++) {

                Point p = cluster.get(i);

                if (getClusterIndex(p) != clusterIndex) {

                    cluster.remove(p);
                    i--;  // don't skip the next particle in this cluster

                    add(p);
                }
            }
        }
    }

    /**
     * remove all points
     */
    public void clear() {
        for (ArrayList<Point> cluster : clusters) {
            cluster.clear();
        }
    }

    @Override
    public void draw(PGraphics context) {
        context.stroke(128);

        float xstep = (maxX - minX) / nx;
        float x = 0;
        for (int i = 0; i < nx-1; i++) {
            x += xstep;
            context.line(x, minY, x, maxY);
        }

        float ystep = (maxY - minY) / ny;
        float y = 0;
        for (int i = 0; i < ny - 1; i++) {
            y += ystep;
            context.line(minX, y, maxX, y);
        }
    }
}
