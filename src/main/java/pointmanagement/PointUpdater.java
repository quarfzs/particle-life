package pointmanagement;

public interface PointUpdater {
    void updateWithRelevant(Point point, Iterable<Point> relevantNeighbors);
}
