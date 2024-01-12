package nl.basmens.utils.io;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import nl.basmens.Main;
import nl.basmens.knot.Knot;
import processing.core.PApplet;
import processing.data.JSONObject;

public final class ResultExporter {
  private static final long FLUSH_INTERVAL = 60 * 1_000_000_000l; // in nanotime

  private static HashMap<String, ResultExporter> exporters = new HashMap<>();

  private final File file;
  private final JSONObject json;

  private long lastFlushNanoTime;


  private long knotCount;

  private ResultExporter(String fileExportName) {
    URL resource = ResultExporter.class.getResource("/");
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

    ((Set<String>) json.keys()).forEach((String k) -> {
      knotCount += json.getJSONObject(k).getLong("count", 0);
    });
  }

  public static synchronized ResultExporter getExporter(String fileExportName) {
    return exporters.computeIfAbsent(fileExportName, ResultExporter::new);
  }

  public static synchronized void saveAll() {
    exporters.forEach((String k, ResultExporter v) -> v.flush());
  }

  public synchronized void save(Collection<Knot> knots) {
    // Update Json and increment counters
    for (Knot k : knots) {
      // Increment length counter
      JSONObject lengthJson = jsonComputeIfAbsant(json, Integer.toString(k.getLength()));
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
    if (lastFlushNanoTime == 0 || System.nanoTime() - lastFlushNanoTime > FLUSH_INTERVAL) {
      flush();
    }
  }

  public synchronized long getKnotCount() {
    return knotCount;
  }

  public synchronized void flush() {
    json.save(file, "indent=2");
    System.out.println("Flushed " + knotCount + " knots to " + file.getName() + " | " + (System.nanoTime() - lastFlushNanoTime) / 1E9 + " seconds after last flush");
    lastFlushNanoTime = System.nanoTime();
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
    json.setLong(key, json.getLong(key, 0) + 1);
  }
}
