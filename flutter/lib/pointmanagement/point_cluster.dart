import 'point.dart';

class PointCluster {
  List<Point> _insidePoints;

  PointCluster() {
    _insidePoints = List();
  }

  List<Point> getPointsInside() {
    return _insidePoints;
  }

  void add(Point point) {
    _insidePoints.add(point);
  }

  void clear() {
    _insidePoints.clear();
  }
}