package nl.basmens.generation;

import nl.basmens.knot.Connection;
import processing.core.PImage;

public class Tile {
  public final PImage img;

  protected Tile(PImage img) {
    this.img = img;
  }

  public int setConnectionInputGoingUp(int x, int y, Connection[][] hor, Connection[][] vert,
      IntersectedConnectionsFactory intersectedConnections) {
    throw new UnsupportedOperationException("setConnectionInputGoingUp method hasn't been implemented");
  }

  public int setConnectionInputGoingLeft(int x, int y, Connection[][] hor, Connection[][] vert,
      IntersectedConnectionsFactory intersectedConnections) {
    throw new UnsupportedOperationException("setConnectionInputGoingLeft method hasn't been implemented");
  }

  public int setConnectionInputGoingDown(int x, int y, Connection[][] hor, Connection[][] vert,
      IntersectedConnectionsFactory intersectedConnections) {
    throw new UnsupportedOperationException("setConnectionInputGoingDown method hasn't been implemented");
  }

  public int setConnectionInputGoingRight(int x, int y, Connection[][] hor, Connection[][] vert,
      IntersectedConnectionsFactory intersectedConnections) {
    throw new UnsupportedOperationException("setConnectionInputGoingRight method hasn't been implemented");
  }

  public void setConnections(int x, int y, Connection[][] hor, Connection[][] vert) {
    throw new UnsupportedOperationException("setConnections method hasn't been implemented");
  }
}
