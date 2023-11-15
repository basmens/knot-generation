package nl.basmens.knot;

public class Intersection {
  public final Connection under;
  public final Connection over;

  public Intersection(Connection under, Connection over) {
    this.under = under;
    this.over = over;

    under.setIntersection(this);
    over.setIntersection(this);
  }
}
