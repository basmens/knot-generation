package nl.basmens.generation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import nl.basmens.Main;
import nl.basmens.generation.analyzers.GridAnalyzer;
import nl.basmens.generation.generators.GridGenerator;
import nl.basmens.knot.Knot;
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

  @Override
  public void run() {
    if (Main.MULTI_THREAD) {
      runThreaded();
    } else {
      knots.clear();
      generator.generateGrid();
      knots = analyzer.extractKnots(generator.getGrid());
    }
  }

  private void runThreaded() {
    if (!running || ResultExporter.getExporter(fileExportName).getKnotCount() >= Main.TARGET_KNOT_COUNT) {
      System.out.println("Skipped " + fileExportName);
      stop();
      return;
    }
    System.out.println("Starting " + fileExportName);

    do {
      knots.clear();
      generator.generateGrid();
      knots = analyzer.extractKnots(generator.getGrid());

      if (Main.SAVE_RESULTS) {

        // Start calculations
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

        ResultExporter exporter = ResultExporter.getExporter(fileExportName);
        exporter.save(knots);

        if (exporter.getKnotCount() >= Main.TARGET_KNOT_COUNT) {
          System.out.println("Finished " + fileExportName);
          stop();
        }
      }
    } while (running && Main.MULTI_THREAD);

    System.out.println("Stopped " + fileExportName);
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
