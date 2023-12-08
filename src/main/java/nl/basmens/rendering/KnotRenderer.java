package nl.basmens.rendering;

import static nl.benmens.processing.PAppletProxy.beginShape;
import static nl.benmens.processing.PAppletProxy.bezierVertex;
import static nl.benmens.processing.PAppletProxy.endShape;
import static nl.benmens.processing.PAppletProxy.fill;
import static nl.benmens.processing.PAppletProxy.image;
import static nl.benmens.processing.PAppletProxy.imageMode;
import static nl.benmens.processing.PAppletProxy.line;
import static nl.benmens.processing.PAppletProxy.noFill;
import static nl.benmens.processing.PAppletProxy.noStroke;
import static nl.benmens.processing.PAppletProxy.scale;
import static nl.benmens.processing.PAppletProxy.stroke;
import static nl.benmens.processing.PAppletProxy.strokeJoin;
import static nl.benmens.processing.PAppletProxy.strokeWeight;
import static nl.benmens.processing.PAppletProxy.text;
import static nl.benmens.processing.PAppletProxy.textAlign;
import static nl.benmens.processing.PAppletProxy.textSize;
import static nl.benmens.processing.PAppletProxy.translate;
import static nl.benmens.processing.PAppletProxy.vertex;
import static processing.core.PConstants.CORNER;
import static processing.core.PConstants.LEFT;
import static processing.core.PConstants.ROUND;
import static processing.core.PConstants.TOP;

import nl.basmens.generation.KnotGenerationPipeline;
import nl.basmens.knot.Connection;
import nl.basmens.knot.Knot;
import nl.basmens.utils.Vector;

public class KnotRenderer {
  public static final float PROGRAM_WINDOW_WIDTH = 2560; // internally uses these sizes to render the screen
  public static final float PROGRAM_WINDOW_HEIGHT = 1440; // then scales it to fit on every screen size

  private final boolean doRenderTiles;
  private final boolean doCurvyKnots;

  private int knotBeingViewed = 0;
 
  // ===================================================================================================================
  // Constructor
  // ===================================================================================================================

  public KnotRenderer() {
    this(true, true);
  }

  public KnotRenderer(boolean doRenderTiles, boolean doCurvyKnots) {
    this.doRenderTiles = doRenderTiles;
    this.doCurvyKnots = doCurvyKnots;
  }

  // ===================================================================================================================
  // Display
  // ===================================================================================================================

  public void display(KnotGenerationPipeline pipeLine, int windowWidth, int windowHeight) {
    // scale the screen so every screensize works
    translate(windowWidth / 2f, windowHeight / 2f);
    scale(Math.min(windowWidth / PROGRAM_WINDOW_WIDTH, windowHeight / PROGRAM_WINDOW_HEIGHT));
    translate(-PROGRAM_WINDOW_WIDTH / 2f, -PROGRAM_WINDOW_HEIGHT / 2f);

    double tileW = (double) PROGRAM_WINDOW_WIDTH / pipeLine.getGridW();
    double tileH = (double) PROGRAM_WINDOW_HEIGHT / pipeLine.getGridH();

    if (doRenderTiles) {
      displayTiles(pipeLine, tileW, tileH);
    }

    Knot viewedKnot = pipeLine.getKnots().get(knotBeingViewed);

    displayKnot(viewedKnot, tileW, tileH);
    displayKnotInfo(pipeLine, viewedKnot);
  }

  private void displayTiles(KnotGenerationPipeline pipeLine, double tileW, double tileH) {
    if (pipeLine.getGridW() <= 300) {
      imageMode(CORNER);
      for (int x = 0; x < pipeLine.getGridW(); x++) {
        for (int y = 0; y < pipeLine.getGridH(); y++) {
          image(pipeLine.getGenerator().getTileAtPos(x, y).img,
              (float) (x * tileW),
              (float) (y * tileH), (float) tileW, (float) tileH);
        }
      }
    }
  }

  private void displayKnotInfo(KnotGenerationPipeline pipeLine, Knot knot) {
    // View knot info
    noStroke();
    fill(255);
    textSize(45);
    textAlign(LEFT, TOP);
    text("Displaying knot " + (knotBeingViewed + 1) + "/" + pipeLine.getKnots().size(),
        2078, 30);
    textSize(35);
    text(" - Length = " + knot.getLength(), 2078, 90);
    text(" - Intersection # = " + knot.getIntersections().size(), 2078, 130);
  }

  // ===================================================================================================================
  // Knot visualization
  // ===================================================================================================================

  private void displayKnot(Knot knot, double tileW, double tileH) {
    Connection connection = knot.getFirstConnection();
    stroke(220, 210, 150, 255);
    strokeWeight(80f / 10);
    strokeJoin(ROUND);
    noFill();
    beginShape();

    if (doCurvyKnots) {
      // Curvy
      Vector anchor1 = Vector.mult(connection.getPrev().getPos(), tileW, tileH);
      vertex((float) anchor1.getX(), (float) anchor1.getY());
      do {
        Vector anchor2 = Vector.mult(connection.getPos(), tileW, tileH);

        double dir1 = connection.getPrev().getDir();
        double dir2 = connection.getDir() + Math.PI;

        Vector diff = Vector.sub(connection.getPos(), connection.getPrev().getPos());

        double controlPointDist = Math.sqrt(diff.getX() * diff.getX() + diff.getY() * diff.getY()) * 0.35;
        if (Math.abs(angleDifference(dir1, dir2)) < 0.1) {
          controlPointDist *= 2;
        }

        Vector control1 = new Vector(Math.cos(dir1) * tileW * controlPointDist,
            Math.sin(dir1) * tileH * controlPointDist)
            .add(anchor1);
            
        Vector control2 = new Vector(Math.cos(dir2) * tileW * controlPointDist,
            Math.sin(dir2) * tileH * controlPointDist)
            .add(anchor2);

        bezierVertex((float) control1.getX(), (float) control1.getY(), (float) control2.getX(), (float) control2.getY(), (float) anchor2.getX(),
            (float) anchor2.getY());

        anchor1 = anchor2;

        connection = connection.getNext();
      } while (connection != knot.getFirstConnection());
      endShape();
    } else {
      // Straight
      Vector prevPos = getConnectionPos(connection.getPrev(), false).mult(tileW, tileH);
      do {
        Vector pos = getConnectionPos(connection, true).mult(tileW, tileH);
        if (connection.isIntersected()) {
          line((float) prevPos.getX(), (float) prevPos.getY(), (float) pos.getX(), (float) pos.getY());

          prevPos = pos;
          pos = getConnectionPos(connection, false).mult(tileW, tileH);
          if (connection.isOver()) {
            line((float) prevPos.getX(), (float) prevPos.getY(), (float) pos.getX(), (float) pos.getY());
          }
        } else {
          line((float) prevPos.getX(), (float) prevPos.getY(), (float) pos.getX(), (float) pos.getY());
        }

        prevPos = pos;
        connection = connection.getNext();
      } while (connection != knot.getFirstConnection());
    }
  }

  private static Vector getConnectionPos(Connection connection, boolean isPrevSide) {
    double intersectionGap = 0.1;

    if (!connection.isIntersected()) {
      return connection.getPos().copy();
    }

    if (isPrevSide) {
      return Vector.fromAngle(connection.getDir()).mult(-intersectionGap).add(connection.getPos());
    } else {
      return Vector.fromAngle(connection.getDir()).mult(intersectionGap).add(connection.getPos());
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
  // Setters
  // ===================================================================================================================

  public void setKnotBeingViewed(int knotBeingViewed) {
    this.knotBeingViewed = knotBeingViewed;
  }

  // ===================================================================================================================
  // Getters
  // ===================================================================================================================

  public int getKnotBeingViewed() {
    return knotBeingViewed;
  }
}
