package nl.basmens.generation.generators;

import java.util.Random;

import nl.basmens.Main;
import nl.basmens.generation.IntersectedConnectionsFactory;
import nl.basmens.generation.Tile;
import nl.basmens.generation.Tileset;
import nl.basmens.knot.Connection;
import nl.benmens.processing.PAppletProxy;

public class GridGeneratorBasic implements GridGenerator {
  public static final Tile tileEmpty;
  private static final Tile[] tileCurve = new Tile[4];

  private final Random random = new Random();
  private final Tileset tileset;

  private int gridW;
  private int gridH;
  private Tile[][] grid;

  static {
    // Load tileEmpty and tileCurves
    String path = Main.RESOURCE_PATH;
    if ("".equals(path)) {
      tileEmpty = null;
    } else {
      // Empty
      tileEmpty = new Tile(PAppletProxy.getSharedApplet().loadImage(path + "empty.png")) {
      };
      // Curve 0
      tileCurve[0] = new Tile(PAppletProxy.getSharedApplet().loadImage(path + "curve0.png")) {
        @Override
        public int setConnectionInputGoingLeft(int x, int y, Connection[][] hor, Connection[][] vert,
            IntersectedConnectionsFactory intersectedConnections) {
          hor[x][y].setNext(vert[x][y - 1]);
          return 0;
        }

        @Override
        public int setConnectionInputGoingDown(int x, int y, Connection[][] hor, Connection[][] vert,
            IntersectedConnectionsFactory intersectedConnections) {
          vert[x][y - 1].setNext(hor[x][y]);
          return 1;
        }
      };
      // Curve 1
      tileCurve[1] = new Tile(PAppletProxy.getSharedApplet().loadImage(path + "curve1.png")) {
        @Override
        public int setConnectionInputGoingUp(int x, int y, Connection[][] hor, Connection[][] vert,
            IntersectedConnectionsFactory intersectedConnections) {
          vert[x][y].setNext(hor[x][y]);
          return 1;
        }

        @Override
        public int setConnectionInputGoingLeft(int x, int y, Connection[][] hor, Connection[][] vert,
            IntersectedConnectionsFactory intersectedConnections) {
          hor[x][y].setNext(vert[x][y]);
          return 2;
        }
      };
      // Curve 2
      tileCurve[2] = new Tile(PAppletProxy.getSharedApplet().loadImage(path + "curve2.png")) {
        @Override
        public int setConnectionInputGoingUp(int x, int y, Connection[][] hor, Connection[][] vert,
            IntersectedConnectionsFactory intersectedConnections) {
          vert[x][y].setNext(hor[x - 1][y]);
          return 3;
        }

        @Override
        public int setConnectionInputGoingRight(int x, int y, Connection[][] hor, Connection[][] vert,
            IntersectedConnectionsFactory intersectedConnections) {
          hor[x - 1][y].setNext(vert[x][y]);
          return 2;
        }
      };
      // Curve 3
      tileCurve[3] = new Tile(PAppletProxy.getSharedApplet().loadImage(path + "curve3.png")) {
        @Override
        public int setConnectionInputGoingDown(int x, int y, Connection[][] hor, Connection[][] vert,
            IntersectedConnectionsFactory intersectedConnections) {
          vert[x][y - 1].setNext(hor[x - 1][y]);
          return 3;
        }

        @Override
        public int setConnectionInputGoingRight(int x, int y, Connection[][] hor, Connection[][] vert,
            IntersectedConnectionsFactory intersectedConnections) {
          hor[x - 1][y].setNext(vert[x][y - 1]);
          return 0;
        }
      };
    }
  }

  public GridGeneratorBasic(Tileset tileset) {
    this.tileset = tileset;
  }

  public void generateGrid() {
    grid = new Tile[gridW][gridH];

    // Fill edges
    for (int x = 1; x < gridW - 1; x++) {
      grid[x][0] = tileCurve[1 + (x + 1) % 2];
      grid[x][gridH - 1] = tileCurve[(x + 1) % 2 * 3];
    }
    for (int y = 1; y < gridH - 1; y++) {
      grid[0][y] = tileCurve[y % 2];
      grid[gridW - 1][y] = tileCurve[2 + (y + 1) % 2];
    }
    grid[0][0] = tileEmpty;
    grid[0][gridH - 1] = tileEmpty;
    grid[gridW - 1][0] = tileEmpty;
    grid[gridW - 1][gridH - 1] = tileEmpty;

    // Fill center
    for (int x = 1; x < gridW - 1; x++) {
      for (int y = 1; y < gridH - 1; y++) {
        grid[x][y] = tileset.getTileByIndex(random.nextInt(tileset.getTileCount()));
      }
    }
  }

  public Tile getTileAtPos(int x, int y) {
    return grid[x][y];
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

  public Tile[][] getGrid() {
    return grid;
  }
}
