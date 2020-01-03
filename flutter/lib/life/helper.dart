import 'dart:math';

class Helper {
  static final _random = Random();

  static num modulo(num a, num b) {
    return ((a % b) + b) % b;
  }

  static num uniform(num a, num b) {
    return a + (b - a) * _random.nextDouble();
  }
}