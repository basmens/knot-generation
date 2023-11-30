package nl.basmens.generation.generators;

import java.util.Random;

import nl.basmens.Main;
import nl.basmens.generation.Tile;
import nl.basmens.generation.Tileset;
import nl.basmens.knot.Connection;
import nl.benmens.processing.PAppletProxy;

public class GridGeneratorDouble implements GridGenerator {
  private static final Tile tileEmpty;
  private static final Tile[] tileCurve = new Tile[4];

  private final Random random = new Random();
  private final Tileset tileset;

  private int gridW;
  private int gridH;
  private Tile[][] grid;

  static {
    String path = Main.RESOURCE_PATH;

    // Empty
    tileEmpty = new Tile(PAppletProxy.getSharedApplet().loadImage(path + "empty.png")) {
      @Override
      public void setConnections(int x, int y, Connection[][] hor, Connection[][] vert) {
        // No connections to be made
      }
    };
    // Curve 0
    tileCurve[0] = new Tile(PAppletProxy.getSharedApplet().loadImage(path + "curve0_double.png")) {
      @Override
      public void setConnections(int x, int y, Connection[][] hor, Connection[][] vert) {
        hor[x][y * 2].setNext(vert[x * 2 + 1][y - 1]);
        vert[x * 2][y - 1].setNext(hor[x][y * 2 + 1]);
      }
    };
    // Curve 1
    tileCurve[1] = new Tile(PAppletProxy.getSharedApplet().loadImage(path + "curve1_double.png")) {
      @Override
      public void setConnections(int x, int y, Connection[][] hor, Connection[][] vert) {
        vert[x * 2 + 1][y].setNext(hor[x][y * 2 + 1]);
        hor[x][y * 2].setNext(vert[x * 2][y]);
      }
    };
    // Curve 2
    tileCurve[2] = new Tile(PAppletProxy.getSharedApplet().loadImage(path + "curve2_double.png")) {
      @Override
      public void setConnections(int x, int y, Connection[][] hor, Connection[][] vert) {
        vert[x * 2 + 1][y].setNext(hor[x - 1][y * 2]);
        hor[x - 1][y * 2 + 1].setNext(vert[x * 2][y]);
      }
    };
    // Curve 3
    tileCurve[3] = new Tile(PAppletProxy.getSharedApplet().loadImage(path + "curve3_double.png")) {
      @Override
      public void setConnections(int x, int y, Connection[][] hor, Connection[][] vert) {
        vert[x * 2][y - 1].setNext(hor[x - 1][y * 2]);
        hor[x - 1][y * 2 + 1].setNext(vert[x * 2 + 1][y - 1]);
      }
    };
  }

  public GridGeneratorDouble(Tileset tileset) {
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
