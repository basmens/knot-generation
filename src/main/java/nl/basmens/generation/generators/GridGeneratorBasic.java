package nl.basmens.generation.generators;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.Random;

import nl.basmens.Main;
import nl.basmens.generation.AbstractTile;
import nl.basmens.generation.Tileset;
import nl.basmens.knot.Connection;
import nl.benmens.processing.PAppletProxy;

public class GridGeneratorBasic implements GridGenerator {
  private static final AbstractTile tileEmpty;
  private static final AbstractTile[] tileCurve = new AbstractTile[4];

  private final Random random = new Random();
  private final Tileset tileset;

  private int gridW;
  private int gridH;
  private AbstractTile[][] grid;

  static {
    // Load tileEmpty and tileCurves
    String path = "";
    try {
      URL resource = GridGeneratorBasic.class.getResource("/");
      path = Paths.get(resource.toURI()).toAbsolutePath().toString() + FileSystems.getDefault().getSeparator();
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }
    if ("".equals(path)) {
      tileEmpty = null;
    } else {
      // Empty
      tileEmpty = new AbstractTile(PAppletProxy.getSharedApplet().loadImage(path + "empty.png")) {
        public int setConnectionInputGoingUp(int x, int y, Connection[][] hor, Connection[][] vert) {
          return -1;
        }

        public int setConnectionInputGoingLeft(int x, int y, Connection[][] hor, Connection[][] vert) {
          return -1;
        }

        public int setConnectionInputGoingDown(int x, int y, Connection[][] hor, Connection[][] vert) {
          return -1;
        }

        public int setConnectionInputGoingRight(int x, int y, Connection[][] hor, Connection[][] vert) {
          return -1;
        }
      };
      // Curve 0
      tileCurve[0] = new AbstractTile(PAppletProxy.getSharedApplet().loadImage(path + "curve0.png")) {
        public int setConnectionInputGoingUp(int x, int y, Connection[][] hor, Connection[][] vert) {
          return -1;
        }
        
        public int setConnectionInputGoingLeft(int x, int y, Connection[][] hor, Connection[][] vert) {
          hor[x][y].setNext(vert[x][y - 1]);
          return 0;
        }
        
        public int setConnectionInputGoingDown(int x, int y, Connection[][] hor, Connection[][] vert) {
          vert[x][y - 1].setNext(hor[x][y]);
          return 1;
        }
        
        public int setConnectionInputGoingRight(int x, int y, Connection[][] hor, Connection[][] vert) {
          return -1;
        }
      };
      // Curve 1
      tileCurve[1] = new AbstractTile(PAppletProxy.getSharedApplet().loadImage(path + "curve1.png")) {
        public int setConnectionInputGoingUp(int x, int y, Connection[][] hor, Connection[][] vert) {
          vert[x][y].setNext(hor[x][y]);
          return 1;
        }
        
        public int setConnectionInputGoingLeft(int x, int y, Connection[][] hor, Connection[][] vert) {
          hor[x][y].setNext(vert[x][y]);
          return 2;
        }
        
        public int setConnectionInputGoingDown(int x, int y, Connection[][] hor, Connection[][] vert) {
          return -1;
        }
        
        public int setConnectionInputGoingRight(int x, int y, Connection[][] hor, Connection[][] vert) {
          return -1;
        }
      };
      // Curve 2
      tileCurve[2] = new AbstractTile(PAppletProxy.getSharedApplet().loadImage(path + "curve2.png")) {
        public int setConnectionInputGoingUp(int x, int y, Connection[][] hor, Connection[][] vert) {
          vert[x][y].setNext(hor[x - 1][y]);
          return 3;
        }
        
        public int setConnectionInputGoingLeft(int x, int y, Connection[][] hor, Connection[][] vert) {
          return -1;
        }
        
        public int setConnectionInputGoingDown(int x, int y, Connection[][] hor, Connection[][] vert) {
          return -1;
        }
        
        public int setConnectionInputGoingRight(int x, int y, Connection[][] hor, Connection[][] vert) {
          hor[x - 1][y].setNext(vert[x][y]);
          return 2;
        }
      };
      // Curve 3
      tileCurve[3] = new AbstractTile(PAppletProxy.getSharedApplet().loadImage(path + "curve3.png")) {
        public int setConnectionInputGoingUp(int x, int y, Connection[][] hor, Connection[][] vert) {
          return -1;
        }
        
        public int setConnectionInputGoingLeft(int x, int y, Connection[][] hor, Connection[][] vert) {
          return -1;
        }
        
        public int setConnectionInputGoingDown(int x, int y, Connection[][] hor, Connection[][] vert) {
          vert[x][y - 1].setNext(hor[x - 1][y]);
          return 3;
        }
        
        public int setConnectionInputGoingRight(int x, int y, Connection[][] hor, Connection[][] vert) {
          hor[x - 1][y].setNext(vert[x][y - 1]);
          return 0;
        }
      };
    }
  }

  public GridGeneratorBasic(Tileset tileset, int gridW, int gridH) {
    this.tileset = tileset;

    this.gridW = gridW;
    this.gridH = gridH;
  }

  public void generateGrid() {
    grid = new AbstractTile[gridW][gridH];

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

  public AbstractTile getTileAtPos(int x, int y) {
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

  public AbstractTile[][] getGrid() {
    return grid;
  }
}
