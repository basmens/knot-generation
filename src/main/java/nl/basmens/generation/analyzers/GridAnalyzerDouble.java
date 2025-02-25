package nl.basmens.generation.analyzers;

import java.util.ArrayList;

import nl.basmens.generation.Tile;
import nl.basmens.knot.Connection;
import nl.basmens.knot.Knot;
import nl.basmens.utils.collections.IndexedSet;
import nl.basmens.utils.concurrent.PerformanceTimer;
import nl.basmens.utils.maths.Vector;

public class GridAnalyzerDouble implements GridAnalyzer {
  private int gridW;
  private int gridH;

  public ArrayList<Knot> extractKnots(Tile[][] grid) {
    PerformanceTimer timer = new PerformanceTimer(getClass(), "extractKnots", "create empty connections");
    ArrayList<Knot> result = new ArrayList<>();

    // Create empty connections
    AnalyzerConnection[][] horizontalConnections = new AnalyzerConnection[gridW - 1][gridH * 2];
    AnalyzerConnection[][] verticalConnections = new AnalyzerConnection[gridW * 2][gridH - 1];
    IndexedSet<AnalyzerConnection> allConnections = new IndexedSet<>();
    for (int x = 0; x < gridW - 1; x++) {
      for (int y = 0; y < gridH; y++) {
        if ((y == 0 || y == gridH - 1) && x % 2 == 0) {
          continue;
        }
        horizontalConnections[x][y * 2] = new AnalyzerConnection(new Vector(x + 1D, y + 0.35D), Math.PI);
        horizontalConnections[x][y * 2 + 1] = new AnalyzerConnection(new Vector(x + 1D, y + 0.65D), 0);
        allConnections.add(horizontalConnections[x][y * 2 + 1]);
      }
    }
    for (int x = 0; x < gridW; x++) {
      for (int y = 0; y < gridH - 1; y++) {
        if ((x == 0 || x == gridW - 1) && y % 2 == 0) {
          continue;
        }
        verticalConnections[x * 2][y] = new AnalyzerConnection(new Vector(x + 0.35D, y + 1D), Math.PI / 2);
        verticalConnections[x * 2 + 1][y] = new AnalyzerConnection(new Vector(x + 0.65D, y + 1D), Math.PI * 1.5);
        allConnections.add(verticalConnections[x * 2][y]);
      }
    }

    // Make tiles set the connections
    timer.nextSegment("connect connections in tiles");
    for (int x = 0; x < gridW; x++) {
      for (int y = 0; y < gridH; y++) {
        grid[x][y].setConnections(x, y, horizontalConnections, verticalConnections);
      }
    }

    // Read one loop at a time, until no loops remain unread
    timer.nextSegment("Read knots");
    while (!allConnections.isEmpty()) {
      result.add(readKnot(allConnections));
    }

    timer.stop();
    return result;
  }

  private static Knot readKnot(IndexedSet<AnalyzerConnection> allConnections) {
    
    AnalyzerConnection firstConnection = allConnections.getAny();
    IndexedSet<AnalyzerIntersection> intersectionsToRemove = new IndexedSet<>();
    Connection current = firstConnection;

    // Loop through the knot
    do {
      // Remove current
      allConnections.remove(current);

      // Add intersections
      while (current.getNext().isIntersected()) {
        current = current.getNext();
        current.setPos(current.getPos().add(0.5, 0.5));
        
        // If new intersection, queue remove. If not new, it is double and thus belongs
        // to the loop
        AnalyzerIntersection ai = (AnalyzerIntersection) current.getIntersection();
        if (intersectionsToRemove.contains(ai)) {
          intersectionsToRemove.remove(ai);
        } else {
          intersectionsToRemove.add(ai);
        }
      }

      // Step
      current = current.getNext();
    } while (current != firstConnection);

    // Remove singular intersections
    for (AnalyzerIntersection i : intersectionsToRemove) {
      if (i.under.getNext() != null) {
        i.under.getPrev().setNext(i.under.getNext());
      }
      if (i.over.getNext() != null) {
        i.over.getPrev().setNext(i.over.getNext());
      }
    }

    return new Knot(firstConnection);
  }

  public int getGridW() {
    return gridW;
  }

  public void setGridW(int gridW) {
    this.gridW = gridW;
  }

  public int getGridH() {
    return gridH;
  }

  public void setGridH(int gridH) {
    this.gridH = gridH;
  }
}
