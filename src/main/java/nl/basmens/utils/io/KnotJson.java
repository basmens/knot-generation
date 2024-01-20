package nl.basmens.utils.io;

import java.util.HashMap;
import java.util.Set;

import nl.basmens.Main;
import nl.basmens.knot.Knot;
import processing.data.JSONObject;

public class KnotJson {
  private long count;

  // Leaving out tricolorability because it is never used
  private HashMap<Long, LongValue> knotDeterminants = new HashMap<>();
  private HashMap<String, LongValue> alexanderPolynomials = new HashMap<>();

  public KnotJson() {
  }

  public KnotJson(JSONObject fromJson) {
    count = fromJson.getLong("count", 0);

    JSONObject kdJson = fromJson.getJSONObject("knot determinant");
    if (kdJson != null) {
      ((Set<String>) kdJson.keys())
          .forEach(k -> knotDeterminants.put(Long.parseLong(k), new LongValue(kdJson.getLong(k))));
    }
    
    JSONObject apJson = fromJson.getJSONObject("alexander polynomial");
    if (apJson != null) {
      ((Set<String>) apJson.keys())
          .forEach(k -> alexanderPolynomials.put(k, new LongValue(apJson.getLong(k))));
    }
  }

  public void addKnot(Knot knot) {
    count++;
    if (Main.SAVE_KNOT_DETERMINANT) {
      knotDeterminants.computeIfAbsent(knot.getKnotDeterminant(), k -> new LongValue()).increment();
    }
    if (Main.SAVE_ALEXANDER_POLYNOMIAL) {
      alexanderPolynomials.computeIfAbsent(knot.getAlexanderPolynomial().toString(), k -> new LongValue()).increment();
    }
  }

  public String toString() {
    StringBuilder b = new StringBuilder("{\n\"count\":");
    b.append(count);

    if (!knotDeterminants.isEmpty()) {
      b.append(",\n\"knot determinant\":{\n\"");
      knotDeterminants.forEach((k, v) -> b.append(k).append("\":").append(v.get()).append(",\n\""));
      b.setLength(b.length() - 3);
      b.append("\n}");
    }
    if (!alexanderPolynomials.isEmpty()) {
      b.append(",\n\"alexander polynomial\":{\n\"");
      alexanderPolynomials.forEach((k, v) -> b.append(k).append("\":").append(v.get()).append(",\n\""));
      b.setLength(b.length() - 3);
      b.append("\n}");
    }

    b.append("\n}");
    return b.toString();
  }

  private static class LongValue {
    private long value;

    public LongValue() {
    }

    public LongValue(long value) {
      this.value = value;
    }

    public long get() {
      return value;
    }

    public void increment() {
      this.value++;
    }
  }
}
