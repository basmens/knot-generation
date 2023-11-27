package nl.basmens;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.ArrayList;

import nl.basmens.generation.KnotGenerationPipeline;
import nl.basmens.generation.AbstractTile;
import nl.basmens.generation.Tileset;
import nl.basmens.generation.analyzers.GridAnalyzerBasic;
import nl.basmens.generation.analyzers.GridAnalyzerDouble;
import nl.basmens.generation.generators.GridGeneratorBasic;
import nl.basmens.generation.generators.GridGeneratorDouble;
import nl.basmens.knot.Connection;
import nl.basmens.knot.Knot;
import nl.benmens.processing.PApplet;
import processing.core.PGraphics;
import processing.opengl.PGraphicsOpenGL;

public class Main extends PApplet {
  public static final boolean SAVE_RESULTS = false;
  public static final boolean MULTI_THREAD = false;
  public static final boolean CURVY_KNOT_DISPLAY = true;

  private Tileset tileset;
  private KnotGenerationPipeline[] knotGenerationPipelines = new KnotGenerationPipeline[1];

  private int gridW = 40;
  private int gridH = 40;

  private int imgRes = 7;

  private String resourcePath;

  private int knotBeingViewed;

  // ===================================================================================================================
  // Native processing functions for lifecycle
  // ===================================================================================================================
  @Override
  public void settings() {
    if (MULTI_THREAD) {
      size(300, 300, P2D);
    } else {
      size(3200, 1600, P2D); // FullScreen
      // size(1800, 1200, P2D);
    }
  }

  @Override
  public void setup() {
    // A workaround for noSmooth() not being compatible with P2D
    ((PGraphicsOpenGL) g).textureSampling(3);
    surface.setLocation(0, 0);

    // Get resource path;
    try {
      URL resource = Main.class.getResource("/");
      resourcePath = Paths.get(resource.toURI()).toAbsolutePath().toString() + FileSystems.getDefault().getSeparator();
    } catch (URISyntaxException e) {
      e.printStackTrace();
      stop();
      return;
    }

    // Set tileset
    // setTilesetToBasicFour();
    // setTilesetToDoubled(1, 1, 1, 1, 1, 1, 1, 1, 1);
    setTilesetToDoubled(0, 1, 1, 1, 1, 0, 1, 1, 2);
    // setTilesetToDoubled(1, 0, 0, 0, 0, 1, 0, 0, 2);

    // Start
    for (int i = 0; i < knotGenerationPipelines.length; i++) {
      // knotGenerationPipelines[i] = new KnotGenerationPipeline(tileset, gridW,
      // gridH, GridGeneratorBasic::new,
      // GridAnalyzerBasic::new, "knots " + gridW + "x" + gridH);

      // knotGenerationPipelines[i] = new KnotGenerationPipeline(tileset, gridW,
      // gridH, GridGeneratorDouble::new,
      // GridAnalyzerDouble::new, "knots " + gridW + "x" + gridH);

      int s = 10 * (knotGenerationPipelines.length - i);
      s = 1500;
      knotGenerationPipelines[i] = new KnotGenerationPipeline(tileset, s, s, GridGeneratorDouble::new,
          GridAnalyzerDouble::new, "knots " + s + "x" + s);

      // startGenerationCycle(i);
    }
  }

  private void startGenerationCycle(int index) {
    if (MULTI_THREAD) {
      Thread t = new Thread(knotGenerationPipelines[index]);
      t.start();
    } else {
      knotGenerationPipelines[index].run();
    }
  }

  boolean toGen = true;
  @Override
  public void draw() {
    background(30);
    if (toGen) {
      startGenerationCycle(0);
      toGen = false;
    }

    if (!MULTI_THREAD) {
      // Draw tiles
      imageMode(CORNER);
      double tileW = (double) width / knotGenerationPipelines[0].getGridW() * 0.8;
      double tileH = (double) height / knotGenerationPipelines[0].getGridH();
      // for (int x = 0; x < knotGenerationPipelines[0].getGridW(); x++) {
      //   for (int y = 0; y < knotGenerationPipelines[0].getGridH(); y++) {
      //     image(knotGenerationPipelines[0].getGenerator().getTileAtPos(x, y).img,
      //         (float) (x * tileW),
      //         (float) (y * tileH), (float) tileW, (float) tileH);
      //   }
      // }

      // View knot on grid
      ArrayList<Knot> knots = knotGenerationPipelines[0].getKnots();
      if (!knots.isEmpty()) {
        Knot knot = knots.get(knotBeingViewed);
        Connection c = knot.getFirstConnection();
        stroke(250, 220, 150, 90);
        strokeWeight((float) (height / 5D / knotGenerationPipelines[0].getGridH()));
        strokeJoin(ROUND);
        noFill();
        beginShape();
        if (CURVY_KNOT_DISPLAY) {
          // Curvy
          double anchorX1 = (c.getPrev().getPosX() + 0.5D) * tileW;
          double anchorY1 = (c.getPrev().getPosY() + 0.5D) * tileH;
          vertex((float) anchorX1, (float) anchorY1);
          do {
            double anchorX2 = (c.getPosX() + 0.5D) * tileW;
            double anchorY2 = (c.getPosY() + 0.5D) * tileH;

            double dir1 = c.getPrev().getDir();
            double dir2 = c.getDir() + Math.PI;

            double controlX1 = anchorX1 + Math.cos(dir1) * tileW * 0.3;
            double controlY1 = anchorY1 + Math.sin(dir1) * tileH * 0.3;
            double controlX2 = anchorX2 + Math.cos(dir2) * tileW * 0.3;
            double controlY2 = anchorY2 + Math.sin(dir2) * tileH * 0.3;

            bezierVertex((float) controlX1, (float) controlY1, (float) controlX2, (float) controlY2, (float) anchorX2,
                (float) anchorY2);
            anchorX1 = anchorX2;
            anchorY1 = anchorY2;

            c = c.getNext();
          } while (c != knot.getFirstConnection());
          endShape();
        } else {
          // Straight
          do {
            double x = (c.getPosX() + 0.5D) * tileW;
            double y = (c.getPosY() + 0.5D) * tileH;
            vertex((float) x, (float) y);
            c = c.getNext();
          } while (c != knot.getFirstConnection());
          endShape(CLOSE);
        }

        // View knot info
        noStroke();
        fill(255);
        textSize(45);
        textAlign(LEFT, TOP);
        text("Displaying knot " + (knotBeingViewed + 1) + "/" + knots.size(), (float) (width * 0.8) + 30, 30);
        textSize(35);
        text(" - Length = " + knot.getLength(), (float) (width * 0.8) + 30, 90);
      }
    }
  }

  // ===================================================================================================================
  // Events
  // ===================================================================================================================
  @Override
  public void mousePressed() {
    if (!MULTI_THREAD) {
      for (int i = 0; i < knotGenerationPipelines.length; i++) {
        startGenerationCycle(i);
      }
    }
  }

  @Override
  public void keyPressed() {
    if (key == 'w') {
      knotBeingViewed = (knotBeingViewed + 1) % knotGenerationPipelines[0].getKnots().size();
      return;
    }
    if (key == 's') {
      knotBeingViewed = (knotBeingViewed - 1 + knotGenerationPipelines[0].getKnots().size())
          % knotGenerationPipelines[0].getKnots().size();
      return;
    }
    if (key == 'f') {
      for (int i = 0; i < knotGenerationPipelines.length; i++) {
        knotGenerationPipelines[i].running = false;
      }
      return;
    }

    // Init save
    println("Saving...");
    PGraphics p = createGraphics(knotGenerationPipelines[0].getGridW() * imgRes,
        knotGenerationPipelines[0].getGridH() * imgRes);
    p.beginDraw();
    p.background(0);

    // Draw Tiles
    p.imageMode(CORNER);
    for (int x = 0; x < knotGenerationPipelines[0].getGridW(); x++) {
      for (int y = 0; y < knotGenerationPipelines[0].getGridH(); y++) {
        p.image(knotGenerationPipelines[0].getGenerator().getTileAtPos(x, y).img, x * imgRes, y * imgRes, imgRes,
            imgRes);
      }
    }

    // Save
    p.endDraw();
    String path = resourcePath.substring(0, resourcePath.length() - "target/classes/".length());
    p.save(path + "results/gen result.png");
    println("Saved");
  }

  // ===================================================================================================================
  // Tileset basic four
  // ===================================================================================================================
  private void setTilesetToBasicFour() {
    ArrayList<AbstractTile> tiles = new ArrayList<>();

    String path = resourcePath + "tilesets/basic_four/";

    AbstractTile t = new AbstractTile(loadImage(path + "corner0.png")) {
      public int setConnectionInputGoingUp(int x, int y, Connection[][] hor, Connection[][] vert) {
        vert[x][y].setNext(hor[x - 1][y]);
        return 3;
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
        hor[x - 1][y].setNext(vert[x][y]);
        return 2;
      }
    };
    tiles.add(t);

    t = new AbstractTile(loadImage(path + "corner1.png")) {
      public int setConnectionInputGoingUp(int x, int y, Connection[][] hor, Connection[][] vert) {
        vert[x][y].setNext(hor[x][y]);
        return 1;
      }

      public int setConnectionInputGoingLeft(int x, int y, Connection[][] hor, Connection[][] vert) {
        hor[x][y].setNext(vert[x][y]);
        return 2;
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
    tiles.add(t);

    t = new AbstractTile(loadImage(path + "intersection0.png")) {
      public int setConnectionInputGoingUp(int x, int y, Connection[][] hor, Connection[][] vert) {
        vert[x][y].setNext(vert[x][y - 1]);
        return 0;
      }

      public int setConnectionInputGoingLeft(int x, int y, Connection[][] hor, Connection[][] vert) {
        hor[x][y].setNext(hor[x - 1][y]);
        return 3;
      }

      public int setConnectionInputGoingDown(int x, int y, Connection[][] hor, Connection[][] vert) {
        vert[x][y - 1].setNext(vert[x][y]);
        return 2;
      }

      public int setConnectionInputGoingRight(int x, int y, Connection[][] hor, Connection[][] vert) {
        hor[x - 1][y].setNext(hor[x][y]);
        return 1;
      }
    };
    tiles.add(t);

    t = new AbstractTile(loadImage(path + "intersection1.png")) {
      public int setConnectionInputGoingUp(int x, int y, Connection[][] hor, Connection[][] vert) {
        vert[x][y].setNext(vert[x][y - 1]);
        return 0;
      }

      public int setConnectionInputGoingLeft(int x, int y, Connection[][] hor, Connection[][] vert) {
        hor[x][y].setNext(hor[x - 1][y]);
        return 3;
      }

      public int setConnectionInputGoingDown(int x, int y, Connection[][] hor, Connection[][] vert) {
        vert[x][y - 1].setNext(vert[x][y]);
        return 2;
      }

      public int setConnectionInputGoingRight(int x, int y, Connection[][] hor, Connection[][] vert) {
        hor[x - 1][y].setNext(hor[x][y]);
        return 1;
      }
    };
    tiles.add(t);

    tileset = new Tileset(tiles.toArray(AbstractTile[]::new));
  }

  // ===================================================================================================================
  // Tileset doubled
  // ===================================================================================================================
  private void setTilesetToDoubled(int rrrr, int rlrl, int rlbb, int lrlr, int lbbr, int llll, int brlb, int bbrl,
      int bbbb) {
    ArrayList<AbstractTile> tiles = new ArrayList<>();

    String path = resourcePath + "tilesets/doubled/";

    // rrrr
    AbstractTile t = new AbstractTile(loadImage(path + "rrrr.png")) {
      public int setConnectionInputGoingUp(int x, int y, Connection[][] hor, Connection[][] vert) {
        vert[x * 2 + 1][y].setNext(hor[x][y * 2 + 1]);
        return 1;
      }

      public int setConnectionInputGoingLeft(int x, int y, Connection[][] hor, Connection[][] vert) {
        hor[x][y * 2].setNext(vert[x * 2 + 1][y - 1]);
        return 0;
      }

      public int setConnectionInputGoingDown(int x, int y, Connection[][] hor, Connection[][] vert) {
        vert[x * 2][y - 1].setNext(hor[x - 1][y * 2]);
        return 3;
      }

      public int setConnectionInputGoingRight(int x, int y, Connection[][] hor, Connection[][] vert) {
        hor[x - 1][y * 2 + 1].setNext(vert[x * 2][y]);
        return 2;
      }
    };
    for (int i = 0; i < rrrr; i++) {
      tiles.add(t);
    }

    // rlrl
    t = new AbstractTile(loadImage(path + "rlrl.png")) {
      public int setConnectionInputGoingUp(int x, int y, Connection[][] hor, Connection[][] vert) {
        vert[x * 2 + 1][y].setNext(hor[x][y * 2 + 1]);
        return 1;
      }

      public int setConnectionInputGoingLeft(int x, int y, Connection[][] hor, Connection[][] vert) {
        hor[x][y * 2].setNext(vert[x * 2][y]);
        return 2;
      }

      public int setConnectionInputGoingDown(int x, int y, Connection[][] hor, Connection[][] vert) {
        vert[x * 2][y - 1].setNext(hor[x - 1][y * 2]);
        return 3;
      }

      public int setConnectionInputGoingRight(int x, int y, Connection[][] hor, Connection[][] vert) {
        hor[x - 1][y * 2 + 1].setNext(vert[x * 2 + 1][y - 1]);
        return 0;
      }
    };
    for (int i = 0; i < rlrl; i++) {
      tiles.add(t);
    }

    // rlbb
    t = new AbstractTile(loadImage(path + "rlbb.png")) {
      public int setConnectionInputGoingUp(int x, int y, Connection[][] hor, Connection[][] vert) {
        vert[x * 2 + 1][y].setNext(hor[x][y * 2 + 1]);
        return 1;
      }

      public int setConnectionInputGoingLeft(int x, int y, Connection[][] hor, Connection[][] vert) {
        hor[x][y * 2].setNext(vert[x * 2][y]);
        return 2;
      }

      public int setConnectionInputGoingDown(int x, int y, Connection[][] hor, Connection[][] vert) {
        vert[x * 2][y - 1].setNext(vert[x * 2 + 1][y - 1]);
        return 0;
      }

      public int setConnectionInputGoingRight(int x, int y, Connection[][] hor, Connection[][] vert) {
        hor[x - 1][y * 2 + 1].setNext(hor[x - 1][y * 2]);
        return 3;
      }
    };
    for (int i = 0; i < rlbb; i++) {
      tiles.add(t);
    }

    // lrlr
    t = new AbstractTile(loadImage(path + "lrlr.png")) {
      public int setConnectionInputGoingUp(int x, int y, Connection[][] hor, Connection[][] vert) {
        vert[x * 2 + 1][y].setNext(hor[x - 1][y * 2]);
        return 3;
      }

      public int setConnectionInputGoingLeft(int x, int y, Connection[][] hor, Connection[][] vert) {
        hor[x][y * 2].setNext(vert[x * 2 + 1][y - 1]);
        return 0;
      }

      public int setConnectionInputGoingDown(int x, int y, Connection[][] hor, Connection[][] vert) {
        vert[x * 2][y - 1].setNext(hor[x][y * 2 + 1]);
        return 1;
      }

      public int setConnectionInputGoingRight(int x, int y, Connection[][] hor, Connection[][] vert) {
        hor[x - 1][y * 2 + 1].setNext(vert[x * 2][y]);
        return 2;
      }
    };
    for (int i = 0; i < lrlr; i++) {
      tiles.add(t);
    }

    // lbbr
    t = new AbstractTile(loadImage(path + "lbbr.png")) {
      public int setConnectionInputGoingUp(int x, int y, Connection[][] hor, Connection[][] vert) {
        vert[x * 2 + 1][y].setNext(hor[x - 1][y * 2]);
        return 3;
      }

      public int setConnectionInputGoingLeft(int x, int y, Connection[][] hor, Connection[][] vert) {
        hor[x][y * 2].setNext(hor[x][y * 2 + 1]);
        return 1;
      }

      public int setConnectionInputGoingDown(int x, int y, Connection[][] hor, Connection[][] vert) {
        vert[x * 2][y - 1].setNext(vert[x * 2 + 1][y - 1]);
        return 0;
      }

      public int setConnectionInputGoingRight(int x, int y, Connection[][] hor, Connection[][] vert) {
        hor[x - 1][y * 2 + 1].setNext(vert[x * 2][y]);
        return 2;
      }
    };
    for (int i = 0; i < lbbr; i++) {
      tiles.add(t);
    }

    // llll
    t = new AbstractTile(loadImage(path + "llll.png")) {
      public int setConnectionInputGoingUp(int x, int y, Connection[][] hor, Connection[][] vert) {
        vert[x * 2 + 1][y].setNext(hor[x - 1][y * 2]);
        return 3;
      }

      public int setConnectionInputGoingLeft(int x, int y, Connection[][] hor, Connection[][] vert) {
        hor[x][y * 2].setNext(vert[x * 2][y]);
        return 2;
      }

      public int setConnectionInputGoingDown(int x, int y, Connection[][] hor, Connection[][] vert) {
        vert[x * 2][y - 1].setNext(hor[x][y * 2 + 1]);
        return 1;
      }

      public int setConnectionInputGoingRight(int x, int y, Connection[][] hor, Connection[][] vert) {
        hor[x - 1][y * 2 + 1].setNext(vert[x * 2 + 1][y - 1]);
        return 0;
      }
    };
    for (int i = 0; i < llll; i++) {
      tiles.add(t);
    }

    // brlb
    t = new AbstractTile(loadImage(path + "brlb.png")) {
      public int setConnectionInputGoingUp(int x, int y, Connection[][] hor, Connection[][] vert) {
        vert[x * 2 + 1][y].setNext(vert[x * 2][y]);
        return 2;
      }

      public int setConnectionInputGoingLeft(int x, int y, Connection[][] hor, Connection[][] vert) {
        hor[x][y * 2].setNext(vert[x * 2 + 1][y - 1]);
        return 0;
      }

      public int setConnectionInputGoingDown(int x, int y, Connection[][] hor, Connection[][] vert) {
        vert[x * 2][y - 1].setNext(hor[x][y * 2 + 1]);
        return 1;
      }

      public int setConnectionInputGoingRight(int x, int y, Connection[][] hor, Connection[][] vert) {
        hor[x - 1][y * 2 + 1].setNext(hor[x - 1][y * 2]);
        return 3;
      }
    };
    for (int i = 0; i < brlb; i++) {
      tiles.add(t);
    }

    // bbrl
    t = new AbstractTile(loadImage(path + "bbrl.png")) {
      public int setConnectionInputGoingUp(int x, int y, Connection[][] hor, Connection[][] vert) {
        vert[x * 2 + 1][y].setNext(vert[x * 2][y]);
        return 2;
      }

      public int setConnectionInputGoingLeft(int x, int y, Connection[][] hor, Connection[][] vert) {
        hor[x][y * 2].setNext(hor[x][y * 2 + 1]);
        return 1;
      }

      public int setConnectionInputGoingDown(int x, int y, Connection[][] hor, Connection[][] vert) {
        vert[x * 2][y - 1].setNext(hor[x - 1][y * 2]);
        return 3;
      }

      public int setConnectionInputGoingRight(int x, int y, Connection[][] hor, Connection[][] vert) {
        hor[x - 1][y * 2 + 1].setNext(vert[x * 2 + 1][y - 1]);
        return 0;
      }
    };
    for (int i = 0; i < bbrl; i++) {
      tiles.add(t);
    }

    // bbbb
    t = new AbstractTile(loadImage(path + "bbbb.png")) {
      public int setConnectionInputGoingUp(int x, int y, Connection[][] hor, Connection[][] vert) {
        vert[x * 2 + 1][y].setNext(vert[x * 2][y]);
        return 2;
      }

      public int setConnectionInputGoingLeft(int x, int y, Connection[][] hor, Connection[][] vert) {
        hor[x][y * 2].setNext(hor[x][y * 2 + 1]);
        return 1;
      }

      public int setConnectionInputGoingDown(int x, int y, Connection[][] hor, Connection[][] vert) {
        vert[x * 2][y - 1].setNext(vert[x * 2 + 1][y - 1]);
        return 0;
      }

      public int setConnectionInputGoingRight(int x, int y, Connection[][] hor, Connection[][] vert) {
        hor[x - 1][y * 2 + 1].setNext(hor[x - 1][y * 2]);
        return 3;
      }
    };
    for (int i = 0; i < bbbb; i++) {
      tiles.add(t);
    }

    tileset = new Tileset(tiles.toArray(AbstractTile[]::new));
  }

  // ===================================================================================================================
  // Main function
  // ===================================================================================================================
  public static void main(String[] passedArgs) {
    if (passedArgs != null) {
      PApplet.main(new Object() {
      }.getClass().getEnclosingClass(), passedArgs);
    } else {
      PApplet.main(new Object() {
      }.getClass().getEnclosingClass());
    }
  }
}
