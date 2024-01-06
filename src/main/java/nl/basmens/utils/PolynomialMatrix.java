package nl.basmens.utils;

public class PolynomialMatrix {
  private Polynomial[][] polynomials;

  // =================================================================================================================
  // Constructor
  // =================================================================================================================
  public PolynomialMatrix(int width, int height) {
    polynomials = new Polynomial[width][height];
    for (int i = 0; i < width; i++) {
      for (int j = 0; j < height; j++) {
        polynomials[i][j] = new Polynomial();
      }
    }
  }

  public PolynomialMatrix(PolynomialMatrix toCopy) {
    polynomials = new Polynomial[toCopy.width()][toCopy.height()];
    for (int i = 0; i < toCopy.width(); i++) {
      for (int j = 0; j < toCopy.height(); j++) {
        polynomials[i][j] = new Polynomial(toCopy.polynomials[i][j]);
      }
    }
  }

  // =================================================================================================================
  // Functions
  // =================================================================================================================

  private void subtractRows(int minuendRow, int subtrahendRow, Polynomial multiplier) {
    for (int i = 0; i < width(); i++) {
      set(i, minuendRow, get(i, minuendRow).sub(Polynomial.mult(get(i, subtrahendRow), multiplier)));
    }
  }

  private void multiplyRow(int row, Polynomial multiplier) {
    for (int i = 0; i < width(); i++) {
      set(i, row, Polynomial.mult(get(i, row), multiplier));
    }
  }

  // =================================================================================================================
  // Getters
  // =================================================================================================================

  public Polynomial get(int column, int row) {
    return polynomials[column][row];
  }

  public int width() {
    return polynomials.length;
  }

  public int height() {
    if (width() == 0) {
      return 0;
    }
    return polynomials[0].length;
  }

  public Polynomial getDeterminant() {
    if (width() != height()) {
      throw new IllegalArgumentException("ERROR: cannot calculate determinant, width and height are not the same");
    }
    if (width() == 0) {
      return new Polynomial();
    }

    PolynomialMatrix matrix = new PolynomialMatrix(this);
    Polynomial finalDivisor = new Polynomial(new Monomial(1, 0));

    for (int i = 0; i < matrix.width() - 1; i++) {
      for (int j = i + 1; j < matrix.height(); j++) {
        if (!matrix.get(i, j).isZero()) {
          if (matrix.get(i, i).isZero()) {
            matrix.subtractRows(i, j, new Polynomial(new Monomial(1, 0)));
            matrix.subtractRows(j, i, new Polynomial(new Monomial(-1, 0)));
          } else {
            Polynomial multiplier = matrix.get(i, j);
            matrix.multiplyRow(j, matrix.get(i, i));
            matrix.subtractRows(j, i, multiplier);
            finalDivisor.mult(matrix.get(i, i));
          }
        }
      }
    }

    Polynomial result = matrix.get(0, 0);
    for (int i = 1; i < matrix.width(); i++) {
      result = Polynomial.mult(result, matrix.get(i, i));
    }

    return Polynomial.div(result, finalDivisor);
  }

  @Override
  public String toString() {
    StringBuilder stringBuilder = new StringBuilder();

    for (int r = 0; r < height(); r++) {
      stringBuilder.append(",\n[");

      for (int c = 0; c < width(); c++) {
        String p = get(c, r).toString();

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
  // Setters
  // =================================================================================================================

  public void set(int column, int row, Polynomial polynomial) {
    polynomials[column][row] = polynomial;
  }
}
