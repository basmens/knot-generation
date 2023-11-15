package nl.basmens.generation.analyzers;

import java.util.ArrayList;
import java.util.HashSet;

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
    Connection[][] horizontalConnections = new Connection[gridW - 1][gridH * 2];
    Connection[][] verticalConnections = new Connection[gridW * 2][gridH - 1];
    HashSet<Connection> allConnections = new HashSet<>();
    for (int x = 0; x < gridW - 1; x++) {
      for (int y = 0; y < gridH; y++) {
        if ((y == 0 || y == gridH - 1) && x % 2 == 0) {
          continue;
        }
        horizontalConnections[x][y * 2] = new Connection(x + 0.5D, y - 0.15, Math.PI);
        horizontalConnections[x][y * 2 + 1] = new Connection(x + 0.5D, y + 0.15, 0);
        allConnections.add(horizontalConnections[x][y * 2 + 1]);
      }
    }
    for (int x = 0; x < gridW; x++) {
      for (int y = 0; y < gridH - 1; y++) {
        if ((x == 0 || x == gridW - 1) && y % 2 == 0) {
          continue;
        }
        verticalConnections[x * 2][y] = new Connection(x - 0.15, y + 0.5D, Math.PI / 2);
        verticalConnections[x * 2 + 1][y] = new Connection(x + 0.15, y + 0.5D, Math.PI * 1.5);
        allConnections.add(verticalConnections[x * 2][y]);
      }
    }

    int count = 0;
    // Read one loop at a time, untill no loops remain unread
    while (!allConnections.isEmpty()) {
      Connection firstConnection = allConnections.iterator().next();
      HashSet<Intersection> intersectionsToRemove = new HashSet<>();

      Connection current = firstConnection;
      int x = (int) Math.floor(current.getPosX() + 0.2);
      int y = (int) Math.floor(current.getPosY() + 0.2);
      int dir = (current.getDir() / Math.PI < 0.25) ? 1 : 2;
      // If horizontal connection, then dir = right, else dir = down

      // Loop through loop
      do {
        count++;
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
          current = current.getNext();

          // If new intersection, queue remove. If not new, it is double and thus belongs
          // to the loop
          if (intersectionsToRemove.contains(current.getIntersection())) {
            intersectionsToRemove.remove(current.getIntersection());
          } else {
            intersectionsToRemove.add(current.getIntersection());
          }
        }
        allConnections.remove(current);
        current = current.getNext();
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
}
