import 'dart:math';

import 'helper.dart';

class Matrix {
  final int n;
  List _m;

  Matrix(this.n, MatrixInitializer initializer) {
    initializer.init(n);
    this._m = List.generate(
      n,
      (i) => List.generate(n, (j) => initializer.getValue(i, j)),
      growable: false
    );
  }

  num get(int i, int j) {
    return _m[i][j];
  }

  void set(int i, int j, num val) {
    _m[i][j] = val;
  }
}

abstract class MatrixInitializer {
  void init(int n);
  num getValue(int i, int j);
}

class RandomInitializer implements MatrixInitializer {
  
  @override
  num getValue(int i, int j) {
    return Helper.uniform(-1, 1);
  }

  @override
  void init(int n) {
  }
}

class ChainsInitializer implements MatrixInitializer {
  int n;

  @override
  void init(int n) {
    this.n = n;
  }

  @override
  num getValue(int i, int j) {
    if (j == i) {
      return 1;
    } else if (j == Helper.modulo(i - 1, n)) {
      return 0;
    } else if (j == Helper.modulo(i + 1, n)) {
      return 0.2;
    }
    return 0;
  }
}

class RandomChainsInitializer implements MatrixInitializer {
  int n;

  @override
  void init(int n) {
    this.n = n;
  }

  @override
  num getValue(int i, int j) {
    if (j == i) {
      return Helper.uniform(0.2, 1.0);
    } else if (j == Helper.modulo(i - 1, n)) {
      return 0.0;
    } else if (j == Helper.modulo(i + 1, n)) {
      return 0.2;
    }
    return 0;
  }
}

class EqualPairsInitializer implements MatrixInitializer {
  Matrix m;

  @override
  void init(int n) {
    m = new Matrix(n, RandomInitializer());
  }

  @override
  num getValue(int i, int j) {
    return m.get(min(i, j), max(i, j));
  }
}