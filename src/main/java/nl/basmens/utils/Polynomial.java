package nl.basmens.utils;

import java.util.ArrayList;

public class Polynomial {
  private ArrayList<Monomial> monomials = new ArrayList<>();
  private int index0Power = 0;

  // =================================================================================================================
  // Constructor
  // =================================================================================================================

  public Polynomial() {
  }

  public Polynomial(Monomial... inputMonomials) {
    for (Monomial m : inputMonomials) {
      if (m == null || m.getCoefficient() == 0) {
        continue;
      }

      getMonomial(m.getPower()).add(m);
    }
  }

  public Polynomial copy() {
    Polynomial result = new Polynomial();
    for (Monomial m : monomials) {
      if (m == null || m.getCoefficient() == 0) {
        continue;
      }

      result.getMonomial(m.getPower()).add(m);
    }
    return result;
  }

  // =================================================================================================================
  // Math
  // =================================================================================================================

  public Polynomial add(Polynomial otherPolynomial) {
    for (Monomial m : otherPolynomial.monomials) {
      if (m == null || m.getCoefficient() == 0) {
        continue;
      }

      getMonomial(m.getPower()).add(m);
    }
    return this;
  }

  public Polynomial sub(Polynomial otherPolynomial) {
    for (Monomial m : otherPolynomial.monomials) {
      if (m == null || m.getCoefficient() == 0) {
        continue;
      }

      getMonomial(m.getPower()).sub(m);
    }
    return this;
  }

  public static Polynomial mult(Polynomial polynomial1, Polynomial polynomial2) {
    Polynomial result = new Polynomial();

    for (Monomial monomial1 : polynomial1.monomials) {
      if (monomial1 == null || monomial1.getCoefficient() == 0) {
        continue;
      }

      for (Monomial monomial2 : polynomial2.monomials) {
        if (monomial2 == null || monomial2.getCoefficient() == 0) {
          continue;
        }

        Monomial resultMonomial = monomial1.copy().mult(monomial2);

        result.getMonomial(resultMonomial.getPower()).add(resultMonomial);
      }
    }

    return result;
  }

  public static Polynomial div(Polynomial polynomial1, Monomial monomial2) {
    Polynomial result = new Polynomial();

    if (monomial2 == null || monomial2.getCoefficient() == 0) {
      throw new IllegalArgumentException("ERROR: cannot divide by 0");
    }

    for (Monomial monomial1 : polynomial1.monomials) {
      if (monomial1 == null || monomial1.getCoefficient() == 0) {
        continue;
      }

      Monomial resultMonomial = monomial1.copy().div(monomial2);

      result.getMonomial(resultMonomial.getPower()).add(resultMonomial);
    }

    return result;
  }

  public static Polynomial div(Polynomial polynomial1, Polynomial polynomial2) {
    Polynomial result = new Polynomial();
    if (polynomial2.isZero()) {
      throw new IllegalArgumentException("ERROR: cannot divide by 0");
    }
    if (polynomial1.isZero()) {
      return result;
    }

    int iterationCount = polynomial1.getHighestPower().getPower() - polynomial1.getLowestPower().getPower() + 1;
    Polynomial rest = polynomial1.copy();

    for (int i = 0; i < iterationCount; i++) {
      if (rest.isZero()) {
        break;
      }

      Monomial multiplier = rest.getHighestPower().copy().div(polynomial2.getHighestPower());
      rest.sub(Polynomial.mult(polynomial2, new Polynomial(multiplier)));

      result.getMonomial(multiplier.getPower()).add(multiplier);
    }

    if (!rest.isZero()) {
      throw new IllegalArgumentException(
          "ERROR: division results in " + result + " with " + rest + " / " + polynomial2 + " as rest");
    }

    return result;
  }

  private void ensureCapacity(int power) {
    if (hasMonomial(power)) {
      return;
    }

    if (power < index0Power) {
      for (int i = 0; i < index0Power - power; i++) {
        monomials.add(0, null);
      }
      index0Power = power;
    } else {
      int arraySize = monomials.size();
      for (int i = 0; i < power - arraySize - index0Power + 1; i++) {
        monomials.add(null);
      }
    }
  }

  // =================================================================================================================
  // Getters
  // =================================================================================================================

  public double getValue(double unknown) {
    double value = 0;
    for (Monomial m: monomials) {
      if (m != null && m.getCoefficient() != 0) {
        value += m.getValue(unknown);
      }
    }
    return value;
  }

  private Monomial getMonomial(int power) {
    ensureCapacity(power);

    if (monomials.get(power - index0Power) == null) {
      monomials.set(power - index0Power, new Monomial(0, power));
    }

    return monomials.get(power - index0Power);
  }

  public boolean hasMonomial(int power) {
    return power >= index0Power && power - index0Power < monomials.size();
  }

  public boolean isZero() {
    return getLowestPower() == null;
  }

  public int getMonomialCount() {
    int count = 0;
    for (Monomial m: monomials) {
      if (m != null && m.getCoefficient() != 0) {
        count++;
      }
    }
    return count;
  }

  public Monomial getLowestPower() {
    for (int i = 0; i < monomials.size(); i++) {
      Monomial monomial = monomials.get(i);
      if (monomial != null && monomial.getCoefficient() != 0) {
        return monomial;
      }
    }
    return null;
  }

  public Monomial getHighestPower() {
    for (int i = monomials.size() - 1; i >= 0; i--) {
      Monomial monomial = monomials.get(i);
      if (monomial != null && monomial.getCoefficient() != 0) {
        return monomial;
      }
    }
    return null;
  }

  @Override
  public String toString() {
    if (isZero()) {
      return "0";
    }

    StringBuilder stringBuilder = new StringBuilder();
    for (Monomial m : monomials) {
      if (m == null || m.getCoefficient() == 0) {
        continue;
      }

      stringBuilder.insert(0, m.toString());
      stringBuilder.insert(0, " + ");
    }
    return stringBuilder.toString().substring(" + ".length()).replace("+ -", "- ");
  }
}