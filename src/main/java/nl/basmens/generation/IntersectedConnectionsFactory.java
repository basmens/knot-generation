package nl.basmens.generation;

import java.util.ArrayList;
import java.util.Random;

import nl.basmens.Main;
import nl.basmens.generation.analyzers.GridAnalyzer;
import nl.basmens.knot.Connection;
import nl.basmens.utils.maths.Vector;

public class IntersectedConnectionsFactory {
  private static Random random = Main.RANDOM_FACTORY.get();
  private ArrayList<ArrayList<Connection>> connectionsA = new ArrayList<>();
  private ArrayList<ArrayList<Connection>> connectionsB = new ArrayList<>();

  public IntersectedConnectionsFactory(int gridW, int gridH) {
    for (int x = 0; x < gridW; x++) {
      connectionsA.add(new ArrayList<>());
      connectionsB.add(new ArrayList<>());
      for (int y = 0; y < gridH; y++) {
        connectionsA.get(x).add(new Connection(new Vector(x, y), 0));
        connectionsB.get(x).add(new Connection(new Vector(x, y), 0));
        createIntersection(connectionsA.get(x).get(y), connectionsB.get(x).get(y));
      }
    }
  }

  public Connection getConnectionA(int x, int y) {
    return connectionsA.get(x).get(y);
  }

  public Connection getConnectionB(int x, int y) {
    return connectionsB.get(x).get(y);
  }

  public static void createIntersection(Connection a, Connection b) {
    if (random.nextBoolean()) {
      new GridAnalyzer.AnalyzerIntersection(a, b);
    } else {
      new GridAnalyzer.AnalyzerIntersection(b, a);
    }
  }
}
