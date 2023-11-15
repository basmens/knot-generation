package nl.basmens.knot;

public class Connection {
  private Connection prev;
  private Connection next;
  
  private Intersection intersection;
  
  private double posX;
  private double posY;
  private double dir;

  public Connection(double posX, double posY, double dir) {
    this.posX = posX;
    this.posY = posY;
    this.dir = dir;
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

  public double getPosX() {
    return posX;
  }

  public void setPosX(double posX) {
    this.posX = posX;
  }

  public double getPosY() {
    return posY;
  }

  public void setPosY(double posY) {
    this.posY = posY;
  }

  public double getDir() {
    return dir;
  }

  public void setDir(double dir) {
    this.dir = dir;
  }

  @Override
  public int hashCode() {
    int hash = 17;
    hash = ((hash + (int) posX) << 5) - (hash + (int) posX);
    hash = ((hash + (int) posY) << 5) - (hash + (int) posY);
    return hash;
  }
}
