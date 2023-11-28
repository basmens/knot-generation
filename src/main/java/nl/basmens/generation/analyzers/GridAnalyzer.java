package nl.basmens.generation.analyzers;

import java.util.ArrayList;

import nl.basmens.generation.Tile;
import nl.basmens.knot.Knot;

public interface GridAnalyzer {
  ArrayList<Knot> extractKnots(Tile[][] grid);

  int getGridW();
  void setGridW(int gridW);
  int getGridH();
  void setGridH(int gridH);
}
