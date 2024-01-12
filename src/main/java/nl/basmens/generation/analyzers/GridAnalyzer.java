package nl.basmens.generation.analyzers;

import java.util.ArrayList;

import nl.basmens.generation.Tile;
import nl.basmens.knot.Connection;
import nl.basmens.knot.Intersection;
import nl.basmens.knot.Knot;
import nl.basmens.utils.collections.IndexedSet.IndexedSetElement;
import nl.basmens.utils.maths.Vector;

public interface GridAnalyzer {
  ArrayList<Knot> extractKnots(Tile[][] grid);

  int getGridW();
  void setGridW(int gridW);
  int getGridH();
  void setGridH(int gridH);

  class AnalyzerConnection extends Connection implements IndexedSetElement {
    private int setIndex;

    public AnalyzerConnection(Vector pos, double dir) {
      super(pos, dir);
    }

    public int getSetIndex() {
      return setIndex;
    }

    public void setSetIndex(int uuid) {
      this.setIndex = uuid;
    }
  }

  class AnalyzerIntersection extends Intersection implements IndexedSetElement {
    private int setIndex;

    public AnalyzerIntersection(Connection under, Connection over) {
      super(under, over);
    }

    public int getSetIndex() {
      return setIndex;
    }

    public void setSetIndex(int uuid) {
      this.setIndex = uuid;
    }
  }
}
