import 'dart:collection';

class AverageTracker {
  final _queue = Queue<double>();
  int maxSize;
  double emptyValue;


  AverageTracker(this.maxSize, this.emptyValue);

  void pushValue(double value) {
    _queue.addFirst(value);
    if (_queue.length > maxSize) {
      _queue.removeLast();
    }
  }

  double get average {
    if (_queue.length == 0) {
      return emptyValue;
    }

    double sum = 0;
    _queue.forEach((double e) {
      sum += e;
    });
    return sum / _queue.length;
  }
}