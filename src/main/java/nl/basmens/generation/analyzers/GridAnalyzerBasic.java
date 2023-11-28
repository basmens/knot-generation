package nl.basmens.generation.analyzers;

import java.util.ArrayList;
import java.util.HashSet;

import nl.basmens.IndexedSet;
import nl.basmens.IndexedSet.UuidSetElement;
import nl.basmens.generation.IntersectedConnectionsFactory;
import nl.basmens.generation.Tile;
import nl.basmens.knot.Connection;
import nl.basmens.knot.Intersection;
import nl.basmens.knot.Knot;

public class GridAnalyzerBasic implements GridAnalyzer {
  private int gridW;
  private int gridH;

  public ArrayList<Knot> extractKnots(Tile[][] grid) {
    ArrayList<Knot> result = new ArrayList<>();

    // Create empty connections
    AnalyzerConnection[][] horizontalConnections = new AnalyzerConnection[gridW - 1][gridH];
    AnalyzerConnection[][] verticalConnections = new AnalyzerConnection[gridW][gridH - 1];
    IndexedSet<AnalyzerConnection> allConnections = new IndexedSet<>();
    for (int x = 0; x < gridW - 1; x++) {
      for (int y = 0; y < gridH; y++) {
        if ((y == 0 || y == gridH - 1) && x % 2 == 0) {
          continue;
        }
        horizontalConnections[x][y] = new AnalyzerConnection(x + 0.5D, y, 0, x, y);
        allConnections.add(horizontalConnections[x][y]);
      }
    }
    for (int x = 0; x < gridW; x++) {
      for (int y = 0; y < gridH - 1; y++) {
        if ((x == 0 || x == gridW - 1) && y % 2 == 0) {
          continue;
        }
        verticalConnections[x][y] = new AnalyzerConnection(x, y + 0.5D, Math.PI / 2, x, y);
        allConnections.add(verticalConnections[x][y]);
      }
    }

    IntersectedConnectionsFactory intersectedConnections = new IntersectedConnectionsFactory(gridW, gridH);

    // Read one loop at a time, untill no loops remain unread
    while (!allConnections.isEmpty()) {
      result.add(readKnot(grid, horizontalConnections, verticalConnections, allConnections, intersectedConnections));
    }

    return result;
  }

  private static Knot readKnot(Tile[][] grid, AnalyzerConnection[][] horizontalConnections,
      AnalyzerConnection[][] verticalConnections, IndexedSet<AnalyzerConnection> allConnections,
      IntersectedConnectionsFactory intersectedConnections) {
    AnalyzerConnection firstConnection = allConnections.getAny();
    HashSet<Intersection> intersectionsToRemove = new HashSet<>();

    Connection current = firstConnection;
    int x = ((AnalyzerConnection) current).indexX;
    int y = ((AnalyzerConnection) current).indexY;
    int dir = (current.getPosX() % 1 > 0.1) ? 1 : 2; // If horizontal connection, then dir = right, else dir = down

    // Loop through the knot
    do {
      // Step
      switch (dir) {
        case 0:
          y--;
          current.setDir(current.getDir() + Math.PI);
          dir = grid[x][y].setConnectionInputGoingUp(x, y, horizontalConnections, verticalConnections,
              intersectedConnections);
          break;
        case 1:
          x++;
          dir = grid[x][y].setConnectionInputGoingRight(x, y, horizontalConnections, verticalConnections,
              intersectedConnections);
          break;
        case 2:
          y++;
          dir = grid[x][y].setConnectionInputGoingDown(x, y, horizontalConnections, verticalConnections,
              intersectedConnections);
          break;
        case 3:
          x--;
          current.setDir(current.getDir() + Math.PI);
          dir = grid[x][y].setConnectionInputGoingLeft(x, y, horizontalConnections, verticalConnections,
              intersectedConnections);
          break;
        default:
          break;
      }

      // Add intersections
      allConnections.remove(current);
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
    public final int indexX;
    public final int indexY;
    private int setIndex;

    public AnalyzerConnection(double posX, double posY, double dir, int indexX, int indexY) {
      super(posX, posY, dir);
      this.indexX = indexX;
      this.indexY = indexY;
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
