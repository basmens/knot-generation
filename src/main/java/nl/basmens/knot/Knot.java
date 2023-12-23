package nl.basmens.knot;

import java.util.ArrayList;
import java.util.List;

import nl.basmens.utils.Matrix;

public class Knot {
  private Connection firstConnection;
  private ArrayList<Intersection> intersections = new ArrayList<>();
  private int length;

  private boolean hasAsignedSectionIds = false;

  private boolean isTricolorable;
  private boolean isCalculatingTricolorability = false;
  private boolean isTricolorabilityCalculated = false;

  // -2 = Not calculating // -1 = Calculating...
  private double knotDeterminant = -2;

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

  private void asignSectionIds() {
    if (hasAsignedSectionIds) {
      return;
    }

    Connection firstIntersectedConnection = getFirstConnection();
    while (!firstIntersectedConnection.isUnder() && firstIntersectedConnection != getFirstConnection().getPrev()) {
      firstIntersectedConnection = firstIntersectedConnection.getNext();
    }

    int currentSectionid = -1;

    Connection connection = firstIntersectedConnection;
    do {
      if (connection.isUnder()) {
        currentSectionid++;
        connection.getIntersection().underSectionId2 = currentSectionid;
      } else if (connection.isOver()) {
        connection.getIntersection().overSectionId = currentSectionid;
      }

      if (connection.getNext().isUnder()) {
        connection.getNext().getIntersection().underSectionId1 = currentSectionid;
      }

      connection = connection.getNext();
    } while (connection != firstIntersectedConnection);

    hasAsignedSectionIds = true;
  }

  // ===================================================================================================================
  // Invariants
  // ===================================================================================================================

  // Tricolorability

  public void startCalculatingTricolorability() {
    if (!isCalculatingTricolorability) {
      isCalculatingTricolorability = true;
      new Thread(this::calculateTricolorability).start();
    }
  }

  private void calculateTricolorability() {
    Connection connection = getFirstConnection();
    while (!connection.isUnder() && connection != getFirstConnection().getPrev()) {
      connection = connection.getNext();
    }
    connection.propagateTricolorability(1);

    connection = getFirstConnection();
    int firstSectionValue = connection.getSectionValue();
    do {
      int sectionValue = connection.getSectionValue();
      if (firstSectionValue != sectionValue) {
        isTricolorable = true;
        break;
      }
      connection = connection.getNext();
    } while (connection != getFirstConnection());

    isTricolorabilityCalculated = true;
  }

  // KnotDeterminant

  public void startCalculatingKnotDeterminant() {
    if (knotDeterminant == -2) {
      knotDeterminant = -1;
      new Thread(this::calculateKnotDeterminant).start();
    }
  }

  private void calculateKnotDeterminant() {
    if (intersections.size() < 3) {
      knotDeterminant = 1;
      return;
    }

    asignSectionIds();

    Matrix matrix = new Matrix(intersections.size() - 1, intersections.size() - 1);

    for (int i = 0; i < intersections.size() - 1; i++) {
      Intersection intersection = intersections.get(i);
      if (intersection.overSectionId < intersections.size() - 1) {
        matrix.set(intersection.overSectionId, i, matrix.get(intersection.overSectionId, i) + 2);
      }
      if (intersection.underSectionId1 < intersections.size() - 1) {
        matrix.set(intersection.underSectionId1, i, matrix.get(intersection.underSectionId1, i) - 1);
      }
      if (intersection.underSectionId2 < intersections.size() - 1) {
        matrix.set(intersection.underSectionId2, i, matrix.get(intersection.underSectionId2, i) - 1);
      }
    }

    knotDeterminant = Math.abs(matrix.getDeterminant());
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

  public boolean isTricolorable() {
    return isTricolorable;
  }

  public boolean isTricolorabilityCalculated() {
    return isTricolorabilityCalculated;
  }

  public String getTricolorabilityState() {
    if (isTricolorabilityCalculated) {
      return "" + isTricolorable;
    } else if (isCalculatingTricolorability) {
      return "Calculating...";
    }
    return "Not Calculated";
  }

  public double getKnotDeterminant() {
    return knotDeterminant;
  }

  public boolean isKnotDeterminantCalculated() {
    return knotDeterminant != -2 && knotDeterminant != -1;
  }

  public String getKnotDeterminantState() {
    if (knotDeterminant >= 0) {
      return "" + String.format("%.3f", knotDeterminant);
    } else if (knotDeterminant == -1) {
      return "Calculating...";
    } else if (knotDeterminant == -2) {
      return "Not Calculated";
    }
    return "" + knotDeterminant;
  }
}
