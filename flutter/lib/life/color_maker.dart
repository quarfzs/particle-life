import 'package:flutter/painting.dart';

import 'helper.dart';

class ColorMaker {

  static final _rainbow = [
    [255,   0,   0],
    [255, 127,   0],
    [255, 255,   0],
    [  0, 255,   0],
    [  0,   0, 255],
    [ 75,   0, 130],
    [148,   0, 211],
  ];

  static Color compute(num fraction) {
    fraction *= _rainbow.length;
    int j1 = fraction.floor();
    int j2 = fraction.ceil();

    int i1 = Helper.modulo(j1, _rainbow.length);
    int i2 = Helper.modulo(j2, _rainbow.length);

    if (i1 == i2) {
      
      return Color.fromRGBO(_rainbow[i1][0], _rainbow[i1][1], _rainbow[i1][2], 1);

    }

    num f = (fraction - j1) / (j2 - j1);
    return Color.fromRGBO(
      _lerp(_rainbow[i1][0], _rainbow[i2][0], f).round(),
      _lerp(_rainbow[i1][1], _rainbow[i2][1], f).round(),
      _lerp(_rainbow[i1][2], _rainbow[i2][2], f).round(),
      1
    );
  }

  static num _lerp(num a, num b, num f) {
    return a + (b - a) * f;
  }
}