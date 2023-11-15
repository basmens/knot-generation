package nl.basmens.generation;

public class Tileset {
  private final AbstractTile[] tiles;

  public Tileset(AbstractTile... tiles) {
    this.tiles = tiles;
  }

  public int getTileCount() {
    return tiles.length;
  }

  public AbstractTile getTileByIndex(int index) {
    return tiles[index];
  }
}
