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

  private static void subtractRows(Matrix matrix, int minuendRow, int subtrahendRow, double multiplier) {
    for (int i = 0; i < matrix.height(); i++) {
      matrix.set(minuendRow, i, matrix.get(minuendRow, i) - matrix.get(subtrahendRow, i) * multiplier);
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
      for (int j = i + 1; j < matrix.width(); j++) {
        if (matrix.get(j, i) != 0) {
          if (matrix.get(i, i) == 0) {
            subtractRows(matrix, i, j, 1);
          }
          subtractRows(matrix, j, i, matrix.get(j, i) / matrix.get(i, i));
        }
      }
    }

    double result = matrix.get(0, 0);
    for (int i = 1; i < matrix.width(); i++) {
      result *= matrix.get(i, i);
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
