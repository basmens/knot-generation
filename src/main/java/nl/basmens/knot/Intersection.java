package nl.basmens.knot;

public class Intersection {
  public final Connection under;
  public final Connection over;

  protected int underSectionId1;
  protected int underSectionId2;
  protected int overSectionId;

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
    double underAngle = ((under.getDir() - over.getDir()) % (Math.PI * 2) + Math.PI * 2) % (Math.PI * 2);

    if (underAngle < Math.PI) {
      return 0;
    } else {
      return 1;
    }
  }
}
