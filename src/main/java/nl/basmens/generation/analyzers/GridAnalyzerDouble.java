package nl.basmens.generation.analyzers;

import java.util.ArrayList;
import java.util.HashSet;

import nl.basmens.IndexedSet;
import nl.basmens.IndexedSet.UuidSetElement;
import nl.basmens.generation.Tile;
import nl.basmens.knot.Connection;
import nl.basmens.knot.Intersection;
import nl.basmens.knot.Knot;

public class GridAnalyzerDouble implements GridAnalyzer {
  private int gridW;
  private int gridH;

  public ArrayList<Knot> extractKnots(Tile[][] grid) {
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
        horizontalConnections[x][y * 2] = new AnalyzerConnection(x + 0.5D, y - 0.15, Math.PI);
        horizontalConnections[x][y * 2 + 1] = new AnalyzerConnection(x + 0.5D, y + 0.15, 0);
        allConnections.add(horizontalConnections[x][y * 2 + 1]);
      }
    }
    for (int x = 0; x < gridW; x++) {
      for (int y = 0; y < gridH - 1; y++) {
        if ((x == 0 || x == gridW - 1) && y % 2 == 0) {
          continue;
        }
        verticalConnections[x * 2][y] = new AnalyzerConnection(x - 0.15, y + 0.5D, Math.PI / 2);
        verticalConnections[x * 2 + 1][y] = new AnalyzerConnection(x + 0.15, y + 0.5D, Math.PI * 1.5);
        allConnections.add(verticalConnections[x * 2][y]);
      }
    }

    // Make tiles set the connections
    for (int x = 0; x < gridW; x++) {
      for (int y = 0; y < gridH; y++) {
        grid[x][y].setConnections(x, y, horizontalConnections, verticalConnections);
      }
    }

    // Read one loop at a time, until no loops remain unread
    while (!allConnections.isEmpty()) {
      result.add(readKnot(allConnections));
    }

    return result;
  }

  private Knot readKnot(IndexedSet<AnalyzerConnection> allConnections) {
    AnalyzerConnection firstConnection = allConnections.getAny();
    HashSet<Intersection> intersectionsToRemove = new HashSet<>();
    Connection current = firstConnection;

    // Loop through the knot
    do {
      // Remove current
      allConnections.remove(current);

      // Add intersections
      while (current.getNext().isIntersected()) {
        current = current.getNext();

        // If new intersection, queue remove. If not new, it is double and thus belongs
        // to the loop
        if (intersectionsToRemove.contains(current.getIntersection())) {
          intersectionsToRemove.remove(current.getIntersection());
        } else {
          intersectionsToRemove.add(current.getIntersection());
        }
      }

      // Step
      current = current.getNext();
    } while (current != firstConnection);

    // Remove singular intersections
    for (Intersection i : intersectionsToRemove) {
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

  private static class AnalyzerConnection extends Connection implements UuidSetElement {
    private int setIndex;

    public AnalyzerConnection(double posX, double posY, double dir) {
      super(posX, posY, dir);
    }

    @Override
    public int getSetIndex() {
      return setIndex;
    }

    @Override
    public void setSetIndex(int uuid) {
      this.setIndex = uuid;
    }
  }
}
