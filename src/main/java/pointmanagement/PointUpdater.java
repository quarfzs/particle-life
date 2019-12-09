package pointmanagement;

public interface PointUpdater {
    float getRelevantRadius(Point point);
    void updateWithRelevant(Point point, Iterable<Point> relevantNeighbors);
}
