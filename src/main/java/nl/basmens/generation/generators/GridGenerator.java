package nl.basmens.generation.generators;

import nl.basmens.generation.AbstractTile;

public interface GridGenerator {
  void generateGrid();

  AbstractTile getTileAtPos(int x, int y);

  int getGridW();
  void setGridW(int gridW);
  int getGridH();
  void setGridH(int gridH);

  AbstractTile[][] getGrid();
}
