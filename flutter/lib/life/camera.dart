import 'dart:math';

import 'package:p5/p5.dart';
import 'package:particle_life/pointmanagement/point.dart';
import 'package:particle_life/pointmanagement/point_manager.dart';

class Camera {

  static const int MAX_FOCUS_POOL_SIZE = 50;
  static const int MIN_FOCUS_POOL_SIZE = 5;
  static const num MAX_FOCUS_DEVIATION = 150;

  num scaleLerp = 10;
  num focusLerp = 10;

  num scale = 1;
  num nextScale = 1;

  bool following = false;

  List<Point> focusPool = new List();
  num focusX;
  num focusY;
  num nextFocusX;
  num nextFocusY;

  num centerX;
  num centerY;

  Camera(this.centerX, this.centerY) {
      nextFocusX = centerX;
      nextFocusY = centerY;
      focusX = nextFocusX;
      focusY = nextFocusY;
  }

  void update(num dt) {
      if (following && focusPool.length > 0) {
          nextFocusX = 0;
          nextFocusY = 0;
          for (Point p in focusPool) {
              nextFocusX += p.x;
              nextFocusY += p.y;
          }

          nextFocusX /= focusPool.length;
          nextFocusY /= focusPool.length;

          // cancel following if focus pool is too spread out
          num xdev = 0;
          for (Point p in focusPool) {
              num dx = p.x - nextFocusX;
              xdev += dx*dx;
          }
          if (sqrt(xdev / focusPool.length) > MAX_FOCUS_DEVIATION) {
              stopFollow();
          }
      }

      lerp(dt);
  }

  void lerp(num dt) {
      scale = nextScale + (scale - nextScale) * exp(-scaleLerp * dt);
      num f = exp(-focusLerp * dt);
      focusX = nextFocusX + (focusX - nextFocusX) * f;
      focusY = nextFocusY + (focusY - nextFocusY) * f;
  }

  num getScale() {
      return scale;
  }

  num getFocusX() {
      return focusX;
  }

  num getFocusY() {
      return focusY;
  }

  bool isFollowing() {
      return following;
  }

  void startFollow(PointManager pm, num x, num y, num radius, bool wrapWorld) {

      num r_2 = radius*radius;

      focusPool.clear();
      for (Point p in pm.getRelevant(x, y, wrapWorld)) {
          if (focusPool.length > MAX_FOCUS_POOL_SIZE) {
              break;
          }

          num dx = p.x - x;
          num dy = p.y - y;
          if (dx*dx + dy*dy < r_2) {
              focusPool.add(p);
          }
      }

      if (focusPool.length >= MIN_FOCUS_POOL_SIZE) {
          following = true;
          nextScale = 2.0;
      } else {
          focusPool.clear();
      }
  }

  void stopFollow() {
      following = false;
      nextFocusX = centerX;
      nextFocusY = centerY;
      nextScale = 1.0;
  }

  void apply(PPainter context) {
      context.translate(centerX, centerY);
      context.scale(getScale(), getScale());
      context.translate(-getFocusX(), -getFocusY());
  }
}