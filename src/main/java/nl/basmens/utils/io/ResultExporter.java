package nl.basmens.utils.io;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;

import nl.basmens.Main;
import nl.basmens.knot.Knot;
import nl.basmens.utils.concurrent.PerformanceTimer;
import processing.core.PApplet;
import processing.data.JSONObject;

public final class ResultExporter {
  private static final String COUNT_KEY = "count";
  private static final String LENGTH_BASED_KEY = "length";
  private static final String INTERSECTIONS_BASED_KEY = "intersections";
  private static final long FLUSH_INTERVAL = 60 * 1_000L; // in millis

  private static HashMap<String, ResultExporter> exporters = new HashMap<>();

  private final File file;
  private final JSONObject json;

  private long lastFlushMillis;

  private long knotCount;

  private ResultExporter(String fileExportName) {
    // Get file
    URL resource = getClass().getResource("/");
    String path = null;
    try {
      path = Paths.get(resource.toURI()).toAbsolutePath().toString();
      path = path.substring(0, path.length() - "target/classes".length());
      path += "results/" + fileExportName + ".json";
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }

    file = new File(path);
    json = file.exists() ? PApplet.loadJSONObject(file) : new JSONObject();

    knotCount = json.getLong(COUNT_KEY, 0);
  }

  public static synchronized ResultExporter getExporter(String fileExportName) {
    return exporters.computeIfAbsent(fileExportName, ResultExporter::new);
  }

  public static synchronized void saveAll() {
    exporters.forEach((String k, ResultExporter v) -> v.flush());
  }

  public synchronized void save(Collection<Knot> knots) {
    PerformanceTimer timer = new PerformanceTimer(getClass(), "save");

    JSONObject lengthJson = jsonComputeIfAbsant(json, LENGTH_BASED_KEY);
    JSONObject intersectionsJson = jsonComputeIfAbsant(json, INTERSECTIONS_BASED_KEY);

    for (Knot k : knots) {
      // increment total knot count
      incrementCounter(json, COUNT_KEY);

      saveKnot(jsonComputeIfAbsant(lengthJson, Integer.toString(k.getLength())), k);
      saveKnot(jsonComputeIfAbsant(intersectionsJson, Integer.toString(k.getIntersections().size())), k);
    }

    knotCount += knots.size();
    if (lastFlushMillis == 0 || System.currentTimeMillis() - lastFlushMillis > FLUSH_INTERVAL) {
      flush();
    }

    timer.stop();
  }

  public void saveKnot(JSONObject knotJson, Knot knot) {
    incrementCounter(knotJson, COUNT_KEY);

    // save invariants
    if (Main.SAVE_TRICOLORABILITY) {
      incrementCounter(jsonComputeIfAbsant(knotJson, "tricolorability"), "" + knot.isTricolorable());
    }
    if (Main.SAVE_KNOT_DETERMINANT) {
      incrementCounter(jsonComputeIfAbsant(knotJson, "knot determinant"), "" + knot.getKnotDeterminant());
    }
    if (Main.SAVE_ALEXANDER_POLYNOMIAL) {
      incrementCounter(jsonComputeIfAbsant(knotJson, "alexander polynomial"), "" + knot.getAlexanderPolynomial());
    }
  }

  public synchronized long getKnotCount() {
    return knotCount;
  }

  public synchronized void flush() {
    PerformanceTimer timer = new PerformanceTimer(getClass(), "flush");
    json.save(file, "indent=2");

    System.out.println("Flushed " + knotCount + " knots to " + file.getName() + " | "
        + (System.currentTimeMillis() - lastFlushMillis) / 1E3 + " seconds after last flush");
    lastFlushMillis = System.currentTimeMillis();

    timer.stop();
  }

  private static JSONObject jsonComputeIfAbsant(JSONObject json, String key) {
    JSONObject result = json.getJSONObject(key);
    if (result == null) {
      result = new JSONObject();
      json.put(key, result);
    }
    return result;
  }

  private static void incrementCounter(JSONObject json, String key) {
    json.put(key, json.getLong(key, 0) + 1);
  }
}
