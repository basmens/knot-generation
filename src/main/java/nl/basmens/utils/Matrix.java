package nl.basmens.utils;

public class Matrix {
  private long[][] numerators;
  private long[][] denominators;

  // =================================================================================================================
  // Constructor
  // =================================================================================================================

  public Matrix(int width, int height) {
    numerators = new long[width][height];
    denominators = new long[width][height];

    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        denominators[x][y] = 1;
      }
    }
  }

  public Matrix copy() {
    Matrix result = new Matrix(0, 0);

    result.numerators = new long[width()][];
    result.denominators = new long[width()][];
    for (int i = 0; i < width(); i++) {
      result.numerators[i] = numerators[i].clone();
      result.denominators[i] = denominators[i].clone();
    }

    return result;
  }

  @Override
  public String toString() {
    StringBuilder stringBuilder = new StringBuilder();

    for (int r = 0; r < height(); r++) {
      stringBuilder.append(",\n[");

      for (int c = 0; c < width(); c++) {
        String p = String.valueOf(getCoefficient(c, r));

        if (p.length() == 1) {
          stringBuilder.append(" ");
        }

        stringBuilder.append(p);
        stringBuilder.append(", ");
      }

      stringBuilder.setLength(stringBuilder.length() - 2);
      stringBuilder.append("]");
    }

    return stringBuilder.substring(2);
  }

  // =================================================================================================================
  // Functions
  // =================================================================================================================

  private static void subtractRows(Matrix matrix, int minuendRow, int subtrahendRow, long multiplierNum,
      long multiplierDenum) {
    long[][] nums = matrix.numerators;
    long[][] denoms = matrix.denominators;
    for (int i = 0; i < matrix.height(); i++) {
      nums[minuendRow][i] *= denoms[subtrahendRow][i] * multiplierDenum;
      nums[minuendRow][i] -= nums[subtrahendRow][i] * denoms[minuendRow][i] * multiplierNum;
      denoms[minuendRow][i] *= denoms[subtrahendRow][i] * multiplierDenum;
      simplifyFraction(matrix, minuendRow, i);
    }
  }

  private static void simplifyFraction(Matrix matrix, int col, int row) {
    final long[] primeNumbers = { 2, 3, 5 };
    long[][] nums = matrix.numerators;
    long[][] denoms = matrix.denominators;
    for (long p : primeNumbers) {
      while (nums[col][row] % p == 0 && denoms[col][row] % p == 0) {
        nums[col][row] /= p;
        denoms[col][row] /= p;
      }
    }
  }

  // R1 * A2/A1

  // =================================================================================================================
  // Getters
  // =================================================================================================================

  public double getCoefficient(int column, int row) {
    return numerators[column][row] / (double) denominators[column][row];
  }

  public int width() {
    return numerators.length;
  }

  public int height() {
    if (width() == 0) {
      return 0;
    }
    return numerators[0].length;
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
        if (matrix.numerators[j][i] != 0) {
          if (matrix.numerators[i][i] == 0) {
            subtractRows(matrix, i, j, 1, 1);
          }
          subtractRows(matrix, j, i, matrix.numerators[j][i] * matrix.denominators[i][i],
              matrix.denominators[j][i] * matrix.numerators[i][i]);
        }
      }
    }

    double result = matrix.getCoefficient(0, 0);
    for (int i = 1; i < matrix.width(); i++) {
      result *= matrix.getCoefficient(i, i);
    }

    return -result;
  }

  // =================================================================================================================
  // Setters
  // =================================================================================================================

  public void set(int column, int row, long value) {
    numerators[column][row] = value;
    denominators[column][row] = 1;
  }

  public void set(int column, int row, long numerator, long denominator) {
    numerators[column][row] = numerator;
    denominators[column][row] = denominator;
  }

  public void add(int column, int row, long value) {
    numerators[column][row] += value * denominators[column][row];
  }
}
