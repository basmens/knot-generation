package nl.basmens.generation;

public class Tileset {
  private final Tile[] tiles;

  public Tileset(Tile... tiles) {
    this.tiles = tiles;
  }

  public int getTileCount() {
    return tiles.length;
  }

  public Tile getTileByIndex(int index) {
    return tiles[index];
  }

  public Tile[] getTiles() {
    return tiles;
  }
}
