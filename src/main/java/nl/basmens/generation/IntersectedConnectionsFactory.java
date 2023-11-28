package nl.basmens.generation;

import java.util.ArrayList;
import java.util.Random;

import nl.basmens.knot.Connection;
import nl.basmens.knot.Intersection;

public class IntersectedConnectionsFactory {
  private ArrayList<ArrayList<ArrayList<Connection>>> connections = new ArrayList<>();
  private Random random = new Random();

  public IntersectedConnectionsFactory(int gridW, int gridH) {
    for (int x = 0; x < gridW; x++) {
      ArrayList<ArrayList<Connection>> list = new ArrayList<>();
      connections.add(list);
      for (int y = 0; y < gridH; y++) {
        list.add(new ArrayList<>());
      }
    }
  }

  public Connection getConnection(int x, int y, int id) {
    ArrayList<Connection> list = connections.get(x).get(y);
    while (list.size() <= id) {
      Connection a = new Connection(x, y, 0);
      Connection b = new Connection(x, y, 0);
      list.add(a);
      list.add(b);
      if (random.nextBoolean()) {
        new Intersection(a, b);
      } else {
        new Intersection(b, a);
      }
    }
    return list.get(id);
  }
}
