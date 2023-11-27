package nl.basmens.generation.analyzers;

import java.util.ArrayList;
import java.util.HashSet;

import nl.basmens.IndexedSet;
import nl.basmens.IndexedSet.UuidSetElement;
import nl.basmens.generation.AbstractTile;
import nl.basmens.knot.Connection;
import nl.basmens.knot.Intersection;
import nl.basmens.knot.Knot;

public class GridAnalyzerDouble implements GridAnalyzer {
  private int gridW;
  private int gridH;

  public ArrayList<Knot> extractKnots(AbstractTile[][] grid) {
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
        horizontalConnections[x][y * 2] = new AnalyzerConnection(x + 0.5D, y - 0.15, Math.PI, x, y);
        horizontalConnections[x][y * 2 + 1] = new AnalyzerConnection(x + 0.5D, y + 0.15, 0, x, y);
        allConnections.add(horizontalConnections[x][y * 2 + 1]);
      }
    }
    for (int x = 0; x < gridW; x++) {
      for (int y = 0; y < gridH - 1; y++) {
        if ((x == 0 || x == gridW - 1) && y % 2 == 0) {
          continue;
        }
        verticalConnections[x * 2][y] = new AnalyzerConnection(x - 0.15, y + 0.5D, Math.PI / 2, x, y);
        verticalConnections[x * 2 + 1][y] = new AnalyzerConnection(x + 0.15, y + 0.5D, Math.PI * 1.5, x, y);
        allConnections.add(verticalConnections[x * 2][y]);
      }
    }

    // Read one loop at a time, until no loops remain unread
    while (!allConnections.isEmpty()) {
      AnalyzerConnection firstConnection = allConnections.getAny();
      HashSet<Intersection> intersectionsToRemove = new HashSet<>();

      AnalyzerConnection current = firstConnection;
      int x = current.indexX;
      int y = current.indexY;
      // If horizontal connection, then dir = right, else dir = down
      int dir = (current.getDir() / Math.PI < 0.25) ? 1 : 2;

      // Loop through loop
      do {
        // Step
        switch (dir) {
          case 0:
            y--;
            dir = grid[x][y].setConnectionInputGoingUp(x, y, horizontalConnections, verticalConnections);
            break;
          case 1:
            x++;
            dir = grid[x][y].setConnectionInputGoingRight(x, y, horizontalConnections, verticalConnections);
            break;
          case 2:
            y++;
            dir = grid[x][y].setConnectionInputGoingDown(x, y, horizontalConnections, verticalConnections);
            break;
          case 3:
            x--;
            dir = grid[x][y].setConnectionInputGoingLeft(x, y, horizontalConnections, verticalConnections);
            break;
          default:
            break;
        }

        // Add intersections
        while (current.getNext().isIntersected()) {
          current = (AnalyzerConnection) current.getNext();

          // If new intersection, queue remove. If not new, it is double and thus belongs
          // to the loop
          if (intersectionsToRemove.contains(current.getIntersection())) {
            intersectionsToRemove.remove(current.getIntersection());
          } else {
            intersectionsToRemove.add(current.getIntersection());
          }
        }
        allConnections.remove(current);
        current = (AnalyzerConnection) current.getNext();
      } while (current != firstConnection);

      // Remove singular intersections
      for (Intersection i : intersectionsToRemove) {
        if (i.under != null) {
          i.under.getPrev().setNext(i.under.getNext());
        }
        if (i.over != null) {
          i.over.getPrev().setNext(i.over.getNext());
        }
      }

      result.add(new Knot(firstConnection));
    }

    return result;
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
