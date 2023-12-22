package nl.basmens.rendering;

import static nl.benmens.processing.PAppletProxy.background;
import static nl.benmens.processing.PAppletProxy.bezier;
import static nl.benmens.processing.PAppletProxy.bezierPoint;
import static nl.benmens.processing.PAppletProxy.bezierTangent;
import static nl.benmens.processing.PAppletProxy.color;
import static nl.benmens.processing.PAppletProxy.fill;
import static nl.benmens.processing.PAppletProxy.image;
import static nl.benmens.processing.PAppletProxy.imageMode;
import static nl.benmens.processing.PAppletProxy.line;
import static nl.benmens.processing.PAppletProxy.noFill;
import static nl.benmens.processing.PAppletProxy.noStroke;
import static nl.benmens.processing.PAppletProxy.point;
import static nl.benmens.processing.PAppletProxy.scale;
import static nl.benmens.processing.PAppletProxy.stroke;
import static nl.benmens.processing.PAppletProxy.strokeCap;
import static nl.benmens.processing.PAppletProxy.strokeJoin;
import static nl.benmens.processing.PAppletProxy.strokeWeight;
import static nl.benmens.processing.PAppletProxy.text;
import static nl.benmens.processing.PAppletProxy.textAlign;
import static nl.benmens.processing.PAppletProxy.textSize;
import static nl.benmens.processing.PAppletProxy.translate;
import static processing.core.PConstants.CORNER;
import static processing.core.PConstants.LEFT;
import static processing.core.PConstants.ROUND;
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
  private final boolean doStroke;
  private final boolean doDebugMode;

  private int fillColor = color(200, 210, 220);
  private int strokeColor = color(0);
  private float lineWidth = 60;
  private float strokeWidth = 40;

  private int debugPrimairyColor = color(255, 190, 20, 200);
  private int debugSecondairyColor = color(20, 50, 190, 200);
  private int debugTertairyColor = color(190, 20, 50, 200);

  private int knotBeingViewed = 0;

  // ===================================================================================================================
  // Constructor
  // ===================================================================================================================

  public KnotRenderer() {
    this(true, true, false);
  }

  public KnotRenderer(boolean doRenderTiles, boolean doStroke, boolean doDebugMode) {
    this.doRenderTiles = doRenderTiles;
    this.doStroke = doStroke;
    this.doDebugMode = doDebugMode;
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
    text(" - is Tricolorable : " + knot.isTricolorable(), 2078, 170);
    text(" - Knot Determinant : " + knot.getKnotDeterminant(), 2078, 210);
  }

  // ===================================================================================================================
  // Knot visualization
  // ===================================================================================================================

  private void displayKnot(Knot knot, double tileW, double tileH) {
    noFill();
    strokeJoin(ROUND);

    Connection connection = knot.getFirstConnection();
    do {
      if ((connection.isIntersected() && connection.isUnder())) {
        drawConnection(connection, tileW, tileH);

        drawConnection(connection.getIntersection().over, tileW, tileH);
      } else if (!connection.isIntersected()) {
        drawConnection(connection, tileW, tileH);
      }

      connection = connection.getNext();
    } while (connection != knot.getFirstConnection());

    if (doDebugMode) {
      do {
        drawDebug(connection, tileW, tileH);

        connection = connection.getNext();
      } while (connection != knot.getFirstConnection());
    }
  }

  private void drawConnection(Connection connection, double tileW, double tileH) {
    Vector pos1 = Vector.mult(connection.getPrev().getPos(), tileW, tileH);
    Vector pos2 = Vector.mult(connection.getPos(), tileW, tileH);
    Vector pos3 = Vector.mult(connection.getNext().getPos(), tileW, tileH);
    double angle1 = connection.getPrev().getDir();
    double angle2 = connection.getDir();
    double angle3 = connection.getNext().getDir();

    if (doStroke) {
      strokeCap(SQUARE);
      strokeWeight((strokeWidth * 2 + lineWidth) / (float) (PROGRAM_WINDOW_HEIGHT / tileH));
      drawCurveSection(pos1, angle1, pos2, angle2, pos3, angle3, strokeColor, strokeColor);
    }

    strokeCap(ROUND);
    strokeWeight(lineWidth / (float) (PROGRAM_WINDOW_HEIGHT / tileH));
    drawCurveSection(pos1, angle1, pos2, angle2, pos3, angle3,
        getFillColor(connection.getPrev().getSectionValue()), getFillColor(connection.getSectionValue()));
  }

  private int getFillColor(int sectionValue) {
    if (sectionValue == 0) {
      return fillColor;
    } else if (sectionValue == 1) {
      return color(255, 0, 0);
    } else if (sectionValue == 2) {
      return color(0, 255, 0);
    } else {
      return color(0, 0, 255);
    }
  }

  // Knot Curve

  private void drawCurveSection(Vector pos1, double angle1, Vector pos2, double angle2, Vector pos3, double angle3,
      int color1, int color2) {
    Vector middlePos1 = getCurveMiddle(pos1, angle1, pos2, angle2);
    Vector middlePos2 = getCurveMiddle(pos2, angle2, pos3, angle3);

    double middleAngle1 = getCurveMiddleAngle(pos1, angle1, pos2, angle2);
    double middleAngle2 = getCurveMiddleAngle(pos2, angle2, pos3, angle3);

    stroke(color1);
    drawCurve(middlePos1, middleAngle1, pos2, angle2);
    stroke(color2);
    drawCurve(pos2, angle2, middlePos2, middleAngle2);
  }

  private Vector getCurveMiddle(Vector startPos, double startAngle, Vector endPos, double endAngle) {
    double controlDist = Math.pow(Vector.dist(startPos, endPos), 0.75) *
        Math.pow(Math.abs((startAngle - endAngle) % (Math.PI + 0.001)), 0.75);
    Vector control1 = Vector.fromAngle(startAngle).mult(controlDist).add(startPos);
    Vector control2 = Vector.fromAngle(endAngle).mult(-controlDist).add(endPos);

    return new Vector(
        bezierPoint((float) startPos.getX(), (float) control1.getX(), (float) control2.getX(), (float) endPos.getX(),
            0.5f),
        bezierPoint((float) startPos.getY(), (float) control1.getY(), (float) control2.getY(), (float) endPos.getY(),
            0.5f));
  }

  private double getCurveMiddleAngle(Vector startPos, double startAngle, Vector endPos, double endAngle) {
    double controlDist = Math.pow(Vector.dist(startPos, endPos), 0.75) *
        Math.pow(Math.abs((startAngle - endAngle) % (Math.PI + 0.001)), 0.75);
    Vector control1 = Vector.fromAngle(startAngle).mult(controlDist).add(startPos);
    Vector control2 = Vector.fromAngle(endAngle).mult(-controlDist).add(endPos);

    return Math.atan2(
        bezierTangent((float) startPos.getY(), (float) control1.getY(), (float) control2.getY(), (float) endPos.getY(),
            0.5f),
        bezierTangent((float) startPos.getX(), (float) control1.getX(), (float) control2.getX(), (float) endPos.getX(),
            0.5f));
  }

  private void drawCurve(Vector startPos, double startAngle, Vector endPos, double endAngle) {
    double controlDist = Math.pow(Vector.dist(startPos, endPos), 0.75) *
        Math.pow(Math.abs((startAngle - endAngle) % (Math.PI + 0.001)), 0.75);
    Vector control1 = Vector.fromAngle(startAngle).mult(controlDist).add(startPos);
    Vector control2 = Vector.fromAngle(endAngle).mult(-controlDist).add(endPos);

    bezier((float) startPos.getX(), (float) startPos.getY(),
        (float) control1.getX(), (float) control1.getY(),
        (float) control2.getX(), (float) control2.getY(),
        (float) endPos.getX(), (float) endPos.getY());
  }

  // DebugMode

  private void drawDebug(Connection connection, double tileW, double tileH) {
    Vector pos = Vector.mult(connection.getPos(), tileW, tileH);

    strokeWeight((lineWidth) / (float) (PROGRAM_WINDOW_HEIGHT / tileH * 2));
    if (connection.isOver()) {
      stroke(debugSecondairyColor);
    } else if (connection.isUnder()) {
      stroke(debugTertairyColor);
    } else {
      stroke(debugPrimairyColor);
    }

    double angle = connection.getDir();
    Vector lineEnd = Vector.fromAngle(angle).mult(30).add(pos);
    line((float) pos.getX(), (float) pos.getY(), (float) lineEnd.getX(), (float) lineEnd.getY());

    strokeWeight((lineWidth) / (float) (PROGRAM_WINDOW_HEIGHT / tileH));
    if (!connection.isIntersected()) {
      stroke(debugPrimairyColor);
    } else if (connection.getIntersection().getType() == 0) {
      stroke(debugSecondairyColor);
    } else {
      stroke(debugTertairyColor);
    }
    point((float) pos.getX(), (float) pos.getY());
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
