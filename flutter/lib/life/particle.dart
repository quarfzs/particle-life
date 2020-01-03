import 'package:particle_life/pointmanagement/point.dart';

class Particle implements Point {

  num x;
  num y;
  num vx;
  num vy;
  int type;

  Particle(num x, num y, num vx, num vy, int type) {
    this.x = x;
    this.y = y;
    this.vx = vx;
    this.vy = vy;
    this.type = type;
  }
}