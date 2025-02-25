package nl.basmens.generation.analyzers;

import java.util.ArrayList;

import nl.basmens.generation.IntersectedConnectionsFactory;
import nl.basmens.generation.Tile;
import nl.basmens.knot.Connection;
import nl.basmens.knot.Knot;
import nl.basmens.utils.collections.IndexedSet;
import nl.basmens.utils.concurrent.PerformanceTimer;
import nl.basmens.utils.maths.Vector;

public class GridAnalyzerBasic implements GridAnalyzer {
  private int gridW;
  private int gridH;

  public ArrayList<Knot> extractKnots(Tile[][] grid) {
    PerformanceTimer timer = new PerformanceTimer(getClass(), "extractKnots", "create empty connections");
    ArrayList<Knot> result = new ArrayList<>();

    // Create empty connections
    IndexAnalyzerConnection[][] horizontalConnections = new IndexAnalyzerConnection[gridW - 1][gridH];
    IndexAnalyzerConnection[][] verticalConnections = new IndexAnalyzerConnection[gridW][gridH - 1];
    IndexedSet<IndexAnalyzerConnection> allConnections = new IndexedSet<>();
    // Hor
    for (int x = 0; x < gridW - 1; x++) {
      for (int y = 0; y < gridH; y++) {
        if ((y == 0 || y == gridH - 1) && x % 2 == 0) {
          continue;
        }
        horizontalConnections[x][y] = new IndexAnalyzerConnection(new Vector(x + 1D, y + 0.5D), 0, x, y, 1);
        allConnections.add(horizontalConnections[x][y]);
      }
    }
    // Vert
    for (int x = 0; x < gridW; x++) {
      for (int y = 0; y < gridH - 1; y++) {
        if ((x == 0 || x == gridW - 1) && y % 2 == 0) {
          continue;
        }
        verticalConnections[x][y] = new IndexAnalyzerConnection(new Vector(x + 0.5D, y + 1D), Math.PI / 2, x, y, 2);
        allConnections.add(verticalConnections[x][y]);
      }
    }

    IntersectedConnectionsFactory intersectedConnections = new IntersectedConnectionsFactory(gridW, gridH);

    timer.nextSegment("Read knots");
    // Read one loop at a time, untill no loops remain unread
    while (!allConnections.isEmpty()) {
      result.add(readKnot(grid, horizontalConnections, verticalConnections, allConnections, intersectedConnections));
    }

    timer.stop();
    return result;
  }

  private static Knot readKnot(Tile[][] grid, IndexAnalyzerConnection[][] horizontalConnections,
      IndexAnalyzerConnection[][] verticalConnections, IndexedSet<IndexAnalyzerConnection> allConnections,
      IntersectedConnectionsFactory intersectedConnections) {

    IndexAnalyzerConnection firstConnection = allConnections.getAny();
    IndexedSet<AnalyzerIntersection> intersectionsToRemove = new IndexedSet<>();

    Connection current = firstConnection;
    int x = ((IndexAnalyzerConnection) current).indexX;
    int y = ((IndexAnalyzerConnection) current).indexY;
    int dir = ((IndexAnalyzerConnection) current).analyzerInitialDir;

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

  private static class IndexAnalyzerConnection extends AnalyzerConnection {
    public final int indexX;
    public final int indexY;
    public final int analyzerInitialDir;

    public IndexAnalyzerConnection(Vector pos, double dir, int indexX, int indexY, int analyzerInitialDir) {
      super(pos, dir);
      this.indexX = indexX;
      this.indexY = indexY;
      this.analyzerInitialDir = analyzerInitialDir;
    }
  }
}
