package nl.basmens;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Locale;
import java.util.function.Supplier;

import nl.basmens.generation.IntersectedConnectionsFactory;
import nl.basmens.generation.KnotGenerationPipeline;
import nl.basmens.generation.Tile;
import nl.basmens.generation.Tileset;
import nl.basmens.generation.analyzers.GridAnalyzerDouble;
import nl.basmens.generation.generators.GridGeneratorDouble;
import nl.basmens.knot.Connection;
import nl.basmens.knot.Knot;
import nl.benmens.processing.PApplet;
import nl.benmens.processing.PAppletProxy;
import processing.core.PGraphics;
import processing.opengl.PGraphicsOpenGL;

public class Main extends PApplet {
  public static final String RESOURCE_PATH;

  public static final boolean SAVE_RESULTS = false;
  public static final boolean MULTI_THREAD = false;
  public static final boolean CURVY_KNOT_DISPLAY = true;
  private static final Tilesets TILESET = Tilesets.UNWEIGHTED;
  private int imgRes = 17;

  private enum Tilesets {
    BASIC(Main::getTilesetBasicFour),
    UNWEIGHTED(() -> getTilesetDoubled(1, 1, 1, 1, 1, 1, 1, 1, 1)),
    WEIGHTED_HIGH(() -> getTilesetDoubled(0, 1, 1, 1, 1, 0, 1, 1, 2)),
    WEIGHTED_LOW(() -> getTilesetDoubled(1, 0, 0, 0, 0, 1, 0, 0, 2));

    private final Supplier<Tileset> tilesetSupplier;
    private Tileset tileset;

    Tilesets(Supplier<Tileset> tilesetSupplier) {
      this.tilesetSupplier = tilesetSupplier;
    }

    public Tileset getTileset() {
      if (tileset == null) {
        tileset = tilesetSupplier.get();
      }
      return tileset;
    }
  }

  private KnotGenerationPipeline[] knotGenerationPipelines = new KnotGenerationPipeline[1];
  private Thread[] knotGenerationPipelineThreads = new Thread[knotGenerationPipelines.length];

  private int knotBeingViewed;

  static {
    String path = "";
    try {
      URL resource = Main.class.getResource("/");
      path = Paths.get(resource.toURI()).toAbsolutePath().toString() + FileSystems.getDefault().getSeparator();
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }
    RESOURCE_PATH = path;
  }

  // ===================================================================================================================
  // Native processing functions for lifecycle
  // ===================================================================================================================
  @Override
  public void settings() {
    PAppletProxy.setSharedApplet(this);

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

    // Start
    for (int i = 0; i < knotGenerationPipelines.length; i++) {
      int s = 10 * (knotGenerationPipelines.length - i);
      s = 8;

      String fileName = "knots tileset " + TILESET.toString().toLowerCase(Locale.ENGLISH) + "/knots " + s + "x" + s;

      // Basic
      // knotGenerationPipelines[i] = new KnotGenerationPipeline(TILESET.getTileset(), s, s,
      //     GridGeneratorBasic::new, GridAnalyzerBasic::new, fileName);

      // Double
      knotGenerationPipelines[i] = new KnotGenerationPipeline(TILESET.getTileset(), s, s,
          GridGeneratorDouble::new, GridAnalyzerDouble::new, fileName);

      startGenerationCycle(i);
    }
  }

  private void startGenerationCycle(int index) {
    knotBeingViewed = 0;

    if (MULTI_THREAD) {
      knotGenerationPipelineThreads[index] = new Thread(knotGenerationPipelines[index]);
      knotGenerationPipelineThreads[index].start();
    } else {
      knotGenerationPipelines[index].run();
    }
  }

  @Override
  public void draw() {
    background(30);

    if (!MULTI_THREAD) {
      // Draw tiles
      double tileW = (double) width / knotGenerationPipelines[0].getGridW() * 0.8;
      double tileH = (double) height / knotGenerationPipelines[0].getGridH();
      if (knotGenerationPipelines[0].getGridW() <= 300) {
        imageMode(CORNER);
        for (int x = 0; x < knotGenerationPipelines[0].getGridW(); x++) {
          for (int y = 0; y < knotGenerationPipelines[0].getGridH(); y++) {
            image(knotGenerationPipelines[0].getGenerator().getTileAtPos(x, y).img,
                (float) (x * tileW),
                (float) (y * tileH), (float) tileW, (float) tileH);
          }
        }
      }

      // View knot on grid
      ArrayList<Knot> knots = knotGenerationPipelines[0].getKnots();
      if (!knots.isEmpty()) {
        Knot knot = knots.get(knotBeingViewed);
        Connection c = knot.getFirstConnection();
        stroke(250, 220, 150, 90);
        strokeWeight((float) (height / 6D / knotGenerationPipelines[0].getGridH()));
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

            double dx = c.getPosX() - c.getPrev().getPosX();
            double dy = c.getPosY() - c.getPrev().getPosY();
            double controlPointDist = Math.sqrt(dx * dx + dy * dy) * 0.35;
            if (Math.abs(angleDifference(dir1, dir2)) < 0.1) {
              controlPointDist *= 2;
            }

            double controlX1 = anchorX1 + Math.cos(dir1) * tileW * controlPointDist;
            double controlY1 = anchorY1 + Math.sin(dir1) * tileH * controlPointDist;
            double controlX2 = anchorX2 + Math.cos(dir2) * tileW * controlPointDist;
            double controlY2 = anchorY2 + Math.sin(dir2) * tileH * controlPointDist;

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
        text(" - Intersection # = " + knot.getIntersections().size(), (float) (width * 0.8) + 30, 130);
      }
    }
  }

  public static double angleDifference(double a1, double a2) {
    double dif = a1 - a2;
    dif %= Math.PI * 2;
    dif += Math.PI * 3;
    dif %= Math.PI * 2;
    dif -= Math.PI;
    return dif;
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
      println("Finishing...");
      // Terminate threads
      for (KnotGenerationPipeline p : knotGenerationPipelines) {
        p.stop();
      }

      // Wait for the threads to end
      for (Thread t : knotGenerationPipelineThreads) {
        while (t.isAlive()) {
          try {
            Thread.sleep(10);
          } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
          }
        }
      }
      exit();
      println("Finished");
      return;
    }

    if (!MULTI_THREAD) {
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
      String path = RESOURCE_PATH.substring(0, RESOURCE_PATH.length() - "target/classes/".length());
      p.save(path + "results/gen result.png");
      println("Saved");
    }
  }

  // ===================================================================================================================
  // Tileset basic four
  // ===================================================================================================================
  private static Tileset getTilesetBasicFour() {
    ArrayList<Tile> tiles = new ArrayList<>();
    String path = RESOURCE_PATH + "tilesets/basic_four/";

    tiles.add(new Tile(PAppletProxy.loadImage(path + "corner0.png")) {
      @Override
      public int setConnectionInputGoingUp(int x, int y, Connection[][] hor, Connection[][] vert,
          IntersectedConnectionsFactory intersectedConnections) {
        vert[x][y].setNext(hor[x - 1][y]);
        return 3;
      }

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

      @Override
      public int setConnectionInputGoingRight(int x, int y, Connection[][] hor, Connection[][] vert,
          IntersectedConnectionsFactory intersectedConnections) {
        hor[x - 1][y].setNext(vert[x][y]);
        return 2;
      }
    });

    tiles.add(new Tile(PAppletProxy.loadImage(path + "corner1.png")) {
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
    });

    tiles.add(new Tile(PAppletProxy.loadImage(path + "intersection0.png")) {
      @Override
      public int setConnectionInputGoingUp(int x, int y, Connection[][] hor, Connection[][] vert,
          IntersectedConnectionsFactory intersectedConnections) {
        Connection c = intersectedConnections.getConnection(x, y, 0);
        c.setDir(-Math.PI / 2);
        vert[x][y].setNext(c);
        c.setNext(vert[x][y - 1]);
        return 0;
      }

      @Override
      public int setConnectionInputGoingLeft(int x, int y, Connection[][] hor, Connection[][] vert,
          IntersectedConnectionsFactory intersectedConnections) {
        Connection c = intersectedConnections.getConnection(x, y, 1);
        c.setDir(Math.PI);
        hor[x][y].setNext(c);
        c.setNext(hor[x - 1][y]);
        return 3;
      }

      @Override
      public int setConnectionInputGoingDown(int x, int y, Connection[][] hor, Connection[][] vert,
          IntersectedConnectionsFactory intersectedConnections) {
        Connection c = intersectedConnections.getConnection(x, y, 0);
        c.setDir(Math.PI / 2);
        vert[x][y - 1].setNext(c);
        c.setNext(vert[x][y]);
        return 2;
      }

      @Override
      public int setConnectionInputGoingRight(int x, int y, Connection[][] hor, Connection[][] vert,
          IntersectedConnectionsFactory intersectedConnections) {
        Connection c = intersectedConnections.getConnection(x, y, 1);
        c.setDir(0);
        hor[x - 1][y].setNext(c);
        c.setNext(hor[x][y]);
        return 1;
      }
    });

    tiles.add(new Tile(PAppletProxy.loadImage(path + "intersection1.png")) {
      @Override
      public int setConnectionInputGoingUp(int x, int y, Connection[][] hor, Connection[][] vert,
          IntersectedConnectionsFactory intersectedConnections) {
        Connection c = intersectedConnections.getConnection(x, y, 0);
        c.setDir(-Math.PI / 2);
        vert[x][y].setNext(c);
        c.setNext(vert[x][y - 1]);
        return 0;
      }

      @Override
      public int setConnectionInputGoingLeft(int x, int y, Connection[][] hor, Connection[][] vert,
          IntersectedConnectionsFactory intersectedConnections) {
        Connection c = intersectedConnections.getConnection(x, y, 1);
        c.setDir(Math.PI);
        hor[x][y].setNext(c);
        c.setNext(hor[x - 1][y]);
        return 3;
      }

      @Override
      public int setConnectionInputGoingDown(int x, int y, Connection[][] hor, Connection[][] vert,
          IntersectedConnectionsFactory intersectedConnections) {
        Connection c = intersectedConnections.getConnection(x, y, 0);
        c.setDir(Math.PI / 2);
        vert[x][y - 1].setNext(c);
        c.setNext(vert[x][y]);
        return 2;
      }

      @Override
      public int setConnectionInputGoingRight(int x, int y, Connection[][] hor, Connection[][] vert,
          IntersectedConnectionsFactory intersectedConnections) {
        Connection c = intersectedConnections.getConnection(x, y, 1);
        c.setDir(0);
        hor[x - 1][y].setNext(c);
        c.setNext(hor[x][y]);
        return 1;
      }
    });

    return new Tileset(tiles.toArray(Tile[]::new));
  }

  // ===================================================================================================================
  // Tileset doubled
  // ===================================================================================================================
  private static Tileset getTilesetDoubled(int rrrr, int rlrl, int rlbb, int lrlr, int lbbr, int llll, int brlb,
      int bbrl, int bbbb) {
    ArrayList<Tile> tiles = new ArrayList<>();
    String path = RESOURCE_PATH + "tilesets/doubled/";

    // rrrr
    for (int i = 0; i < rrrr; i++) {
      tiles.add(new Tile(PAppletProxy.loadImage(path + "rrrr.png")) {
        @Override
        public void setConnections(int x, int y, Connection[][] hor, Connection[][] vert) {
          vert[x * 2 + 1][y].setNext(hor[x][y * 2 + 1]);
          hor[x][y * 2].setNext(vert[x * 2 + 1][y - 1]);
          vert[x * 2][y - 1].setNext(hor[x - 1][y * 2]);
          hor[x - 1][y * 2 + 1].setNext(vert[x * 2][y]);
        }
      });
    }

    // rlrl
    for (int i = 0; i < rlrl; i++) {
      tiles.add(new Tile(PAppletProxy.loadImage(path + "rlrl.png")) {
        @Override
        public void setConnections(int x, int y, Connection[][] hor, Connection[][] vert) {
          vert[x * 2 + 1][y].setNext(hor[x][y * 2 + 1]);
          hor[x][y * 2].setNext(vert[x * 2][y]);
          vert[x * 2][y - 1].setNext(hor[x - 1][y * 2]);
          hor[x - 1][y * 2 + 1].setNext(vert[x * 2 + 1][y - 1]);
        }
      });
    }

    // rlbb
    for (int i = 0; i < rlbb; i++) {
      tiles.add(new Tile(PAppletProxy.loadImage(path + "rlbb.png")) {
        @Override
        public void setConnections(int x, int y, Connection[][] hor, Connection[][] vert) {
          vert[x * 2 + 1][y].setNext(hor[x][y * 2 + 1]);
          hor[x][y * 2].setNext(vert[x * 2][y]);
          vert[x * 2][y - 1].setNext(vert[x * 2 + 1][y - 1]);
          hor[x - 1][y * 2 + 1].setNext(hor[x - 1][y * 2]);
        }
      });
    }

    // lrlr
    for (int i = 0; i < lrlr; i++) {
      tiles.add(new Tile(PAppletProxy.loadImage(path + "lrlr.png")) {
        @Override
        public void setConnections(int x, int y, Connection[][] hor, Connection[][] vert) {
          vert[x * 2 + 1][y].setNext(hor[x - 1][y * 2]);
          hor[x][y * 2].setNext(vert[x * 2 + 1][y - 1]);
          vert[x * 2][y - 1].setNext(hor[x][y * 2 + 1]);
          hor[x - 1][y * 2 + 1].setNext(vert[x * 2][y]);
        }
      });
    }

    // lbbr
    for (int i = 0; i < lbbr; i++) {
      tiles.add(new Tile(PAppletProxy.loadImage(path + "lbbr.png")) {
        @Override
        public void setConnections(int x, int y, Connection[][] hor, Connection[][] vert) {
          vert[x * 2 + 1][y].setNext(hor[x - 1][y * 2]);
          hor[x][y * 2].setNext(hor[x][y * 2 + 1]);
          vert[x * 2][y - 1].setNext(vert[x * 2 + 1][y - 1]);
          hor[x - 1][y * 2 + 1].setNext(vert[x * 2][y]);
        }
      });
    }

    // llll
    for (int i = 0; i < llll; i++) {
      tiles.add(new Tile(PAppletProxy.loadImage(path + "llll.png")) {
        @Override
        public void setConnections(int x, int y, Connection[][] hor, Connection[][] vert) {
          Connection upIn = new Connection(x, y - 0.2, Math.PI * 0.35);
          Connection upOut = new Connection(x, y - 0.2, -Math.PI * 0.35);
          Connection leftIn = new Connection(x - 0.2, y, -Math.PI * 0.15);
          Connection leftOut = new Connection(x - 0.2, y, -Math.PI * 0.85);
          Connection downIn = new Connection(x, y + 0.2, -Math.PI * 0.65);
          Connection downOut = new Connection(x, y + 0.2, Math.PI * 0.65);
          Connection rightIn = new Connection(x + 0.2, y, Math.PI * 0.85);
          Connection rightOut = new Connection(x + 0.2, y, Math.PI * 0.15);

          vert[x * 2 + 1][y].setNext(downIn);
          downIn.setNext(leftOut);
          leftOut.setNext(hor[x - 1][y * 2]);
          hor[x][y * 2].setNext(rightIn);
          rightIn.setNext(downOut);
          downOut.setNext(vert[x * 2][y]);
          vert[x * 2][y - 1].setNext(upIn);
          upIn.setNext(rightOut);
          rightOut.setNext(hor[x][y * 2 + 1]);
          hor[x - 1][y * 2 + 1].setNext(leftIn);
          leftIn.setNext(upOut);
          upOut.setNext(vert[x * 2 + 1][y - 1]);
        }
      });
    }

    // brlb
    for (int i = 0; i < brlb; i++) {
      tiles.add(new Tile(PAppletProxy.loadImage(path + "brlb.png")) {
        @Override
        public void setConnections(int x, int y, Connection[][] hor, Connection[][] vert) {
          vert[x * 2 + 1][y].setNext(vert[x * 2][y]);
          hor[x][y * 2].setNext(vert[x * 2 + 1][y - 1]);
          vert[x * 2][y - 1].setNext(hor[x][y * 2 + 1]);
          hor[x - 1][y * 2 + 1].setNext(hor[x - 1][y * 2]);
        }
      });
    }

    // bbrl
    for (int i = 0; i < bbrl; i++) {
      tiles.add(new Tile(PAppletProxy.loadImage(path + "bbrl.png")) {
        @Override
        public void setConnections(int x, int y, Connection[][] hor, Connection[][] vert) {
          vert[x * 2 + 1][y].setNext(vert[x * 2][y]);
          hor[x][y * 2].setNext(hor[x][y * 2 + 1]);
          vert[x * 2][y - 1].setNext(hor[x - 1][y * 2]);
          hor[x - 1][y * 2 + 1].setNext(vert[x * 2 + 1][y - 1]);
        }
      });
    }

    // bbbb
    for (int i = 0; i < bbbb; i++) {
      tiles.add(new Tile(PAppletProxy.loadImage(path + "bbbb.png")) {
        @Override
        public void setConnections(int x, int y, Connection[][] hor, Connection[][] vert) {
          vert[x * 2 + 1][y].setNext(vert[x * 2][y]);
          hor[x][y * 2].setNext(hor[x][y * 2 + 1]);
          vert[x * 2][y - 1].setNext(vert[x * 2 + 1][y - 1]);
          hor[x - 1][y * 2 + 1].setNext(hor[x - 1][y * 2]);
        }
      });
    }

    return new Tileset(tiles.toArray(Tile[]::new));
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
