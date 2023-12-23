package nl.basmens.knot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import nl.basmens.utils.Matrix;

public class Knot {
  private Connection firstConnection;
  private ArrayList<Intersection> intersections = new ArrayList<>();
  private int length;

  private boolean hasAsignedSectionIds;

  // Invariants
  private FutureTask<Boolean> tricolorabilityFuture;
  private FutureTask<Integer> knotDeterminantFuture;

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
  private boolean calculateTricolorability() {
    if (intersections.size() < 3) {
      return false;
    }

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
        return true;
      }
      connection = connection.getNext();
    } while (connection != getFirstConnection());

    return false;
  }

  // KnotDeterminant
  private int calculateKnotDeterminant() {
    if (intersections.size() < 3) {
      return 1;
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

    // round to get rid of precision loss
    return (int)Math.round(Math.abs(matrix.getDeterminant()));
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

  public boolean hasCalculatedTricolorability() {
    return tricolorabilityFuture != null && tricolorabilityFuture.isDone();
  }

  public synchronized Future<Boolean> isTricolorable() {
    if (tricolorabilityFuture == null) {
      tricolorabilityFuture = new FutureTask<>(this::calculateTricolorability);
      new Thread(tricolorabilityFuture::run).start();
    }
    return tricolorabilityFuture;
  }

  public String getTricolorabilityState() {
    if (hasCalculatedTricolorability()) {
      try {
        return "" + isTricolorable().get();
      } catch (Exception e) {
        // Should be imposible to reach: the future is already done
      }
    } else if (tricolorabilityFuture != null) {
      return "Calculating...";
    }
    return "Not Calculated";
  }

  public Future<Integer> getKnotDeterminant() {
    if (knotDeterminantFuture == null) {
      knotDeterminantFuture = new FutureTask<>(this::calculateKnotDeterminant);
      new Thread(knotDeterminantFuture::run).start();
    }
    return knotDeterminantFuture;
  }

  public boolean hasCalculatedKnotDeterminant() {
    return knotDeterminantFuture != null && knotDeterminantFuture.isDone();
  }

  public String getKnotDeterminantState() {
    if (hasCalculatedKnotDeterminant()) {
      try {
        return "" + String.format(Locale.UK, "%d", getKnotDeterminant().get());
      } catch (Exception e) {
        // Should be imposible to reach: the future is already done
      }
    } else if (knotDeterminantFuture != null) {
      return "Calculating...";
    }
    return "Not Calculated";
  }
}
