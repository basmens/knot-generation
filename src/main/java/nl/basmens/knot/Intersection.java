package nl.basmens.knot;

import nl.basmens.Main;
import nl.basmens.rendering.KnotRenderer;

public class Intersection {
  public final Connection under;
  public final Connection over;

  // ===================================================================================================================
  // Constructor
  // ===================================================================================================================

  public Intersection(Connection under, Connection over) {
    this.under = under;
    this.over = over;

    under.setIntersection(this);
    over.setIntersection(this);
  }

  // ===================================================================================================================
  // Commands
  // ===================================================================================================================

  // type 0
  // / / ^ / /
  // / / | / /
  // >---|--->
  // / / | / /
  // / / ^ / /

  // type 1
  // / / ^ / /
  // / / | / /
  // >------->
  // / / | / /
  // / / ^ / /

  public int getType() {
    double dirDifference = KnotRenderer.angleDifference(over.getDir(), under.getDir());

    if (dirDifference < 0) {
      return 0;
    } else {
      return 1;
    }
  }

  public void printState() {
    System.out.println(over.getPos() + " direction over: " + over.getDir() + " direction under: " + under.getDir() + " next: " + over.getNext().getPos());
  }
}
