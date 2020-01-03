import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:p5/p5.dart';
import 'package:particle_life/life/color_maker.dart';

import 'life/matrix.dart';

typedef SelectionCallback = void Function(bool, int, int);

class MatrixSketch extends PPainter {

  static final int minColorBorderSize = 10;
  Matrix _matrix;

  // paint values
  num sx;
  num sy;
  num leftBorderSize;
  num topBorderSize;

  SelectionCallback _selectionCallback;
  bool _selectionActive;
  int _iSelected;
  int _jSelected;

  MatrixSketch(
      this._matrix,
      {
        bool active: false,
        int i: 0,
        int j: 0,
        SelectionCallback selectionCallback
      }):
        _selectionActive = active,
        _iSelected = i,
        _jSelected = j,
        _selectionCallback = selectionCallback,
        super();

  set matrix(Matrix matrix) {
    _matrix = matrix;
  }

  void setup() {
    fullScreen();
    noStroke();
  }

  @override
  void mousePressed() {
    _calcPaintValues();

    int i = ((mouseY - topBorderSize) / sy).floor();
    int j = ((mouseX - leftBorderSize) / sx).floor();

    if (i >= 0 && i < _matrix.n &&
        j >= 0 && j < _matrix.n) {

      bool changed = i == _iSelected && j == _jSelected;

      _iSelected = i;
      _jSelected = j;

      if (_selectionActive && changed) {
        _selectionActive = false;
      } else {
        _selectionActive = true;
      }
    } else {
      _selectionActive = false;
    }

    _selectionChanged();
  }

  void _selectionChanged() {

    redraw();

    if (_selectionCallback != null) {
      _selectionCallback(_selectionActive, _iSelected, _jSelected);
    }
  }

  void _calcPaintValues() {
    final int n = _matrix.n;
    if (n == 0) {
      return;
    }

    sx = ((paintSize.width - minColorBorderSize) / n).floor();
    sy = ((paintSize.height - minColorBorderSize) / n).floor();

    leftBorderSize = paintSize.width - sx * n;
    topBorderSize = paintSize.height - sy * n;
  }

  void draw() {

    final int n = _matrix.n;
    if (n == 0) {
      return;
    }

    background(Colors.transparent);

    _calcPaintValues();


    // draw color border
    noStroke();
    for (int i=0; i < n; i++) {
      fill(ColorMaker.compute(i / n));

      // top
      rect(leftBorderSize + i * sx, 0, sx, topBorderSize * 0.66);

      // left
      rect(0, topBorderSize + i * sy, leftBorderSize * 0.66, sy);
    }

    // draw values

    push();
    noStroke();
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < n; j++) {
        num val = _matrix.get(i, j);
        if (val < 0) {
          fill(HSVColor.fromColor(Colors.red).withValue(val.abs()).toColor());
        } else {
          fill(HSVColor.fromColor(Colors.green).withValue(val).toColor());
        }
        rect(leftBorderSize + j * sx, topBorderSize + i * sy, sx, sy);
      }
    }
    pop();

    // draw selection

    if (_selectionActive) {
      push();
      stroke(Colors.white);
      strokeWeight(2);
      noFill();
      rect(leftBorderSize + _jSelected * sx, topBorderSize + _iSelected * sy, sx, sy);
      pop();
    }
  }
}