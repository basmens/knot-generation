package nl.basmens.generation;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;

import nl.basmens.Main;
import nl.basmens.knot.Knot;
import processing.core.PApplet;
import processing.data.JSONObject;

public final class ResultExporter {
  private static HashMap<String, ResultExporter> exporters = new HashMap<>();

  private final File file;
  private final JSONObject json;

  private int savesSinceLastFlush;

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
  }

  public static synchronized ResultExporter getExporter(String fileExportName) {
    return exporters.computeIfAbsent(fileExportName, ResultExporter::new);
  }

  public static synchronized void saveAll() {
    exporters.forEach((String k, ResultExporter v) -> v.flush());
  }

  public synchronized void save(Iterable<Knot> knots) {
    for (Knot k : knots) {
      // Increment length counter
      String keyLength = Integer.toString(k.getLength());
      JSONObject lengthJson = jsonComputeIfAbsant(json, keyLength);
      lengthJson.setLong("count", lengthJson.getLong("count", 0) + 1);

      // Start calculations
      if (Main.SAVE_TRICOLORABILITY) {
        k.isTricolorable();
      }
      if (Main.SAVE_KNOT_DETERMINANT) {
        k.getKnotDeterminant();
      }

      // Save calculations
      if (Main.SAVE_TRICOLORABILITY) {
        incrementCounter(jsonComputeIfAbsant(lengthJson, "tricolorability"), "" + k.isTricolorable());
      }
      if (Main.SAVE_KNOT_DETERMINANT) {
        incrementCounter(jsonComputeIfAbsant(lengthJson, "knot determinant"), "" + k.getKnotDeterminant());
      }
    }

    savesSinceLastFlush++;
    if (savesSinceLastFlush > 5) {
      flush();
    }
  }

  public synchronized void flush() {
    if (savesSinceLastFlush > 0) {
      json.save(file, "indent=2");
      savesSinceLastFlush = 0;
    }
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
