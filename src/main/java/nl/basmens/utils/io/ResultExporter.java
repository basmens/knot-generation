package nl.basmens.utils.io;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import nl.basmens.Main;
import nl.basmens.knot.Knot;
import nl.basmens.utils.concurrent.PerformanceTimer;
import processing.core.PApplet;
import processing.data.JSONObject;

public final class ResultExporter {
  private static final long FLUSH_INTERVAL = 60 * 1_000L; // in millis
  private static final int CACHE_SIZE = 20;

  private static HashMap<String, ResultExporter> exporters = new HashMap<>();

  private final File file;
  private final JSONObject json;
  private final ArrayList<JSONObject> lengthJsonCache = new ArrayList<>();

  private long lastFlushMillis;

  private long knotCount;

  private ResultExporter(String fileExportName) {
    // Get file
    URL resource = getClass().getResource("/");
    String p = null;
    try {
      p = Paths.get(resource.toURI()).toAbsolutePath().toString();
      p = p.substring(0, p.length() - "target/classes".length());
      p += "results/" + fileExportName + ".json";
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }
    file = new File(p);
    json = file.exists() ? PApplet.loadJSONObject(file) : new JSONObject();

    // Load knot count
    ((Set<String>) json.keys()).forEach((String k) -> {
      knotCount += json.getJSONObject(k).getLong("count", 0);
    });

    // Init cache
    lengthJsonCache.ensureCapacity(CACHE_SIZE);
    for (int l = 2; l < (CACHE_SIZE + 2) * 2; l += 2) {
      lengthJsonCache.add(jsonComputeIfAbsant(json, Integer.toString(l)));
    }

    lastFlushMillis = System.currentTimeMillis();
  }

  public static synchronized ResultExporter getExporter(String fileExportName) {
    return exporters.computeIfAbsent(fileExportName, ResultExporter::new);
  }

  public static synchronized void saveAll() {
    exporters.forEach((String k, ResultExporter v) -> v.flush());
  }

  public synchronized void save(Collection<Knot> knots) {
    PerformanceTimer timer = new PerformanceTimer(getClass(), "save");

    // Update Json and increment counters
    for (Knot k : knots) {
      // Increment length counter
      int len = k.getLength();
      JSONObject lengthJson = (len / 2 <= CACHE_SIZE) ? lengthJsonCache.get(len / 2 - 1)
          : jsonComputeIfAbsant(json, Integer.toString(len));
      incrementCounter(lengthJson, "count");

      // save invariants
      if (Main.SAVE_TRICOLORABILITY) {
        incrementCounter(jsonComputeIfAbsant(lengthJson, "tricolorability"), "" + k.isTricolorable());
      }
      if (Main.SAVE_KNOT_DETERMINANT) {
        incrementCounter(jsonComputeIfAbsant(lengthJson, "knot determinant"), "" + k.getKnotDeterminant());
      }
      if (Main.SAVE_ALEXANDER_POLYNOMIAL) {
        incrementCounter(jsonComputeIfAbsant(lengthJson, "alexander polynomial"), "" + k.getAlexanderPolynomial());
      }
    }

    knotCount += knots.size();
    if (lastFlushMillis == 0 || System.currentTimeMillis() - lastFlushMillis > FLUSH_INTERVAL) {
      flush();
    }

    timer.stop();
  }

  public synchronized long getKnotCount() {
    return knotCount;
  }

  public synchronized void flush() {
    PerformanceTimer timer = new PerformanceTimer(getClass(), "flush");

    // Remove the empty lengthJsons created by the cache from the save
    boolean[] isToBeAddedBack = new boolean[CACHE_SIZE];
    for (int i = 0; i < CACHE_SIZE; i++) {
      if (!lengthJsonCache.get(i).hasKey("count")) {
        isToBeAddedBack[i] = true;
      }
      json.remove(Integer.toString((i + 1) * 2));
    }

    // Save
    json.save(file, "indent=2");
    System.out.println("Flushed " + knotCount + " knots to " + file.getName() + " | "
        + (System.currentTimeMillis() - lastFlushMillis) / 1E3 + " seconds after last flush");
    lastFlushMillis = System.currentTimeMillis();

    // Add back the cache elements
    for (int i = 0; i < CACHE_SIZE; i++) {
      if (isToBeAddedBack[i]) {
        json.put(Integer.toString((i + 1) * 2), lengthJsonCache.get(i));
        System.out.println("Put back " + i);
      }
    }

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
