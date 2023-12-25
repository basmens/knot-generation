package nl.basmens.knot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.FutureTask;

import nl.basmens.Main;
import nl.basmens.utils.Matrix;
import nl.basmens.utils.Vector;

public class Knot {
  // Unknot
  private static final FutureTask<Boolean> FUTURE_UNKNOT_TRICOLORABILITY = new FutureTask<>(() ->{}, false);
  private static final FutureTask<Long> FUTURE_UNKNOT_KNOT_DETERMINANT = new FutureTask<>(() ->{}, 1L);
  static {
    FUTURE_UNKNOT_TRICOLORABILITY.run();
    FUTURE_UNKNOT_KNOT_DETERMINANT.run();
  }

  // Reduced
  private Connection reducedFirstConnection;
  private ArrayList<Intersection> intersections = new ArrayList<>();
  
  private boolean hasAsignedSectionIds;
  
  // Drawable
  private Connection drawableFirstConnection;
  private int length;

  // Invariants
  private FutureTask<Boolean> tricolorabilityFuture;
  private FutureTask<Long> knotDeterminantFuture;

  // ===================================================================================================================
  // Constructor
  // ===================================================================================================================
  public Knot(Connection firstConnection) {
    if (Main.KEEP_DRAWABLE_KNOTS) {
      initKeepDrawable(firstConnection);
    } else {
      initLoseDrawable(firstConnection);
    }
  }

  private void initKeepDrawable(Connection firstConnection) {
    drawableFirstConnection = firstConnection;

    // Copy drawable knot into new reduced knot
    Connection current = firstConnection;
    Connection currentReduced = null;
    HashMap<Connection, Connection> equivalentsForCopy = new HashMap<>();
    do {
      if (current.isIntersected()) {
        Intersection intersection = current.getIntersection();

        // First time crossing this Intersection
        if (!equivalentsForCopy.containsKey(intersection.under)) {
          Connection under = new Connection(intersection.under);
          Connection over = new Connection(intersection.over);
          intersections.add(new Intersection(under, over));
          equivalentsForCopy.put(intersection.under, under);
          equivalentsForCopy.put(intersection.over, over);
        }

        if (currentReduced == null) {
          currentReduced = equivalentsForCopy.get(current);
          reducedFirstConnection = currentReduced;
        } else {
          currentReduced.setNext(equivalentsForCopy.get(current));
          currentReduced = currentReduced.getNext();
        }
      }

      length++;
      current = current.getNext();
    } while (current != firstConnection);

    // Check for obvious unknot
    if (intersections.size() < 3) {
      initToUnknot(firstConnection.getPos());
    } else {
      // Complete the loop
      currentReduced.setNext(reducedFirstConnection);
    }
  }

  private void initLoseDrawable(Connection firstConnection) {
    // Cut out any unconnected Connections
    Connection current = firstConnection;
    Connection lastIntersected = firstConnection;
    do {
      if (current.isIntersected()) {
        if (!intersections.contains(current.getIntersection())) {
          intersections.add(current.getIntersection());
        }

        if (current != firstConnection) {
          lastIntersected.setNext(current);
        }
        lastIntersected = current;
      }

      length++;
      current = current.getNext();
    } while (current != firstConnection);

    // Check if obvious unknot
    if (intersections.size() < 3) {
      initToUnknot(firstConnection.getPos());
    } else {
      // In case firstConnection was not Intersected, also cut it out
      reducedFirstConnection = lastIntersected;
      if (firstConnection.isIntersected()) {
        lastIntersected.setNext(firstConnection);
      } else {
        lastIntersected.setNext(firstConnection.getNext());
      }
    }
    drawableFirstConnection = reducedFirstConnection;
  }

  private void initToUnknot(Vector pos) {
    reducedFirstConnection = new Connection(pos, 0);
    reducedFirstConnection.setNext(reducedFirstConnection);
    intersections.clear();

    tricolorabilityFuture = FUTURE_UNKNOT_TRICOLORABILITY;
    knotDeterminantFuture = FUTURE_UNKNOT_KNOT_DETERMINANT;

    hasAsignedSectionIds = true;
  }


  // ===================================================================================================================
  // Asign section id's
  // ===================================================================================================================
  private void asignSectionIds() {
    if (hasAsignedSectionIds) {
      return;
    }

    int currentSectionid = 0;
    Connection connection = reducedFirstConnection;
    do {
      if (connection.isUnder()) {
        connection.getIntersection().underSectionId1 = currentSectionid;
        currentSectionid = (currentSectionid + 1) % intersections.size();
        connection.getIntersection().underSectionId2 = currentSectionid;
      } else {
        connection.getIntersection().overSectionId = currentSectionid;
      }

      connection = connection.getNext();
    } while (connection != reducedFirstConnection);

    hasAsignedSectionIds = true;
  }

  // ===================================================================================================================
  // Invariants
  // ===================================================================================================================

  // Tricolorability
  private boolean calculateTricolorability() {
    Connection connection = reducedFirstConnection;
    while (!connection.isUnder() && connection != reducedFirstConnection.getPrev()) {
      connection = connection.getNext();
    }
    connection.propagateTricolorability(1);

    connection = reducedFirstConnection;
    int firstSectionValue = connection.getSectionValue();
    do {
      int sectionValue = connection.getSectionValue();
      if (firstSectionValue != sectionValue) {
        return true;
      }
      connection = connection.getNext();
    } while (connection != reducedFirstConnection);

    return false;
  }

  // KnotDeterminant
  private long calculateKnotDeterminant() {
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
    return Math.round(Math.abs(matrix.getDeterminant()));
  }

  // ===================================================================================================================
  // Getters
  // ===================================================================================================================
  public Connection getReducedFirstConnection() {
    return reducedFirstConnection;
  }

  public Connection getDrawableFirstConnection() {
    return drawableFirstConnection;
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

  public synchronized void startCalcTricolorability() {
    if (tricolorabilityFuture == null) {
      tricolorabilityFuture = new FutureTask<>(this::calculateTricolorability);
      new Thread(tricolorabilityFuture::run).start();
    }
  }

  public synchronized boolean isTricolorable() {
    if (tricolorabilityFuture == null) {
      startCalcTricolorability();
    }

    try {
      return tricolorabilityFuture.get();
    } catch (Exception e) {
      // Should be imposible to reach: the future is already done
      e.printStackTrace();
      return false;
    }
  }

  public String getTricolorabilityState() {
    if (hasCalculatedTricolorability()) {
      return "" + isTricolorable();
    } else if (tricolorabilityFuture != null) {
      return "Calculating...";
    }
    return "Not Calculated";
  }

  public synchronized void startCalcKnotDeterminant() {
    if (knotDeterminantFuture == null) {
      knotDeterminantFuture = new FutureTask<>(this::calculateKnotDeterminant);
      new Thread(knotDeterminantFuture::run).start();
    }
  }

  public long getKnotDeterminant() {
    if (knotDeterminantFuture == null) {
      startCalcKnotDeterminant();
    }

    try {
      return knotDeterminantFuture.get();
    } catch (Exception e) {
      // Should be imposible to reach: the future is already done
      e.printStackTrace();
      return 0;
    }
  }

  public boolean hasCalculatedKnotDeterminant() {
    return knotDeterminantFuture != null && knotDeterminantFuture.isDone();
  }

  public String getKnotDeterminantState() {
    if (hasCalculatedKnotDeterminant()) {
      return "" + String.format(Locale.UK, "%d", getKnotDeterminant());
    } else if (knotDeterminantFuture != null) {
      return "Calculating...";
    }
    return "Not Calculated";
  }
}
