package nl.basmens.knot;

public class Intersection {
  public final Connection under;
  public final Connection over;

  protected int underSectionId1; // to previous
  protected int underSectionId2; // to next
  protected int overSectionId;

  // indexes of areaIds
  // / / | / /
  // / 2 | 1 /
  // >---|--->
  // / 3 | 0 /
  // / / | / /
  protected int[] areaIds = new int[] { -1, -1, -1, -1 };

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
  // Getters
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

  public int getAreaId(int index) {
    return areaIds[index];
  }
}
