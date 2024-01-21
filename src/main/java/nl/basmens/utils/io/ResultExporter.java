package nl.basmens.utils.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

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
  private HashMap<Integer, KnotJson> lengths = new HashMap<>();
  private HashMap<Integer, KnotJson> intersections = new HashMap<>();
  private long knotCount;

  private long lastFlushMillis;

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

    // Read file
    if (file.exists()) {
      JSONObject json = PApplet.loadJSONObject(file);
      knotCount = json.getLong(COUNT_KEY, 0);

      JSONObject lengthJson = json.getJSONObject(LENGTH_BASED_KEY);
      if (lengthJson != null) {
        ((Set<String>) lengthJson.keys())
            .forEach(k -> lengths.put(Integer.parseInt(k), new KnotJson(lengthJson.getJSONObject(k))));
      }

      JSONObject intersectionJson = json.getJSONObject(INTERSECTIONS_BASED_KEY);
      if (intersectionJson != null) {
        ((Set<String>) intersectionJson.keys())
            .forEach(k -> intersections.put(Integer.parseInt(k), new KnotJson(intersectionJson.getJSONObject(k))));
      }
    }
  }

  public static synchronized ResultExporter getExporter(String fileExportName) {
    return exporters.computeIfAbsent(fileExportName, ResultExporter::new);
  }

  public static synchronized void closeExporter(String fileExportName) {
    ResultExporter exporter = exporters.get(fileExportName);
    if (exporter != null) {
      exporter.flush();
      exporters.remove(fileExportName);
    }
  }

  public static synchronized void closeAll() {
    exporters.forEach((String k, ResultExporter v) -> v.flush());
    exporters.clear();
  }

  public synchronized void save(Collection<Knot> knots) {
    PerformanceTimer timer = new PerformanceTimer(getClass(), "save");

    for (Knot k : knots) {
      lengths.computeIfAbsent(k.getLength(), s -> new KnotJson()).addKnot(k);
      intersections.computeIfAbsent(k.getIntersectionCount(), s -> new KnotJson()).addKnot(k);
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
    PApplet.createPath(file);
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
      bw.append("{\n\"").append(COUNT_KEY).append("\":").append(Long.toString(knotCount));

      bw.append(",\n\"").append(LENGTH_BASED_KEY).append("\":{");
      boolean addComma = false;
      for (Entry<Integer, KnotJson> e : lengths.entrySet()) {
        if (addComma) {
          bw.append(",");
        }
        bw.append("\n\"").append(e.getKey().toString()).append("\":").append(e.getValue().toString());
        addComma = true;
      }
      
      bw.append("\n},\n\"").append(INTERSECTIONS_BASED_KEY).append("\":{");
      addComma = false;
      for (Entry<Integer, KnotJson> e : intersections.entrySet()) {
        if (addComma) {
          bw.append(",");
        }
        bw.append("\n\"").append(e.getKey().toString()).append("\":").append(e.getValue().toString());
        addComma = true;
      }
      bw.append("\n}\n}");
    } catch (Exception e) {
      e.printStackTrace();
    }

    System.out.println("Flushed " + knotCount + " knots to " + file.getName() + " | "
        + (System.currentTimeMillis() - lastFlushMillis) / 1E3 + " seconds after last flush");
    lastFlushMillis = System.currentTimeMillis();

    timer.stop();
  }
}
