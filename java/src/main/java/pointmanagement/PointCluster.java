package pointmanagement;

import java.util.ArrayList;

/**
 * All points inside this cluster have the same relevant particles.
 */
public class PointCluster {

    private ArrayList<Point> insidePoints;

    PointCluster(int initialCapacity) {
        insidePoints = new ArrayList<>(initialCapacity);
    }

    public Iterable<Point> getPointsInside() {
        return insidePoints;
    }

    void add(Point point) {
        this.insidePoints.add(point);
    }

    void clear() {
        insidePoints.clear();
    }
}
