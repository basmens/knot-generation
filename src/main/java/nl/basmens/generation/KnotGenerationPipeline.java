package nl.basmens.generation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import nl.basmens.Main;
import nl.basmens.generation.analyzers.GridAnalyzer;
import nl.basmens.generation.generators.GridGenerator;
import nl.basmens.knot.Knot;
import nl.basmens.utils.concurrent.PerformanceTimer;
import nl.basmens.utils.io.ResultExporter;

public class KnotGenerationPipeline implements Runnable {
  public final Tileset tileset;
  public final int gridW;
  public final int gridH;

  private GridGenerator generator;
  private GridAnalyzer analyzer;

  private String fileExportName;

  private ArrayList<Knot> knots = new ArrayList<>();

  private volatile boolean running = true;

  public KnotGenerationPipeline(Tileset tileset, int gridW, int gridH, Function<Tileset, GridGenerator> gridGenerator,
      Supplier<GridAnalyzer> gridAnalyzer, String fileExportName) {
    this.tileset = tileset;
    this.gridW = gridW;
    this.gridH = gridH;
    this.fileExportName = fileExportName;

    generator = gridGenerator.apply(tileset);
    analyzer = gridAnalyzer.get();

    generator.setGridW(gridW);
    generator.setGridH(gridH);
    analyzer.setGridW(gridW);
    analyzer.setGridH(gridH);
  }

  private void runGenCycle() {
    PerformanceTimer timer = new PerformanceTimer(getClass(), "runGenCycle", "clear");
    knots.clear();
    timer.nextSegment("generateGrid");
    generator.generateGrid();
    timer.nextSegment("extractKnots");
    knots = analyzer.extractKnots(generator.getGrid());
    timer.stop();
  }

  @Override
  public void run() {
    PerformanceTimer timer = new PerformanceTimer(getClass(), "run - " + fileExportName);
    if (Main.MULTI_THREAD) {
      if (!running || ResultExporter.getExporter(fileExportName).getKnotCount() >= Main.TARGET_KNOT_COUNT) {
        System.out.println("Skipped " + fileExportName);
        stop();
        timer.stop();
        return;
      }
      
      runThreaded();
    } else {
      runGenCycle();
    }
    timer.stop();
  }

  private void runThreaded() {
    PerformanceTimer timer = new PerformanceTimer(getClass(), "runThreaded", "start");
    System.out.println("Starting " + fileExportName);
    do {
      timer.nextSegment("gen knots");
      runGenCycle();

      if (Main.SAVE_RESULTS) {
        // Start calculations
        timer.nextSegment("calc invariants");
        for (Knot k : knots) {
          if (Main.SAVE_TRICOLORABILITY) {
            k.startCalcTricolorability();
          }
          if (Main.SAVE_KNOT_DETERMINANT) {
            k.startCalcKnotDeterminant();
          }
          if (Main.SAVE_ALEXANDER_POLYNOMIAL) {
            k.startCalcAlexanderPolynomial();
          }
        }

        // Export
        timer.nextSegment("export");
        ResultExporter exporter = ResultExporter.getExporter(fileExportName);
        exporter.save(knots);
        if (exporter.getKnotCount() >= Main.TARGET_KNOT_COUNT) {
          System.out.println("Finished " + fileExportName);
          stop();
        }
      }
    } while (running);

    System.out.println("Stopped " + fileExportName);
    timer.stop();
  }

  public void stop() {
    running = false;
  }

  public Tileset getTileset() {
    return tileset;
  }

  public int getGridW() {
    return gridW;
  }

  public int getGridH() {
    return gridH;
  }

  public GridGenerator getGenerator() {
    return generator;
  }

  public GridAnalyzer getAnalyzer() {
    return analyzer;
  }

  public List<Knot> getKnots() {
    return knots;
  }

  public String getFileExportName() {
    return fileExportName;
  }
}
