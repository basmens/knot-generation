package nl.basmens.generation;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.function.Supplier;

import nl.basmens.Main;
import nl.basmens.generation.analyzers.GridAnalyzer;
import nl.basmens.generation.generators.GridGenerator;
import nl.basmens.knot.Knot;
import processing.core.PApplet;
import processing.data.JSONObject;

public class KnotGenerationPipeline implements Runnable {
  public final Tileset tileset;
  public final int gridW;
  public final int gridH;

  private GridGenerator generator;
  private GridAnalyzer analyzer;

  private String fileExportName;

  private ArrayList<Knot> knots = new ArrayList<>();

  private boolean running = true;

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
    do {
      knots.clear();
      generator.generateGrid();
      knots = analyzer.extractKnots(generator.getGrid());

      if (Main.SAVE_RESULTS) {
        saveKnots();
      }
    } while (running && Main.MULTI_THREAD);
  }

  private synchronized void saveKnots() {
    try {
      URL resource = KnotGenerationPipeline.class.getResource("/");
      String path = Paths.get(resource.toURI()).toAbsolutePath().toString();
      path = path.substring(0, path.length() - "target/classes".length());
      path += "results/" + fileExportName + ".json";
      File file = new File(path);
      JSONObject json = file.exists() ? PApplet.loadJSONObject(file) : new JSONObject();

      for (Knot k : knots) {
        String key = Integer.toString(k.getLength());
        int count = json.getInt(key, 0) + 1;
        json.setInt(key, count);
      }

      json.save(file, "indent=2");
    } catch (URISyntaxException e) {
      e.printStackTrace();
    } catch (ClassCastException e) {
      System.out.println("Caught in " + fileExportName);
    }
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

  public ArrayList<Knot> getKnots() {
    return knots;
  }
}
