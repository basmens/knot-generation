package nl.basmens.generation.analyzers;

import java.util.ArrayList;

import nl.basmens.generation.AbstractTile;
import nl.basmens.knot.Knot;

public interface GridAnalyzer {
  ArrayList<Knot> extractKnots(AbstractTile[][] grid);

  int getGridW();
  void setGridW(int gridW);
  int getGridH();
  void setGridH(int gridH);
}
