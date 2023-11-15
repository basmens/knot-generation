package nl.basmens.generation;

import nl.basmens.knot.Connection;
import processing.core.PImage;

public abstract class AbstractTile {
  public final PImage img;

  protected AbstractTile(PImage img) {
    this.img = img;
  }

  public abstract int setConnectionInputGoingUp(int x, int y, Connection[][] hor, Connection[][] vert);
  public abstract int setConnectionInputGoingLeft(int x, int y, Connection[][] hor, Connection[][] vert);
  public abstract int setConnectionInputGoingDown(int x, int y, Connection[][] hor, Connection[][] vert);
  public abstract int setConnectionInputGoingRight(int x, int y, Connection[][] hor, Connection[][] vert);
}
