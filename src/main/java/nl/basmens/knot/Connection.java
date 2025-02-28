package nl.basmens.knot;

import nl.basmens.Main;
import nl.basmens.utils.maths.Vector;

public class Connection implements Comparable<Connection> {
  private Connection prev;
  private Connection next;

  private Intersection intersection;

  private Vector pos;
  private double dir;

  private int sectionValue; // 0 = unset

  // ===================================================================================================================
  // Constructor
  // ===================================================================================================================
  public Connection(Vector pos, double dir) {
    this.pos = pos;

    double twoPi = Math.PI * 2;
    this.dir = ((dir % twoPi) + twoPi) % twoPi;
  }

  public Connection(Connection copy) {
    this.pos = copy.getPos();
    this.dir = copy.getDir();
  }

  // ===================================================================================================================
  // Tricolorability
  // ===================================================================================================================

  protected boolean propagateTricolorability(int sectionValue, long startTime) {
    if (System.currentTimeMillis() - startTime > Main.MAX_CALC_TIME_PER_INVARIANT) {
      throw new RuntimeException("Max calculation time exceeded in tricolorability");
    }

    if (this.sectionValue != 0) {
      return true;
    }

    if (!isIntersected()) {
      this.sectionValue = sectionValue;

      if (next.propagateTricolorability(sectionValue, startTime)) {
        return true;
      } else {
        this.sectionValue = 0;
        return false;
      }
    } else if (isOver()) {
      this.sectionValue = sectionValue;

      if (!isFollowingTricolorability()) {
        this.sectionValue = 0;
        return false;
      }

      if (next.propagateTricolorability(sectionValue, startTime)) {
        return true;
      } else {
        this.sectionValue = 0;
        return false;
      }
    } else {

      if (propagateNextValue(sectionValue, startTime)) {
        return true;
      } else {
        this.sectionValue = 0;
        return false;
      }
    }
  }

  private boolean isFollowingTricolorability() {
    // check if initialized
    return getIntersection().under.getSectionValue() == 0 ||
        getIntersection().under.getPrev().getSectionValue() == 0 ||
        getIntersection().over.getSectionValue() == 0 ||

        // check if the same
        (getIntersection().under.getSectionValue() == getIntersection().over.getSectionValue() &&
            getIntersection().under.getPrev().getSectionValue() == getIntersection().over.getSectionValue())

        // check if different
        || (getIntersection().under.getSectionValue() != getIntersection().over.getSectionValue() &&
            getIntersection().under.getPrev().getSectionValue() != getIntersection().over.getSectionValue() &&
            getIntersection().under.getSectionValue() != getIntersection().under.getPrev().getSectionValue());
  }

  private boolean propagateNextValue(int lastValue, long startTime) {
    for (int i = 1; i < 4; i++) {
      if (i == lastValue) {
        continue;
      }

      this.sectionValue = i;
      if (!isFollowingTricolorability()) {
        continue;
      }
      if (getNext().propagateTricolorability(this.sectionValue, startTime)) {
        return true;
      }
    }

    this.sectionValue = lastValue;
    if (isFollowingTricolorability() && getNext().propagateTricolorability(this.sectionValue, startTime)) {
      return true;
    }

    this.sectionValue = 0;
    return false;
  }

  // ===================================================================================================================
  // Getters
  // ===================================================================================================================

  public Vector getPos() {
    return pos.copy();
  }

  public double getDir() {
    return dir;
  }

  public double getBackwardsDir() {
    double twoPi = Math.PI * 2;
    return (((dir - Math.PI) % twoPi) + twoPi) % twoPi;
  }

  public Connection getPrev() {
    return prev;
  }

  public Connection getNext() {
    return next;
  }

  public boolean isIntersected() {
    return intersection != null;
  }

  public Intersection getIntersection() {
    return intersection;
  }

  public boolean isUnder() {
    return isIntersected() && getIntersection().under == this;
  }

  public boolean isOver() {
    return isIntersected() && getIntersection().over == this;
  }

  public int getSectionValue() {
    return sectionValue;
  }

  // ===================================================================================================================
  // Setters
  // ===================================================================================================================

  public void setPos(Vector pos) {
    this.pos = pos;
  }

  public void setDir(double dir) {
    this.dir = dir;
  }

  public void setIntersection(Intersection intersection) {
    this.intersection = intersection;
  }

  public void setPrev(Connection prev) {
    if (this.prev == prev) {
      return;
    }

    Connection ex = this.prev;
    this.prev = null;
    if (ex != null) {
      ex.setNext(null);
    }

    if (prev != null) {
      this.prev = prev;
      prev.setNext(this);
    }
  }

  public void setNext(Connection next) {
    if (this.next == next) {
      return;
    }

    Connection ex = this.next;
    this.next = null;
    if (ex != null) {
      ex.setPrev(null);
    }

    if (next != null) {
      this.next = next;
      next.setPrev(this);
    }
  }

  @Override
  public int compareTo(Connection c) {
    return this.equals(c) ? 0 : (dir > c.getDir() ? 1 : 0);
  }
}
