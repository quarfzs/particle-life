package pointmanagement;

import life.Matrix;

public interface PointUpdater {
    void updateWithRelevant(Point point, Iterable<Point> relevantNeighbors, Matrix matrix);
}
