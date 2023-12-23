package nl.basmens.knot;

import nl.basmens.utils.Vector;

public class Connection {
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
    this.pos = pos.add(0.5, 0.5);

    double twoPi = Math.PI * 2;
    this.dir = ((dir % twoPi) + twoPi) % twoPi;
  }

  // ===================================================================================================================
  // Tricolorability
  // ===================================================================================================================

  protected boolean propagateTricolorability(int sectionValue) {
    if (this.sectionValue != 0) {
      return true;
    }

    if (!isIntersected()) {
      this.sectionValue = sectionValue;

      if (next.propagateTricolorability(sectionValue)) {
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

      if (next.propagateTricolorability(sectionValue)) {
        return true;
      } else {
        this.sectionValue = 0;
        return false;
      }
    } else {

      if (propagateNextValue(sectionValue)) {
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

  private boolean propagateNextValue(int lastValue) {
    for (int i = 1; i < 4; i++) {
      if (i == lastValue) {
        continue;
      }

      this.sectionValue = i;
      if (!isFollowingTricolorability()) {
        continue;
      }
      if (getNext().propagateTricolorability(this.sectionValue)) {
        return true;
      }
    }

    this.sectionValue = lastValue;
    if (isFollowingTricolorability() && getNext().propagateTricolorability(this.sectionValue)) {
      return true;
    }

    this.sectionValue = 0;
    return false;
  }

  // ===================================================================================================================
  // Getters
  // ===================================================================================================================

  public Vector getPos() {
    return pos;
  }

  public double getDir() {
    return dir;
  }

  public Connection getPrev() {
    return prev;
  }

  public Connection getNext() {
    return next;
  }

  public Connection getPrevIntersected() {
    if (prev.isIntersected()) {
      return prev;
    }
    return prev.getPrevIntersected();
  }

  public Connection getNextIntersected() {
    if (next.isIntersected()) {
      return next;
    }
    return next.getNextIntersected();
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
}
