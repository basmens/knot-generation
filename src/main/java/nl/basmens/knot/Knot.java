package nl.basmens.knot;

import java.util.ArrayList;

public class Knot {
  private Connection firstConnection;
  private ArrayList<Intersection> intersections = new ArrayList<>();
  private int length;

  public Knot(Connection firstConnection) {
    this.firstConnection = firstConnection;

    Connection current = firstConnection;
    do {
      if (current.isIntersected() && !intersections.contains(current.getIntersection())) {
        intersections.add(current.getIntersection());
      }
      length++;
      current = current.getNext();
    } while (current != firstConnection);
  }

  public Connection getFirstConnection() {
    return firstConnection;
  }

  public ArrayList<Intersection> getIntersections() {
    return intersections;
  }

  public int getLength() {
    return length;
  }
}
