package nl.basmens.rendering;

import static nl.benmens.processing.PAppletProxy.background;
import static nl.benmens.processing.PAppletProxy.bezier;
import static nl.benmens.processing.PAppletProxy.color;
import static nl.benmens.processing.PAppletProxy.fill;
import static nl.benmens.processing.PAppletProxy.image;
import static nl.benmens.processing.PAppletProxy.imageMode;
import static nl.benmens.processing.PAppletProxy.line;
import static nl.benmens.processing.PAppletProxy.noFill;
import static nl.benmens.processing.PAppletProxy.noStroke;
import static nl.benmens.processing.PAppletProxy.scale;
import static nl.benmens.processing.PAppletProxy.stroke;
import static nl.benmens.processing.PAppletProxy.strokeCap;
import static nl.benmens.processing.PAppletProxy.strokeWeight;
import static nl.benmens.processing.PAppletProxy.text;
import static nl.benmens.processing.PAppletProxy.textAlign;
import static nl.benmens.processing.PAppletProxy.textSize;
import static nl.benmens.processing.PAppletProxy.translate;
import static processing.core.PConstants.CORNER;
import static processing.core.PConstants.LEFT;
import static processing.core.PConstants.PROJECT;
import static processing.core.PConstants.SQUARE;
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
  private final boolean doStroke;

  private int fillColor = color(200);
  private int strokeColor = color(0);
  private float lineWidth = 60;
  private float strokeWidth = 40;

  private int knotBeingViewed = 0;

  // ===================================================================================================================
  // Constructor
  // ===================================================================================================================

  public KnotRenderer() {
    this(true, true, true);
  }

  public KnotRenderer(boolean doRenderTiles, boolean doCurvyKnots, boolean doStroke) {
    this.doRenderTiles = doRenderTiles;
    this.doCurvyKnots = doCurvyKnots;
    this.doStroke = doStroke;
  }

  // ===================================================================================================================
  // Display
  // ===================================================================================================================

  public void display(KnotGenerationPipeline pipeLine, int windowWidth, int windowHeight) {
    // scale the screen so every screensize works
    translate(windowWidth / 2f, windowHeight / 2f);
    scale(Math.min(windowWidth / PROGRAM_WINDOW_WIDTH, windowHeight / PROGRAM_WINDOW_HEIGHT));
    translate(-PROGRAM_WINDOW_WIDTH / 2f, -PROGRAM_WINDOW_HEIGHT / 2f);

    background(0);

    double tileW = (double) PROGRAM_WINDOW_WIDTH / pipeLine.getGridW() * 0.8;
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
    text(" - Length : " + knot.getLength(), 2078, 90);
    text(" - Intersections : " + knot.getIntersections().size(), 2078, 130);
  }

  // ===================================================================================================================
  // Knot visualization
  // ===================================================================================================================

  private void displayKnot(Knot knot, double tileW, double tileH) {
    noFill();
    if (doCurvyKnots) {
      strokeCap(SQUARE);
    } else {
      strokeCap(PROJECT);
    }

    Connection connection = knot.getFirstConnection();
    do {
      if ((connection.isIntersected() && connection.isUnder())) {
        drawConnection(connection, tileW, tileH);
        drawConnection(connection.getPrev(), tileW, tileH);

        drawConnection(connection.getIntersection().over, tileW, tileH);
        drawConnection(connection.getIntersection().over.getPrev(), tileW, tileH);
      } else if (!connection.isIntersected() && !connection.getNext().isIntersected()) {
        drawConnection(connection, tileW, tileH);
      }

      connection = connection.getNext();
    } while (connection != knot.getFirstConnection());
  }

  private void drawConnection(Connection connection, double tileW, double tileH) {
    Vector pos1 = Vector.mult(connection.getPos(), tileW, tileH);
    Vector pos2 = Vector.mult(connection.getNext().getPos(), tileW, tileH);
    if (doCurvyKnots) {
      double angle1 = connection.getDir();
      double angle2 = connection.getNext().getDir();

      if (doStroke) {
        stroke(strokeColor);
        strokeWeight((strokeWidth * 2 + lineWidth) / (float) (PROGRAM_WINDOW_HEIGHT / tileH));
        drawCurve(pos1, angle1, pos2, angle2);
      }

      stroke(fillColor);
      strokeWeight(lineWidth / (float) (PROGRAM_WINDOW_HEIGHT / tileH));
      drawCurve(pos1, angle1, pos2, angle2);

    } else {
      if (doStroke) {
        stroke(strokeColor);
        strokeWeight((strokeWidth * 2 + lineWidth) / (float) (PROGRAM_WINDOW_HEIGHT / tileH));
        drawLine(pos1, pos2);
      }

      stroke(fillColor);
      strokeWeight(lineWidth / (float) (PROGRAM_WINDOW_HEIGHT / tileH));
      drawLine(pos1, pos2);
    }
  }

  private void drawCurveSection() {

  }

  private void drawStroke() {

  }

  private void drawCurve(Vector pos1, double angle1, Vector pos2, double angle2) {
    double controlDist = Math.pow(Vector.dist(pos1, pos2), 0.75) *
        Math.pow(Math.abs((angle1 - angle2) % (Math.PI + 0.001)), 0.75);
    Vector control1 = Vector.fromAngle(angle1).mult(controlDist).add(pos1);
    Vector control2 = Vector.fromAngle(angle2).mult(-controlDist).add(pos2);

    bezier((float) pos1.getX(), (float) pos1.getY(),
        (float) control1.getX(), (float) control1.getY(),
        (float) control2.getX(), (float) control2.getY(),
        (float) pos2.getX(), (float) pos2.getY());
  }

  private void drawLine(Vector pos1, Vector pos2) {
    line((float) pos1.getX(), (float) pos1.getY(), (float) pos2.getX(), (float) pos2.getY());
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
