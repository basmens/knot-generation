package nl.basmens.generation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import nl.basmens.Main;
import nl.basmens.generation.analyzers.GridAnalyzer;
import nl.basmens.generation.generators.GridGenerator;
import nl.basmens.knot.Knot;

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
    System.out.println("Starting " + fileExportName);

    int generation = 0;
    do {
      knots.clear();
      generator.generateGrid();
      knots = analyzer.extractKnots(generator.getGrid());

      // new Thread(() -> knots.forEach((Knot k) -> {k.startCalcAlexanderPolynomial();
      // k.startCalcKnotDeterminant();})).start();
      // for (int i = 0; i < knots.size(); i++) {
      // Knot k = knots.get(i);
      // if ((long) k.getAlexanderPolynomial().evaluateOnT(-1) !=
      // k.getKnotDeterminant()) {
      // System.out.println("Error on knot" + (i + 1));
      // }
      // }
      // System.out.println("Done");

      if (Main.SAVE_RESULTS) {
        ResultExporter exporter = ResultExporter.getExporter(fileExportName);
        exporter.save(knots);

        if (generation % 100 == 0) {
          System.out.println("Ran " + (generation + 1) + " times | Total knots " + exporter.getKnotCount() + " | " + fileExportName);
        }
        generation++;

        if (exporter.getKnotCount() >= Main.TARGET_KNOT_COUNT) {
          System.out.println("Done with " + fileExportName);
          stop();
        }
      }
    } while (running && Main.MULTI_THREAD);
  }

  public void stop() {
    System.out.println("Stopped " + fileExportName);
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
