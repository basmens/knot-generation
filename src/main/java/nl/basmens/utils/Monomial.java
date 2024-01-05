package nl.basmens.utils;

import java.util.Locale;

public class Monomial {
  private static final char VARIABLE_SYMBOL = 't';
  private long numerator = 0;
  private long denominator = 1;
  private int power = 1;

  // =================================================================================================================
  // Constructor
  // =================================================================================================================

  public Monomial() {
  } // for 0

  public Monomial(long coefficient, int power) {
    this.numerator = coefficient;
    this.power = power;
  }

  public Monomial(long numerator, long denominator, int power) {
    this.numerator = numerator;
    this.denominator = denominator;
    this.power = power;
  }

  public Monomial copy() {
    return new Monomial(numerator, denominator, power);
  }

  // =================================================================================================================
  // Math
  // =================================================================================================================

  public Monomial add(Monomial otherMonomial) {
    if (power != otherMonomial.power) {
      throw new IllegalArgumentException("ERROR: cant add Monomial because power is not the same");
    }

    long oldDenominator = denominator;
    numerator *= otherMonomial.denominator;
    denominator *= otherMonomial.denominator;
    numerator += otherMonomial.numerator * oldDenominator;
    return this;
  }

  public Monomial sub(Monomial otherMonomial) {
    if (power != otherMonomial.power) {
      throw new IllegalArgumentException("ERROR: cant subtract Monomial because power is not the same");
    }

    long oldDenominator = denominator;
    numerator *= otherMonomial.denominator;
    denominator *= otherMonomial.denominator;
    numerator -= otherMonomial.numerator * oldDenominator;
    return this;
  }

  public Monomial mult(Monomial otherMonomial) {
    numerator *= otherMonomial.numerator;
    denominator *= otherMonomial.denominator;

    power += otherMonomial.power;
    return this;
  }

  public Monomial div(Monomial otherMonomial) {
    numerator *= otherMonomial.denominator;
    denominator *= otherMonomial.numerator;

    power -= otherMonomial.power;
    return this;
  }

  // =================================================================================================================
  // Getters
  // =================================================================================================================

  public double getCoefficient() {
    return numerator / (double) denominator;
  }

  public int getPower() {
    return power;
  }

  public double getValue(double unknown) {
    return getCoefficient() * Math.pow(unknown, power);
  }

  @Override
  public String toString() {
    if (numerator == 0) {
      return "0";
    }

    return getCoefficientString() + getPowerString();
  }

  private String getCoefficientString() {
    if (numerator == denominator && power != 0) {
      return "";
    }
    if (numerator == -denominator && power != 0) {
      return "-";
    }

    if (numerator % denominator == 0) {
      return String.format(Locale.ENGLISH, "%d", numerator / denominator);
    } else {
      return String.format(Locale.ENGLISH, "%.4f", getCoefficient()).replaceAll("(,*)(0+)$", "");
    }
  }

  private String getPowerString() {
    if (power == 0) {
      return "";
    }
    if (power == 1) {
      return "" + VARIABLE_SYMBOL;
    }

    return VARIABLE_SYMBOL + "^" + power;
  }
}
