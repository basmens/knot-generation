package nl.basmens.utils;

import java.util.Arrays;

public class Matrix {
  private double[][] values;

  // =================================================================================================================
  // Constructor
  // =================================================================================================================

  public Matrix(int width, int height) {
    values = new double[width][height];
  }

  public Matrix copy() {
    Matrix result = new Matrix(0, 0);

    result.values = new double[width()][];
    for (int i = 0; i < width(); i++) {
      result.values[i] = values[i].clone();
    }

    return result;
  }

  @Override
  public String toString() {
    return Arrays.deepToString(values);
  }

  // =================================================================================================================
  // Functions
  // =================================================================================================================

  private static void subtractRows(Matrix matrix, int minuendRow, int subtrahendRow, double multiplier,
      int startIndex) {
    for (int i = startIndex; i < matrix.width(); i++) {
      matrix.values[i][minuendRow] -= matrix.values[i][subtrahendRow] * multiplier;
    }
  }

  // =================================================================================================================
  // Getters
  // =================================================================================================================

  public double get(int column, int row) {
    return values[column][row];
  }

  public int width() {
    return values.length;
  }

  public int height() {
    if (width() == 0) {
      return 0;
    }
    return values[0].length;
  }

  public double getDeterminant() {
    if (width() != height()) {
      throw new IllegalArgumentException("ERROR: cannot calculate determinant, width and height are not the same");
    }
    if (width() == 0) {
      return 0;
    }

    Matrix matrix = this.copy();
    for (int i = 0; i < matrix.width() - 1; i++) {
      for (int j = i + 1; j < matrix.height(); j++) {
        if (values[i][j] != 0) {
          if (values[i][i] == 0) {
            subtractRows(matrix, i, j, 1, i);
          }
          subtractRows(matrix, j, i, matrix.get(i, j) / matrix.get(i, i), i + 1);
        }
      }
    }

    double result = 1;
    for (int i = 0; i < matrix.width(); i++) {
      result *= values[i][i];
    }
    return -result;
  }

  // =================================================================================================================
  // Setters
  // =================================================================================================================

  public void set(int column, int row, double value) {
    values[column][row] = value;
  }
}
