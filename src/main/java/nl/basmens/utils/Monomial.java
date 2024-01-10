package nl.basmens.utils;

import java.math.BigInteger;
import java.util.Locale;

public class Monomial {
  private BigInteger numerator;
  private long denominator = 1;
  private int power = 1;

  // =================================================================================================================
  // Constructor
  // =================================================================================================================
  public Monomial() {
  }

  public Monomial(long coefficient, int power) {
    this.numerator = BigInteger.valueOf(coefficient);
    this.power = power;
  }

  public Monomial(long numerator, long denominator, int power) {
    this.numerator = BigInteger.valueOf(numerator);
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

    // Math exact functions throw an ArithmeticException in case of an overflow
    numerator = numerator.multiply(BigInteger.valueOf(other.denominator));
    numerator = numerator.add(other.numerator.multiply(BigInteger.valueOf(denominator)));
    denominator = Math.multiplyExact(denominator, other.denominator);

    simplifyFraction();
    return this;
  }

  public Monomial sub(Monomial other) {
    if (power != other.power) {
      throw new IllegalArgumentException("ERROR: cant subtract Monomial because power is not the same");
    }

    // Math exact functions throw an ArithmeticException in case of an overflow
    numerator = numerator.multiply(BigInteger.valueOf(other.denominator));
    numerator = numerator.subtract(other.numerator.multiply(BigInteger.valueOf(denominator)));
    denominator = Math.multiplyExact(denominator, other.denominator);

    simplifyFraction();
    return this;
  }

  public Monomial mult(Monomial other) {
    // Math exact functions throw an ArithmeticException in case of an overflow
    numerator = numerator.multiply(other.numerator);
    denominator = Math.multiplyExact(denominator, other.denominator);
    power += other.power;

    simplifyFraction();
    return this;
  }

  public Monomial div(Monomial other) {
    // Math exact functions throw an ArithmeticException in case of an overflow
    numerator = numerator.multiply(BigInteger.valueOf(other.denominator));

    BigInteger newDenominator = other.numerator.multiply(BigInteger.valueOf(denominator));
    BigInteger gcd = newDenominator.gcd(numerator);
    numerator = numerator.divide(gcd);
    newDenominator = newDenominator.divide(gcd);

    if (newDenominator.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0
        || newDenominator.compareTo(BigInteger.valueOf(Long.MIN_VALUE)) < 0) {
      throw new ArithmeticException("Denominator in Monomial is outside the range of a long");
    }
    
    denominator = newDenominator.longValue();

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
      while (numerator.mod(BigInteger.valueOf(p)).intValueExact() == 0 && denominator % p == 0) {
        numerator = numerator.divide(BigInteger.valueOf(p));
        denominator /= p;
      }
    }
  }

  // =================================================================================================================
  // Getters
  // =================================================================================================================
  public static boolean isZero(Monomial m) {
    return m == null || m.numerator.equals(BigInteger.ZERO);
  }

  public static boolean isOne(Monomial m) {
    return m != null && m.power == 0 && m.numerator.equals(BigInteger.valueOf(m.denominator));
  }

  public static boolean isMinusOne(Monomial m) {
    return m != null && m.power == 0 && m.numerator.equals(BigInteger.valueOf(-m.denominator));
  }

  public double getCoefficient() {
    return numerator.divide(BigInteger.valueOf(denominator)).doubleValue();
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
    // if (power != 0) {
    //   if (isOne(this)) {
    //     return "";
    //   }
    //   if (isMinusOne(this)) {
    //     return "-";
    //   }
    // }

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
}
