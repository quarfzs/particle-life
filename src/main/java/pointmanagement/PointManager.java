package pointmanagement;

import processing.core.PGraphics;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * This class can be used for systems with many locally distributed objects (e.g. particles)
 * of which every object <b>only interacts with other objects in a small radius</b> ("radius of interaction").<br>
 * It internally organizes the objects in a grid of clusters.<br>
 */
public class PointManager {

    private static final float INITIAL_CAPACITY_FACTOR = 10;

    private PointCluster[] clusters;
    private int nx;
    private int ny;
    private float minX;
    private float maxX;
    private float minY;
    private float maxY;

    /**
     * This constructor chooses a realistic number of columns and rows.<br>
     * More specifically, it chooses the number of columns and rows so
     * that their size is twice as large as the given radius:<br>
     * {@code nx = floor( (maxX - minX) / radius )}<br>
     * {@code ny = floor( (maxY - minY) / radius )}
     * @param minGridSpacing minimum width and height of each rectangle in the grid
     * @param particleDensity average number of points per unit area
     */
    public PointManager(float minGridSpacing, float particleDensity, float minX, float maxX, float minY, float maxY) {
        this(
                (int) Math.floor((maxX - minX) / minGridSpacing),
                (int) Math.floor((maxY - minY) / minGridSpacing),
                (int) Math.ceil(INITIAL_CAPACITY_FACTOR * particleDensity * minGridSpacing * minGridSpacing),
                minX, maxX, minY, maxY);
    }

    /**
     *
     * @param nx number of columns
     * @param ny number of rows
     */
    public PointManager(int nx, int ny, int initialCapacity, float minX, float maxX, float minY, float maxY) {
        this.nx = nx;
        this.ny = ny;
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;

        initCluster(initialCapacity);
    }

    private void initCluster(int initialCapacity) {
        clusters = new PointCluster[nx * ny];

        for (int i = 0; i < nx * ny; i++) {
            clusters[i] = new PointCluster(initialCapacity);
        }
    }

    public void add(Point p) {
        clusters[getClusterIndex(p)].add(p);
    }

    private int getClusterIndex(Point p) {
        return getClusterIndexFromClusterPos(
                constrain(getClusterX(p.getX()), 0, nx - 1),
                constrain(getClusterY(p.getY()), 0, ny - 1)
        );
    }

    /**
     * if constrain is false, a cluster position may be returned that has no actual cluster
     */
    private int getClusterX(float x) {
        return (int) Math.floor(nx * (x - minX) / (maxX - minX));
    }

    private int getClusterY(float y) {
        return (int) Math.floor(ny * (y - minY) / (maxY - minY));
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

    private ArrayList<Integer> getRelevantClusterIndices(final int clusterX, final int clusterY, boolean wrapWorld) {

        ArrayList<Integer> relevantClusterIndices = new ArrayList<>(9);

        int minClusterX = clusterX - 1;
        int minClusterY = clusterY - 1;
        int maxClusterX = clusterX + 1;
        int maxClusterY = clusterY + 1;

        if (wrapWorld) {

            for (int cx = minClusterX; cx <= maxClusterX; cx++) {
                for (int cy = minClusterY; cy <= maxClusterY; cy++) {
                    relevantClusterIndices.add(getClusterIndexFromClusterPos(
                            modulo(cx, nx), modulo(cy, ny)
                    ));
                }
            }

        } else {

            minClusterX = constrain(minClusterX, 0, nx - 1);
            maxClusterX = constrain(maxClusterX, 0, nx - 1);
            minClusterY = constrain(minClusterY, 0, ny - 1);
            maxClusterY = constrain(maxClusterY, 0, ny - 1);

            for (int cx = minClusterX; cx <= maxClusterX; cx++) {
                for (int cy = minClusterY; cy <= maxClusterY; cy++) {
                    relevantClusterIndices.add(getClusterIndexFromClusterPos(cx, cy));
                }
            }
        }

        return relevantClusterIndices;
    }

    private int modulo(int a, int b) {
        return ((a % b) + b) % b;
    }

    /**
     * compute relevant points queried with getRelevant()
     * @param wrapWorld       whether to "wrap" the world on the edges like a torus
     */
    public AllIterator getAllWithRelevant(boolean wrapWorld) {
        return new AllIterator(wrapWorld, true);
    }

    public AllIterator getAll() {
        return new AllIterator(false, false);
    }

    public class AllIterator implements Iterator<Point> {

        private int clusterX = 0;
        private int clusterY = 0;
        private int clusterIndex = 0;
        private boolean hasNext = true;

        private Iterator<Point> insidePoints;
        private boolean wrapWorld;
        private boolean computeRelevant;

        private ArrayList<Point> relevantPoints = new ArrayList<>();

        private AllIterator(boolean wrapWorld, boolean computeRelevant) {
            this.wrapWorld = wrapWorld;
            this.computeRelevant = computeRelevant;
            insidePoints = clusters[clusterIndex].getPointsInside().iterator();
            if (!insidePoints.hasNext()) {
                step();
            } else {
                computeRelevant();
            }
        }

        @Override
        public boolean hasNext() {
            return hasNext;
        }

        @Override
        public Point next() {
            Point p = insidePoints.next();
            if (!insidePoints.hasNext()) {
                step();
            }
            return p;
        }

        private void step() {  // jump to next non-empty cluster
            do {
                increment();
                if (clusterIndex >= clusters.length) {
                    hasNext = false;
                    return;
                }
                insidePoints = clusters[clusterIndex].getPointsInside().iterator();

            } while (!insidePoints.hasNext());

            computeRelevant();
        }

        private void computeRelevant() {
            if (computeRelevant) {
                relevantPoints.clear();
                getRelevantClusterIndices(clusterX, clusterY, wrapWorld).forEach(
                        index -> clusters[index].getPointsInside().forEach(
                                point -> relevantPoints.add(point)
                        )
                );
            }
        }

        private void increment() {
            clusterX++;
            if (clusterX == nx) {
                clusterX = 0;
                clusterY++;
            }
            clusterIndex++;
        }

        /**
         * This method returns all particles that lie in the moore neighbourhood of the containing cluster.
         * @return iterable with all particles that could lie inside the specified radius of interaction
         */
        public ArrayList<Point> getRelevant() {
            return relevantPoints;
        }
    }

    public ArrayList<Point> getRelevant(float x, float y, boolean wrapWorld) {
        ArrayList<Point> points = new ArrayList<>();
        getRelevantClusterIndices(getClusterX(x), getClusterY(y), wrapWorld).forEach(
                index -> clusters[index].getPointsInside().forEach(
                        point -> points.add(point)
                )
        );
        return points;
    }

    /**
     * Call this before every update step in order to get correct "relevant" points.
     */
    public void recalculate() {
        for (int clusterIndex = 0; clusterIndex < clusters.length; clusterIndex++) {

            PointCluster cluster = clusters[clusterIndex];

            Iterator<Point> iterator = cluster.getPointsInside().iterator();
            while (iterator.hasNext()) {

                Point point = iterator.next();

                int newClusterIndex = getClusterIndex(point);

                if (newClusterIndex != clusterIndex) {
                    clusters[newClusterIndex].add(point);
                    iterator.remove();
                }
            }
        }
    }

    /**
     * remove all points
     */
    public void clear() {
        for (PointCluster cluster : clusters) {
            cluster.clear();
        }
    }

    public void draw(PGraphics context) {
        context.stroke(128);

        float xStep = (maxX - minX) / nx;
        float x = 0;
        for (int i = 0; i < nx-1; i++) {
            x += xStep;
            context.line(x, minY, x, maxY);
        }

        float yStep = (maxY - minY) / ny;
        float y = 0;
        for (int i = 0; i < ny - 1; i++) {
            y += yStep;
            context.line(minX, y, maxX, y);
        }
    }
}
