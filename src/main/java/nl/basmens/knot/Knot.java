package nl.basmens.knot;

import java.util.ArrayList;
import java.util.List;

public class Knot {
  private Connection firstConnection;
  private ArrayList<Intersection> intersections = new ArrayList<>();
  private int length;

  private boolean isTricolorableCalculated = false;

  // ===================================================================================================================
  // Constructor
  // ===================================================================================================================

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

  // ===================================================================================================================
  // Invariants
  // ===================================================================================================================

  public void calculateTricolorability() {
    if (isTricolorableCalculated) {
      return;
    }
    
    Connection connection = getFirstConnection();
    while (!connection.isUnder() && connection != getFirstConnection().getPrev()) {
      connection = connection.getNext();
    }
    connection.propagateTricolorability(1);
    isTricolorableCalculated = true;
  }

  // ===================================================================================================================
  // Getters
  // ===================================================================================================================

  public Connection getFirstConnection() {
    return firstConnection;
  }

  public List<Intersection> getIntersections() {
    return intersections;
  }

  public int getLength() {
    return length;
  }
}
