package nl.basmens.knot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.FutureTask;

import nl.basmens.Main;
import nl.basmens.utils.Matrix;
import nl.basmens.utils.Monomial;
import nl.basmens.utils.Polynomial;
import nl.basmens.utils.PolynomialMatrix;
import nl.basmens.utils.Vector;

public class Knot {
  // Unknot
  private static final FutureTask<Boolean> FUTURE_UNKNOT_TRICOLORABILITY = new FutureTask<>(() -> {
  }, false);
  private static final FutureTask<Long> FUTURE_UNKNOT_KNOT_DETERMINANT = new FutureTask<>(() -> {
  }, 1L);
  private static final FutureTask<Polynomial> FUTURE_UNKNOT_ALEXANDER_POLYNOMIAL = new FutureTask<>(() -> {
  }, new Polynomial(new Monomial(1, 0)));
  static {
    FUTURE_UNKNOT_TRICOLORABILITY.run();
    FUTURE_UNKNOT_KNOT_DETERMINANT.run();
    FUTURE_UNKNOT_ALEXANDER_POLYNOMIAL.run();
  }

  // Reduced
  private Connection reducedFirstConnection;
  private ArrayList<Intersection> intersections = new ArrayList<>();

  private boolean hasAsignedSectionIds;
  private boolean hasAsignedAreaIds;

  // Drawable
  private Connection drawableFirstConnection;
  private int length;

  // Invariants
  private FutureTask<Boolean> tricolorabilityFuture;
  private FutureTask<Long> knotDeterminantFuture;
  private FutureTask<Polynomial> alexanderPolynomialFuture;

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
    alexanderPolynomialFuture = FUTURE_UNKNOT_ALEXANDER_POLYNOMIAL;

    hasAsignedSectionIds = true;
    hasAsignedAreaIds = true;
  }

  // ===================================================================================================================
  // Asign section id's
  // ===================================================================================================================

  private void asignSectionIds() {
    if (hasAsignedSectionIds) {
      return;
    }

    int currentSectionId = 0;
    Connection connection = reducedFirstConnection;
    do {
      if (connection.isUnder()) {
        connection.getIntersection().underSectionId1 = currentSectionId;
        currentSectionId = (currentSectionId + 1) % intersections.size();
        connection.getIntersection().underSectionId2 = currentSectionId;
      } else {
        connection.getIntersection().overSectionId = currentSectionId;
      }

      connection = connection.getNext();
    } while (connection != reducedFirstConnection);

    hasAsignedSectionIds = true;
  }

  // ===================================================================================================================
  // Asign area id's
  // ===================================================================================================================
  private void asignAreaIds() {
    if (hasAsignedAreaIds) {
      return;
    }

    int currentAreaId = 0;

    for (Intersection intersection : intersections) {
      for (int i = 0; i < 4; i++) {
        if (intersection.areaIds[i] == -1) {
          intersection.areaIds[i] = currentAreaId;
          spreadAreaId(currentAreaId, intersection, i);
          currentAreaId++;
        }
      }
    }
    hasAsignedAreaIds = true;
  }

  private void spreadAreaId(int areaId, Intersection prevIntersection, int prevAreaIdIndex) {
    Connection nextConnection;
    boolean isBackwards;

    double twoPi = Math.PI * 2;
    boolean isUnderAngleLeft = ((prevIntersection.under.getDir() - prevIntersection.over.getDir()) % twoPi + twoPi)
        % twoPi < Math.PI;

    // find connection to continue on
    switch (prevAreaIdIndex) {
      case 0:
        nextConnection = prevIntersection.over.getNext();
        isBackwards = false;
        break;
      case 1:
        isBackwards = isUnderAngleLeft;
        if (isUnderAngleLeft) {
          nextConnection = prevIntersection.under.getPrev();
        } else {
          nextConnection = prevIntersection.under.getNext();
        }
        break;
      case 2:
        nextConnection = prevIntersection.over.getPrev();
        isBackwards = true;
        break;

      default:
        isBackwards = !isUnderAngleLeft;
        if (isUnderAngleLeft) {
          nextConnection = prevIntersection.under.getNext();
        } else {
          nextConnection = prevIntersection.under.getPrev();
        }
        break;
    }

    Intersection nextIntersection = nextConnection.getIntersection();

    isUnderAngleLeft = ((nextIntersection.under.getDir() - nextIntersection.over.getDir()) % twoPi + twoPi)
        % twoPi < Math.PI;

    int nextAreaIdIndex;
    // find areaIdIndex based on where the intersection got entered
    if (nextConnection == nextIntersection.over) {
      if (isBackwards) {
        nextAreaIdIndex = 1;
      } else {
        nextAreaIdIndex = 3;
      }
    } else {
      if (isBackwards) {
        if (isUnderAngleLeft) {
          nextAreaIdIndex = 0;
        } else {
          nextAreaIdIndex = 2;
        }
      } else {
        if (isUnderAngleLeft) {
          nextAreaIdIndex = 2;
        } else {
          nextAreaIdIndex = 0;
        }
      }
    }

    if (nextIntersection.areaIds[nextAreaIdIndex] == -1) {
      nextIntersection.areaIds[nextAreaIdIndex] = areaId;
      spreadAreaId(areaId, nextIntersection, nextAreaIdIndex);
    }
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
    return Math.round(Math.abs(matrix.getDeterminant()));
  }

  // AlexanderPolynomial
  private Polynomial calculateAlexanderPolynomial() {
    if (intersections.size() < 3) {
      return new Polynomial(new Monomial(1, 0));
    }

    asignAreaIds();

    PolynomialMatrix matrix = new PolynomialMatrix(intersections.size(), intersections.size());

    for (int i = 0; i < intersections.size(); i++) {
      Intersection intersection = intersections.get(i);
      for (int j = 0; j < 4; j++) {
        if (intersection.areaIds[j] < 2) {
          continue;
        }

        // / / | / /
        // / t |-1 /
        // >------->
        // /-t | 1 /
        // / / | / /
        matrix.get(intersection.areaIds[j] - 2, i).add(switch (j) {
          case 0:
            yield new Polynomial(new Monomial(1, 0));
          case 1:
            yield new Polynomial(new Monomial(-1, 0));
          case 2:
            yield new Polynomial(new Monomial(1, 1));
          default:
            yield new Polynomial(new Monomial(-1, 1));
        });
      }
    }

    Polynomial determinant = matrix.getDeterminant();
    Monomial smallestTerm = determinant.getLowestMonomial();
    if (smallestTerm != null) {
      determinant = Polynomial.div(determinant,
          new Monomial((long) Math.signum(smallestTerm.getCoefficient()), smallestTerm.getPower()));
    }
    return determinant;
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
      Thread.currentThread().interrupt();
      e.printStackTrace();
      throw new IllegalStateException();
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

  public boolean hasCalculatedKnotDeterminant() {
    return knotDeterminantFuture != null && knotDeterminantFuture.isDone();
  }

  public synchronized void startCalcKnotDeterminant() {
    if (knotDeterminantFuture == null) {
      knotDeterminantFuture = new FutureTask<>(this::calculateKnotDeterminant);
      knotDeterminantFuture.run();
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
      Thread.currentThread().interrupt();
      e.printStackTrace();
      throw new IllegalStateException();
    }
  }

  public String getKnotDeterminantState() {
    if (hasCalculatedKnotDeterminant()) {
      return "" + String.format(Locale.UK, "%d", getKnotDeterminant());
    } else if (knotDeterminantFuture != null) {
      return "Calculating...";
    }
    return "Not Calculated";
  }

  public boolean hasCalculatedAlexanderPolynomial() {
    return alexanderPolynomialFuture != null && alexanderPolynomialFuture.isDone();
  }

  public synchronized void startCalcAlexanderPolynomial() {
    if (alexanderPolynomialFuture == null) {
      alexanderPolynomialFuture = new FutureTask<>(this::calculateAlexanderPolynomial);
      alexanderPolynomialFuture.run();
    }
  }

  public Polynomial getAlexanderPolynomial() {
    if (alexanderPolynomialFuture == null) {
      startCalcAlexanderPolynomial();
    }

    try {
      return alexanderPolynomialFuture.get();
    } catch (Exception e) {
      // Should be imposible to reach: the future is already done
      Thread.currentThread().interrupt();
      e.printStackTrace();
      throw new IllegalStateException();
    }
  }

  public String getAlexanderPolynomialState() {
    if (hasCalculatedAlexanderPolynomial()) {
      return getAlexanderPolynomial().toString();
    } else if (knotDeterminantFuture != null) {
      return "Calculating...";
    }
    return "Not Calculated";
  }
}
