package nl.basmens.utils.maths;

import java.util.ArrayList;

import nl.basmens.Main;
import nl.basmens.utils.concurrent.PerformanceTimer;

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

  private void subtractRows(int minuendRow, int subtrahendRow, int beginColumn, Polynomial multiplier) {
    for (int i = beginColumn; i < width(); i++) {
      set(i, minuendRow, get(i, minuendRow).sub(Polynomial.mult(get(i, subtrahendRow), multiplier)));
    }
  }

  private void multiplyRow(int row, int beginColumn, Polynomial multiplier) {
    for (int i = beginColumn; i < width(); i++) {
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

  public Polynomial getDeterminant(long startTime) {
    if (width() != height()) {
      throw new IllegalArgumentException("ERROR: cannot calculate determinant, width and height are not the same");
    }
    if (width() == 0) {
      return new Polynomial();
    }

    PerformanceTimer timer = new PerformanceTimer(getClass(), "getDeterminant", "mold matrix");

    // Arithmetic to set lower left corner to 0, so that the resulting determinant
    // equals the product of the diagonal:
    // [n00, n10, n20, n30, n40],
    // [0, n11, n21, n31, n41],
    // [0, 0, n22, n32, n42],
    // [0, 0, 0, n33, n43],
    // [0, 0, 0, 0, n44]
    // det = n00 * n11 * n22 * n33 * n44
    PolynomialMatrix matrix = new PolynomialMatrix(this);
    ArrayList<Polynomial> finalDivisors = new ArrayList<>();
    // System.out.println();
    // System.out.println();
    // System.out.println();
    // System.out.println(matrix);

    for (int col = 0; col < matrix.width() - 1; col++) {
      boolean isDiagonalZero = matrix.get(col, col).isZero();

      for (int row = col + 1; row < matrix.height(); row++) {
        if (System.currentTimeMillis() - startTime > Main.MAX_CALC_TIME_PER_INVARIANT) {
          timer.stop();
          throw new RuntimeException("Max calculation time exceeded in alexander polynomial");
        }

        if (!matrix.get(col, row).isZero()) {
          if (isDiagonalZero) {
            matrix.subtractRows(col, row, 0, new Polynomial(new Monomial(1, 0)));
            matrix.subtractRows(row, col, 0, new Polynomial(new Monomial(-1, 0)));
            isDiagonalZero = false;
          } else {
            Monomial lowestMonomial = matrix.get(col, col).getLowestMonomial();
            Polynomial multiplier = Polynomial.div(matrix.get(col, col), lowestMonomial);
            Polynomial ratio = Polynomial.div(matrix.get(col, row), lowestMonomial);
            if (!multiplier.isOne()) {
              matrix.multiplyRow(row, col + 1, multiplier);
              finalDivisors.add(multiplier);
            }
            matrix.subtractRows(row, col, col + 1, ratio);
          }

          // System.out.println();
          // System.out.println();
          // System.out.println();
          // System.out.println(matrix);
        }
      }
    }

    timer.nextSegment("multiply diagonals");
    // Multiply the diagonal and divide by the final divisors in parts to make long
    // overflows less likely to occur
    Polynomial result = new Polynomial(new Monomial(1, 0));
    Polynomial remainder = new Polynomial();
    Polynomial devisor = new Polynomial(new Monomial(1, 0));
    int dividerIndex = 0;
    for (int i = 0; i < matrix.width(); i++) {
      result.mult(matrix.get(i, i));
      remainder.mult(matrix.get(i, i));
      result.add(Polynomial.divWithRemainder(remainder, devisor, remainder));
      // System.out.println("matrix: " + matrix.get(i, i));
      // System.out.println("result: " + result);
      // System.out.println("remainder: " + remainder);

      while (remainder.isZero() && dividerIndex < finalDivisors.size()) {
        devisor = finalDivisors.get(dividerIndex);
        result.divWithRemainder(devisor, remainder);
        dividerIndex++;
        // System.out.println("result: " + result);
        // System.out.println("remainder: " + remainder);
      }
    }

    if (!remainder.isZero()) {
      timer.stop();
      throw new RuntimeException("Determinant calculation in PolynomialMatrix resulted in a remainder");
    }

    timer.stop();
    return result;
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
    // StringBuilder stringBuilder = new StringBuilder();

    // for (int r = 0; r < height(); r++) {
    // stringBuilder.append(",{");

    // for (int c = 0; c < width(); c++) {
    // String p = get(c, r).toString();
    // stringBuilder.append(p);
    // stringBuilder.append(",");
    // }

    // stringBuilder.setLength(stringBuilder.length() - 1);
    // stringBuilder.append("}");
    // }

    // return "{" + stringBuilder.substring(1) + "}";
  }

  // =================================================================================================================
  // Setters
  // =================================================================================================================

  public void set(int column, int row, Polynomial polynomial) {
    polynomials[column][row] = polynomial;
  }
}
