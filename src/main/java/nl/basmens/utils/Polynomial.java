package nl.basmens.utils;

public class Polynomial {
  private Monomial[] monomials = new Monomial[0];
  private int index0Power;

  private Monomial lowestMonomial;
  private Monomial highestMonomial;
  private boolean isLowestMonomialOutdated = true;
  private boolean isHighestMonomialOutdated = true;

  // =================================================================================================================
  // Constructor
  // =================================================================================================================
  public Polynomial() {
  }

  public Polynomial(Monomial... inputMonomials) {
    // Ensure range
    int lowest = Integer.MAX_VALUE;
    int highest = Integer.MIN_VALUE;
    for (Monomial m : inputMonomials) {
      if (!Monomial.isZero(m)) {
        lowest = Math.min(lowest, m.getPower());
        highest = Math.max(highest, m.getPower());
      }
    }
    ensureCapacityRange(lowest, highest);

    // Copy values into destination
    for (Monomial m : inputMonomials) {
      if (!Monomial.isZero(m)) {
        monomials[m.getPower() - index0Power] = m;
      }
    }
  }

  public Polynomial(Polynomial toCopy) {
    if (toCopy.isZero()) {
      return;
    }

    ensureCapacityRange(toCopy.getLowestMonomial().getPower(), toCopy.getHighestMonomial().getPower());
    for (Monomial m : toCopy.monomials) {
      if (!Monomial.isZero(m)) {
        monomials[m.getPower() - index0Power] = new Monomial(m);
      }
    }
  }

  private void moveSemanticOperation(Polynomial toMove) {
    monomials = toMove.monomials;
    index0Power = toMove.index0Power;
    lowestMonomial = toMove.lowestMonomial;
    highestMonomial = toMove.highestMonomial;
    isLowestMonomialOutdated = toMove.isLowestMonomialOutdated;
    isHighestMonomialOutdated = toMove.isHighestMonomialOutdated;
  }

  // =================================================================================================================
  // Math instance
  // =================================================================================================================
  public Polynomial add(Polynomial other) {
    if (other.isZero()) {
      return this;
    }

    ensureCapacityRange(other.getLowestMonomial().getPower(), other.getHighestMonomial().getPower());
    for (Monomial m : other.monomials) {
      if (!Monomial.isZero(m)) {
        getMonomial(m.getPower()).add(m);
      }
    }

    isLowestMonomialOutdated = true;
    isHighestMonomialOutdated = true;
    return this;
  }

  public Polynomial sub(Polynomial other) {
    if (other.isZero()) {
      return this;
    }

    ensureCapacityRange(other.getLowestMonomial().getPower(), other.getHighestMonomial().getPower());
    for (Monomial m : other.monomials) {
      if (!Monomial.isZero(m)) {
        getMonomial(m.getPower()).sub(m);
      }
    }

    isLowestMonomialOutdated = true;
    isHighestMonomialOutdated = true;
    return this;
  }

  public Polynomial mult(Monomial other) {
    if (Monomial.isZero(other)) {
      monomials = new Monomial[0];
      return this;
    }

    for (Monomial m : monomials) {
      if (!Monomial.isZero(m)) {
        m.mult(other);
      }
    }
    index0Power += other.getPower();

    isLowestMonomialOutdated = true;
    isHighestMonomialOutdated = true;
    return this;
  }

  public Polynomial mult(Polynomial other) {
    if (other.isZero()) {
      monomials = new Monomial[0];
      return this;
    }

    Polynomial result = Polynomial.mult(this, other);
    moveSemanticOperation(result);
    return this;
  }

  public Polynomial div(Monomial other) {
    if (Monomial.isZero(other)) {
      throw new IllegalArgumentException("ERROR: cannot divide by 0");
    }

    for (Monomial m : monomials) {
      if (!Monomial.isZero(m)) {
        m.div(other);
      }
    }
    index0Power -= other.getPower();
    return this;
  }

  public Polynomial div(Polynomial other) {
    moveSemanticOperation(Polynomial.div(this, other));
    return this;
  }

  public Polynomial divWithRemainder(Polynomial other, Polynomial outRemainder) {
    moveSemanticOperation(Polynomial.divWithRemainder(this, other, outRemainder));
    return this;
  }

  // =================================================================================================================
  // Math static
  // =================================================================================================================
  public static Polynomial mult(Polynomial p, Monomial m) {
    return new Polynomial(p).mult(m);
  }

  public static Polynomial mult(Polynomial p1, Polynomial p2) {
    if (p1.isZero() || p2.isZero()) {
      return new Polynomial();
    }

    // Create new Polynomial for the result with the required capacity
    Polynomial result = new Polynomial();
    result.ensureCapacityRange(p1.getLowestMonomial().getPower() + p2.getLowestMonomial().getPower(),
        p1.getHighestMonomial().getPower() + p2.getHighestMonomial().getPower());

    // Do the multiplication
    for (Monomial m1 : p1.monomials) {
      if (Monomial.isZero(m1)) {
        continue;
      }

      for (Monomial m2 : p2.monomials) {
        if (!Monomial.isZero(m2)) {
          Monomial product = Monomial.mult(m1, m2);
          result.getMonomial(product.getPower()).add(product);
        }
      }
    }
    return result;
  }

  public static Polynomial div(Polynomial p, Monomial m) {
    return new Polynomial(p).div(m);
  }

  public static Polynomial div(Polynomial numerator, Polynomial denominator) {
    Polynomial remainder = new Polynomial();
    Polynomial result = divWithRemainder(numerator, denominator, remainder);
    if (!remainder.isZero()) {
      throw new IllegalArgumentException(
          "ERROR: division results in " + result + " with " + remainder + " / " + denominator
              + " as rest. No rest value is permitted. If you want a remainder, use the divWithRemainder function");
    }
    return result;
  }

  public static Polynomial divWithRemainder(Polynomial numerator, Polynomial denominator, Polynomial outRemainder) {
    if (denominator.isZero()) {
      throw new IllegalArgumentException("ERROR: cannot divide by 0");
    }
    if (numerator.isZero()) {
      outRemainder.moveSemanticOperation(new Polynomial());
      return new Polynomial();
    }
    if (denominator.isOne()) {
      outRemainder.moveSemanticOperation(new Polynomial());
      return new Polynomial(numerator);
    }

    // Init result
    Polynomial result = new Polynomial();
    result.ensureCapacityRange(0,
        numerator.getHighestMonomial().getPower() - denominator.getHighestMonomial().getPower());

    // Init outRemainder into the given reference
    outRemainder.moveSemanticOperation(new Polynomial(numerator));

    // Make a copy in case denominator and outRemainder point to the same instance
    denominator = new Polynomial(denominator);

    while (outRemainder.getHighestMonomial().getPower() - outRemainder.getLowestMonomial().getPower() >= denominator
        .getHighestMonomial().getPower() - denominator.getLowestMonomial().getPower()) {
      Monomial multiplier = Monomial.div(outRemainder.getHighestMonomial(), denominator.getHighestMonomial());
      outRemainder.sub(Polynomial.mult(denominator, multiplier));

      result.getMonomial(multiplier.getPower()).add(multiplier);

      if (outRemainder.isZero()) {
        break;
      }
    }

    return result;
  }

  // =================================================================================================================
  // Functionality
  // =================================================================================================================
  private void ensureCapacityRange(int lowerPower, int higherPower) {
    if (index0Power <= lowerPower && monomials.length + index0Power > higherPower) {
      return;
    }

    // Expand requested range
    for (int i = 0; i < Math.min(lowerPower - index0Power, monomials.length); i++) {
      if (!Monomial.isZero(monomials[i])) {
        lowerPower = i + index0Power;
        break;
      }
    }
    for (int i = monomials.length - 1; i >= Math.max(higherPower - index0Power, 0); i--) {
      if (!Monomial.isZero(monomials[i])) {
        higherPower = i + index0Power;
        break;
      }
    }

    // Create new array with enough capacity
    Monomial[] result = new Monomial[higherPower - lowerPower + 1];

    // Copy old values into new array
    int shift = index0Power - lowerPower;
    for (int i = Math.max(lowerPower - index0Power, 0); i < Math.min(higherPower - index0Power + 1,
        monomials.length); i++) {
      result[i + shift] = monomials[i];
    }

    index0Power = lowerPower;
    monomials = result;
  }

  // =================================================================================================================
  // Getters
  // =================================================================================================================
  public double evaluateOnT(double t) {
    double result = 0;
    for (Monomial m : monomials) {
      if (!Monomial.isZero(m)) {
        result += m.evaluateOnT(t);
      }
    }
    return result;
  }

  private Monomial getMonomial(int power) {
    if (hasMonomial(power)) {
      if (monomials[power - index0Power] == null) {
        monomials[power - index0Power] = new Monomial(0, power);
      }
      return monomials[power - index0Power];
    }
    throw new IndexOutOfBoundsException("Power " + power + "is outside of the polynomials range of []" + index0Power
        + ", " + (monomials.length + index0Power - 1) + "]");
  }

  public boolean hasMonomial(int power) {
    return power - index0Power < monomials.length;
  }

  public boolean isZero() {
    return getLowestMonomial() == null;
  }

  public boolean isOne() {
    return getLowestMonomial() != null && getLowestMonomial() == getHighestMonomial()
        && Monomial.isOne(getLowestMonomial());
  }

  public Monomial getLowestMonomial() {
    if (isLowestMonomialOutdated) {
      lowestMonomial = null;
      for (Monomial m : monomials) {
        if (!Monomial.isZero(m)) {
          lowestMonomial = m;
          break;
        }
      }
      isLowestMonomialOutdated = false;
    }
    return lowestMonomial;
  }

  public Monomial getHighestMonomial() {
    if (isHighestMonomialOutdated) {
      highestMonomial = null;
      for (int i = monomials.length - 1; i >= 0; i--) {
        Monomial m = monomials[i];
        if (!Monomial.isZero(m)) {
          highestMonomial = m;
          break;
        }
      }
      isHighestMonomialOutdated = false;
    }
    return highestMonomial;
  }

  @Override
  public String toString() {
    if (isZero()) {
      return "0";
    }

    StringBuilder stringBuilder = new StringBuilder();
    for (Monomial m : monomials) {
      if (!Monomial.isZero(m)) {
        stringBuilder.insert(0, m.toString());
        stringBuilder.insert(0, " + ");
      }
    }
    return stringBuilder.toString().substring(" + ".length()).replace("+ -", "- ");
  }
}
