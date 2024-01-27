package nl.basmens.utils.maths;

import java.math.BigInteger;
import java.util.Locale;

public class Monomial implements Comparable {
  private BigInteger numerator;
  private BigInteger denominator;
  private int power = 1;

  // =================================================================================================================
  // Constructor
  // =================================================================================================================
  public Monomial() {
  }

  public Monomial(long coefficient, int power) {
    this.numerator = BigInteger.valueOf(coefficient);
    this.denominator = BigInteger.ONE;
    this.power = power;
  }

  public Monomial(long numerator, long denominator, int power) {
    this.numerator = BigInteger.valueOf(numerator);
    this.denominator = BigInteger.valueOf(denominator);
    this.power = power;

    simplifyFraction();
  }

  public Monomial(Monomial toCopy) {
    this.numerator = toCopy.numerator;
    this.denominator = toCopy.denominator;
    this.power = toCopy.power;
  }

  // =================================================================================================================
  // Math instance
  // =================================================================================================================
  public Monomial add(Monomial other) {
    if (power != other.power) {
      throw new IllegalArgumentException("ERROR: cant add Monomial because power is not the same");
    }

    // Math exact functions throw an ArithmeticException in case of an overflow
    numerator = numerator.multiply(other.denominator).add(other.numerator.multiply(denominator));
    denominator = denominator.multiply(other.denominator);

    simplifyFraction();
    return this;
  }

  public Monomial sub(Monomial other) {
    if (power != other.power) {
      throw new IllegalArgumentException("ERROR: cant subtract Monomial because power is not the same");
    }

    // Math exact functions throw an ArithmeticException in case of an overflow
    numerator = numerator.multiply(other.denominator).subtract(other.numerator.multiply(denominator));
    denominator = denominator.multiply(other.denominator);

    simplifyFraction();
    return this;
  }

  public Monomial mult(Monomial other) {
    // Math exact functions throw an ArithmeticException in case of an overflow
    numerator = numerator.multiply(other.numerator);
    denominator = denominator.multiply(other.denominator);
    power += other.power;

    simplifyFraction();
    return this;
  }

  public Monomial div(Monomial other) {
    // Math exact functions throw an ArithmeticException in case of an overflow
    numerator = numerator.multiply(other.denominator);
    denominator = denominator.multiply(other.numerator);
    power -= other.power;

    simplifyFraction();
    return this;
  }

  // =================================================================================================================
  // Math static
  // =================================================================================================================
  public static Monomial add(Monomial m1, Monomial m2) {
    return new Monomial(m1).add(m2);
  }

  public static Monomial sub(Monomial m1, Monomial m2) {
    return new Monomial(m1).sub(m2);
  }

  public static Monomial mult(Monomial m1, Monomial m2) {
    return new Monomial(m1).mult(m2);
  }

  public static Monomial div(Monomial m1, Monomial m2) {
    return new Monomial(m1).div(m2);
  }

  // =================================================================================================================
  // Functionality
  // =================================================================================================================
  private void simplifyFraction() {
    BigInteger gcd = numerator.gcd(denominator);
    numerator = numerator.divide(gcd);
    denominator = denominator.divide(gcd);
  }

  private void moveSignumToNumerator() {
    if (denominator.signum() == -1) {
      numerator = numerator.negate();
      denominator = denominator.negate();
    }
  }

  // =================================================================================================================
  // Getters
  // =================================================================================================================
  public static boolean isZero(Monomial m) {
    return m == null || m.numerator.equals(BigInteger.ZERO);
  }

  public static boolean isOne(Monomial m) {
    return m != null && m.power == 0 && m.numerator.equals(m.denominator);
  }

  public double getCoefficient() {
    return numerator.divide(denominator).doubleValue();
  }

  public int getPower() {
    return power;
  }

  public double evaluateOnT(double t) {
    return getCoefficient() * Math.pow(t, power);
  }

  @Override
  public String toString() {
    if (isZero(this)) {
      return "0";
    }

    return getCoefficientString() + getPowerString();
  }

  private String getCoefficientString() {
    // Unreachable code due to bug: isOne(this) should be nominator == 1
    if (power != 0) {
      if (numerator.equals(denominator)) {
        return "";
      }
      if (numerator.equals(denominator.negate())) {
        return "-";
      }
    }

    return String.format(Locale.ENGLISH, "%.4f", getCoefficient()).replaceAll("(\\.?)(0+)$", "");
  }

  private String getPowerString() {
    return switch (power) {
      case 0:
        yield "";
      case 1:
        yield "t";
      default:
        yield "t^" + power;
    };
  }

  @Override
  public int compareTo(Object o) {
    if (this == o) {
      return 0;
    }
    if (!(o instanceof Monomial)) {
      return 1;
    }
    
    Monomial other = (Monomial) o;
    if (power != other.power) {
      return power - other.power;
    }

    moveSignumToNumerator();
    other.moveSignumToNumerator();
    return numerator.multiply(other.denominator).compareTo(denominator.multiply(other.numerator));
  }
}
