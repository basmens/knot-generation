package nl.basmens.knot;

import java.util.ArrayList;
import java.util.List;

public class Knot {
  private Connection firstConnection;
  private ArrayList<Intersection> intersections = new ArrayList<>();
  private int length;

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
  // Commands
  // ===================================================================================================================

  public void test() {
    int result = 0;

    for (Intersection i: intersections) {
      i.printState();
      int type = i.getType();
      if (type == 0) {
        result--;
      } else {
        result++;
      }
    }
    System.out.println(result);
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
