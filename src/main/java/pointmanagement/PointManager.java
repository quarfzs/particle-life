package pointmanagement;

import processing.core.PGraphics;

public interface PointManager {

    void add(Point p);

    void clear();

    void recalculate();

    Iterable<Point> getAll();

    /**
     * Returns all points that are "relevant" for this point, meaning they possibly lie inside the given radius.
     *
     * @param radius the radius of relevance
     * @param wrap   whether to "wrap" the world on the edges like a torus
     */
    Iterable<Point> getRelevant(Point p, float radius, boolean wrap);

    void draw(PGraphics context);
}
