package nl.basmens.utils;

import java.util.Locale;

public class Monomial {
  private long numerator;
  private long denominator = 1;
  private int power = 1;

  // =================================================================================================================
  // Constructor
  // =================================================================================================================
  public Monomial() {
  }

  public Monomial(long coefficient, int power) {
    this.numerator = coefficient;
    this.power = power;
  }

  public Monomial(long numerator, long denominator, int power) {
    this.numerator = numerator;
    this.denominator = denominator;
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

    numerator *= other.denominator;
    numerator += other.numerator * denominator;
    denominator *= other.denominator;

    simplifyFraction();
    return this;
  }

  public Monomial sub(Monomial other) {
    if (power != other.power) {
      throw new IllegalArgumentException("ERROR: cant subtract Monomial because power is not the same");
    }

    numerator *= other.denominator;
    numerator -= other.numerator * denominator;
    denominator *= other.denominator;

    simplifyFraction();
    return this;
  }

  public Monomial mult(Monomial other) {
    numerator *= other.numerator;
    denominator *= other.denominator;
    power += other.power;

    simplifyFraction();
    return this;
  }

  public Monomial div(Monomial other) {
    numerator *= other.denominator;
    denominator *= other.numerator;
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
    final long[] primeNumbers = { 2, 3, 5, 7, 11 };
    for (long p : primeNumbers) {
      while (numerator % p == 0 && denominator % p == 0) {
        numerator /= p;
        denominator /= p;
      }
    }
  }

  // =================================================================================================================
  // Getters
  // =================================================================================================================
  public static boolean isZero(Monomial m) {
    return m == null || m.numerator == 0;
  }

  public double getCoefficient() {
    return numerator / (double) denominator;
  }

  public int getPower() {
    return power;
  }

  public double evaluateOnT(double t) {
    return getCoefficient() * Math.pow(t, power);
  }

  @Override
  public String toString() {
    if (numerator == 0) {
      return "0";
    }

    return getCoefficientString() + getPowerString();
  }

  private String getCoefficientString() {
    if (power != 0) {
      if (numerator == denominator) {
        return "";
      }
      if (numerator == -denominator) {
        return "-";
      }
    }

    if (numerator % denominator == 0) {
      return String.format(Locale.ENGLISH, "%d", numerator / denominator);
    } else {
      return String.format(Locale.ENGLISH, "%.4f", getCoefficient()).replaceAll("(,*)(0+)$", "");
    }
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
}
