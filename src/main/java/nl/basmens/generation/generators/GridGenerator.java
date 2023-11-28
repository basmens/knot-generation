package nl.basmens.generation.generators;

import nl.basmens.generation.Tile;

public interface GridGenerator {
  void generateGrid();

  Tile getTileAtPos(int x, int y);

  int getGridW();
  void setGridW(int gridW);
  int getGridH();
  void setGridH(int gridH);

  Tile[][] getGrid();
}
