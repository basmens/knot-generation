package nl.basmens.knot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.FutureTask;

import nl.basmens.Main;
import nl.basmens.utils.concurrent.PerformanceTimer;
import nl.basmens.utils.maths.Matrix;
import nl.basmens.utils.maths.Monomial;
import nl.basmens.utils.maths.Polynomial;
import nl.basmens.utils.maths.PolynomialMatrix;
import nl.basmens.utils.maths.Vector;

public class Knot {
  // Predefined values
  private static final FutureTask<Boolean> FUTURE_UNKNOT_TRICOLORABILITY = new FutureTask<>(() -> false);
  private static final FutureTask<Long> FUTURE_UNKNOT_KNOT_DETERMINANT = new FutureTask<>(() -> 1L);
  private static final FutureTask<Polynomial> FUTURE_UNKNOT_ALEXANDER_POLYNOMIAL = new FutureTask<>(
      () -> new Polynomial(new Monomial(1, 0)));
  private static final boolean ERROR_VALUE_TRICOLORABILITY = false; // Not really an error value tbh...
  private static final long ERROR_VALUE_KNOT_DETERMINANT = -1;
  private static final Polynomial ERROR_VALUE_ALEXANDER_POLYNOMIAL = new Polynomial(new Monomial(-1, 0));

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
  private int intersectionCount;
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
    intersectionCount = intersections.size();
    if (intersections.size() < 3) {
      initToUnknot(firstConnection.getPos());
    } else {
      // Complete the loop
      currentReduced.setNext(reducedFirstConnection);
      simplifyUsingReidemeisterMoves();
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
    intersectionCount = intersections.size();
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
      simplifyUsingReidemeisterMoves();
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

  public void simplifyUsingReidemeisterMoves() {
    // Simplify using reidemeister move one
    simplifyUsingReidemeisterMoveOne();
    if (intersections.isEmpty()) {
      return;
    }

    for (int index = intersections.size() - 1; index >= 0; index--) {
      // Simplify using reidemeister move two
      Intersection i = intersections.get(index);
      Connection otherUnder = i.under.getNext();
      if (otherUnder.isOver()) {
        continue;
      }

      Connection otherOver = otherUnder.getIntersection().over;
      if (i.over.getNext() == otherOver) {
        i.over.getPrev().setNext(otherOver.getNext());
      } else if (i.over.getPrev() == otherOver) {
        i.over.getNext().setPrev(otherOver.getPrev());
      } else {
        continue;
      }

      i.under.getPrev().setNext(otherUnder.getNext());
      intersections.remove(index);
      intersections.remove(otherUnder.getIntersection());

      // Check if unknot
      if (intersections.size() < 3) {
        initToUnknot(reducedFirstConnection.getPos());
        return;
      }

      // Simplify using reidemeister move one
      simplifyUsingReidemeisterMoveOne();
      if (intersections.isEmpty()) {
        return;
      }

      index = intersections.size();
    }
    reducedFirstConnection = intersections.get(0).under;
  }

  private void simplifyUsingReidemeisterMoveOne() {
    for (int index = intersections.size() - 1; index >= 0; index--) {
      Intersection i = intersections.get(index);

      if (i.under.getNext() == i.over) {
        i.under.getPrev().setNext(i.over.getNext());
      } else if (i.over.getNext() == i.under) {
        i.over.getPrev().setNext(i.under.getNext());
      } else {
        continue;
      }

      intersections.remove(index);
      index = intersections.size();

      if (intersections.size() < 3) {
        initToUnknot(reducedFirstConnection.getPos());
        return;
      }
    }
    reducedFirstConnection = intersections.get(0).under;
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

  // indexes of areaIds
  // / / | / /
  // / 2 | 1 /
  // >---|--->
  // / 3 | 0 /
  // / / | / /

  private void spreadAreaId(int areaId, Intersection prevIntersection, int prevAreaIdIndex) {
    Connection nextConnection;
    boolean isBackwards;

    double twoPi = Math.PI * 2;
    boolean isOverAngleRight = ((prevIntersection.over.getDir() - prevIntersection.under.getDir()) % twoPi + twoPi)
        % twoPi < Math.PI;

    // find connection to continue on
    switch (prevAreaIdIndex) {
      case 0:
        nextConnection = prevIntersection.under.getNext();
        isBackwards = false;
        break;
      case 1:
        isBackwards = isOverAngleRight;
        nextConnection = isOverAngleRight ? prevIntersection.over.getPrev() : prevIntersection.over.getNext();
        break;
      case 2:
        nextConnection = prevIntersection.under.getPrev();
        isBackwards = true;
        break;
      default:
        isBackwards = !isOverAngleRight;
        nextConnection = isOverAngleRight ? prevIntersection.over.getNext() : prevIntersection.over.getPrev();
        break;
    }

    Intersection nextIntersection = nextConnection.getIntersection();

    isOverAngleRight = ((nextIntersection.over.getDir() - nextIntersection.under.getDir()) % twoPi + twoPi)
        % twoPi < Math.PI;

    int nextAreaIdIndex;
    // find areaIdIndex based on where the intersection got entered
    if (nextConnection == nextIntersection.under) {
      if (isBackwards) {
        nextAreaIdIndex = 1;
      } else {
        nextAreaIdIndex = 3;
      }
    } else {
      if (isBackwards) {
        if (isOverAngleRight) {
          nextAreaIdIndex = 0;
        } else {
          nextAreaIdIndex = 2;
        }
      } else {
        if (isOverAngleRight) {
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
    PerformanceTimer timer = new PerformanceTimer(getClass(), "calculateTricolorability");

    long startTime = System.nanoTime();
    try {
      Connection connection = reducedFirstConnection;
      while (!connection.isUnder() && connection != reducedFirstConnection.getPrev()) {
        connection = connection.getNext();
      }
      connection.propagateTricolorability(1, startTime);

      connection = reducedFirstConnection;
      int firstSectionValue = connection.getSectionValue();
      do {
        int sectionValue = connection.getSectionValue();
        if (firstSectionValue != sectionValue) {
          timer.stop();
          return true;
        }
        connection = connection.getNext();
      } while (connection != reducedFirstConnection);

      timer.stop();
      return false;
    } catch (RuntimeException e) {
      e.printStackTrace();
      timer.stop();
      return ERROR_VALUE_TRICOLORABILITY;
    }
  }

  // KnotDeterminant
  private long calculateKnotDeterminant() {
    PerformanceTimer timer = new PerformanceTimer(getClass(), "calculateKnotDeterminant");
    long startTime = System.nanoTime();
    try {
      if (intersections.size() < 3) {
        timer.stop();
        return 1;
      }

      asignSectionIds();

      Matrix matrix = new Matrix(intersections.size() - 1, intersections.size() - 1);

      for (int i = 0; i < intersections.size() - 1; i++) {
        Intersection intersection = intersections.get(i);
        if (intersection.overSectionId < intersections.size() - 1) {
          matrix.add(intersection.overSectionId, i, 2);
        }
        if (intersection.underSectionId1 < intersections.size() - 1) {
          matrix.add(intersection.underSectionId1, i, -1);
        }
        if (intersection.underSectionId2 < intersections.size() - 1) {
          matrix.add(intersection.underSectionId2, i, -1);
        }
      }

      // round to get rid of precision loss
      timer.stop();
      return Math.round(Math.abs(matrix.getDeterminant(startTime)));
    } catch (RuntimeException e) {
      // e.printStackTrace();
      timer.stop();
      return ERROR_VALUE_KNOT_DETERMINANT;
    }
  }

  // AlexanderPolynomial
  private Polynomial calculateAlexanderPolynomial() {
    PerformanceTimer timer = new PerformanceTimer(getClass(), "calculateAlexanderPolynomial");
    long startTime = System.nanoTime();
    try {
      if (intersections.size() < 3) {
        timer.stop();
        return new Polynomial(new Monomial(1, 0));
      }

      asignAreaIds();

      int s = intersections.size();
      PolynomialMatrix matrix = new PolynomialMatrix(s, s);
      for (int i = 0; i < s; i++) {
        Intersection intersection = intersections.get(i);
        for (int j = 0; j < 4; j++) {
          if (intersection.areaIds[j] < 2) {
            continue;
          }

          // / / | / /
          // /-t | t /
          // >---|--->
          // / 1 | -1 /
          // / / | / /

          // Also take the transpose of the matrix, because the right top tends to have
          // more zeros
          matrix.get(i, intersection.areaIds[j] - 2).add(switch (j) {
            case 0:
              yield new Polynomial(new Monomial(-1, 0));
            case 1:
              yield new Polynomial(new Monomial(1, 1));
            case 2:
              yield new Polynomial(new Monomial(-1, 1));
            default:
              yield new Polynomial(new Monomial(1, 0));
          });
        }
      }

      Polynomial determinant = matrix.getDeterminant(startTime);
      Monomial smallestTerm = determinant.getLowestMonomial();
      if (smallestTerm != null) {
        determinant = Polynomial.div(determinant,
            new Monomial((long) Math.signum(smallestTerm.getCoefficient()), smallestTerm.getPower()));
      }
      timer.stop();
      return determinant;
    } catch (RuntimeException e) {
      // e.printStackTrace();
      timer.stop();
      return ERROR_VALUE_ALEXANDER_POLYNOMIAL;
    }
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

  public int getIntersectionCount() {
    return intersectionCount;
  }

  public boolean hasCalculatedTricolorability() {
    return tricolorabilityFuture != null && tricolorabilityFuture.isDone();
  }

  public void startCalcTricolorability() {
    FutureTask<Boolean> f = tricolorabilityFuture;
    if (f != null) {
      return;
    }

    // Double check for thread safety, without potentially blocking all the thread
    // that enter the function
    synchronized (this) {
      if (tricolorabilityFuture == null) {
        tricolorabilityFuture = new FutureTask<>(this::calculateTricolorability);
        tricolorabilityFuture.run();
      }
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

  public void startCalcKnotDeterminant() {
    FutureTask<Long> f = knotDeterminantFuture;
    if (f != null) {
      return;
    }

    // Double check for thread safety, without potentially blocking all the thread
    // that enter the function
    synchronized (this) {
      if (knotDeterminantFuture == null) {
        knotDeterminantFuture = new FutureTask<>(this::calculateKnotDeterminant);
        knotDeterminantFuture.run();
      }
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

  public void startCalcAlexanderPolynomial() {
    FutureTask<Polynomial> f = alexanderPolynomialFuture;
    if (f != null) {
      return;
    }

    // Double check for thread safety, without potentially blocking all the thread
    // that enter the function
    synchronized (this) {
      if (alexanderPolynomialFuture == null) {
        alexanderPolynomialFuture = new FutureTask<>(this::calculateAlexanderPolynomial);
        alexanderPolynomialFuture.run();
      }
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
