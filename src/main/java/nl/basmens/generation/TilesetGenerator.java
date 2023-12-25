package nl.basmens.generation;

import java.util.ArrayList;
import java.util.Arrays;

import nl.basmens.Main;
import nl.basmens.knot.Connection;
import nl.basmens.utils.Vector;
import nl.benmens.processing.PAppletProxy;

public class TilesetGenerator {
  private TilesetGenerator() {}

  // ===================================================================================================================
  // Tileset basic four
  // ===================================================================================================================
  public static Tileset getTilesetBasicFour() {
    // Corner 0
    ArrayList<Tile> tiles = new ArrayList<>();
    String path = Main.RESOURCE_PATH + "tilesets/basic_four/";

    tiles.add(new Tile(PAppletProxy.loadImage(path + "corner0.png")) {
      @Override
      public int setConnectionInputGoingUp(int x, int y, Connection[][] hor, Connection[][] vert,
          IntersectedConnectionsFactory intersectedConnections) {
        vert[x][y].setNext(hor[x - 1][y]);
        return 3;
      }

      @Override
      public int setConnectionInputGoingLeft(int x, int y, Connection[][] hor, Connection[][] vert,
          IntersectedConnectionsFactory intersectedConnections) {
        hor[x][y].setNext(vert[x][y - 1]);
        return 0;
      }

      @Override
      public int setConnectionInputGoingDown(int x, int y, Connection[][] hor, Connection[][] vert,
          IntersectedConnectionsFactory intersectedConnections) {
        vert[x][y - 1].setNext(hor[x][y]);
        return 1;
      }

      @Override
      public int setConnectionInputGoingRight(int x, int y, Connection[][] hor, Connection[][] vert,
          IntersectedConnectionsFactory intersectedConnections) {
        hor[x - 1][y].setNext(vert[x][y]);
        return 2;
      }
    });

    // Corner 1
    tiles.add(new Tile(PAppletProxy.loadImage(path + "corner1.png")) {
      @Override
      public int setConnectionInputGoingUp(int x, int y, Connection[][] hor, Connection[][] vert,
          IntersectedConnectionsFactory intersectedConnections) {
        vert[x][y].setNext(hor[x][y]);
        return 1;
      }

      @Override
      public int setConnectionInputGoingLeft(int x, int y, Connection[][] hor, Connection[][] vert,
          IntersectedConnectionsFactory intersectedConnections) {
        hor[x][y].setNext(vert[x][y]);
        return 2;
      }

      @Override
      public int setConnectionInputGoingDown(int x, int y, Connection[][] hor, Connection[][] vert,
          IntersectedConnectionsFactory intersectedConnections) {
        vert[x][y - 1].setNext(hor[x - 1][y]);
        return 3;
      }

      @Override
      public int setConnectionInputGoingRight(int x, int y, Connection[][] hor, Connection[][] vert,
          IntersectedConnectionsFactory intersectedConnections) {
        hor[x - 1][y].setNext(vert[x][y - 1]);
        return 0;
      }
    });

    // Intersection 0
    tiles.add(new Tile(PAppletProxy.loadImage(path + "intersection0.png")) {
      @Override
      public int setConnectionInputGoingUp(int x, int y, Connection[][] hor, Connection[][] vert,
          IntersectedConnectionsFactory intersectedConnections) {
        Connection c = intersectedConnections.getConnectionA(x, y);
        c.setDir(-Math.PI / 2);
        vert[x][y].setNext(c);
        c.setNext(vert[x][y - 1]);
        return 0;
      }

      @Override
      public int setConnectionInputGoingLeft(int x, int y, Connection[][] hor, Connection[][] vert,
          IntersectedConnectionsFactory intersectedConnections) {
        Connection c = intersectedConnections.getConnectionB(x, y);
        c.setDir(Math.PI);
        hor[x][y].setNext(c);
        c.setNext(hor[x - 1][y]);
        return 3;
      }

      @Override
      public int setConnectionInputGoingDown(int x, int y, Connection[][] hor, Connection[][] vert,
          IntersectedConnectionsFactory intersectedConnections) {
        Connection c = intersectedConnections.getConnectionA(x, y);
        c.setDir(Math.PI / 2);
        vert[x][y - 1].setNext(c);
        c.setNext(vert[x][y]);
        return 2;
      }

      @Override
      public int setConnectionInputGoingRight(int x, int y, Connection[][] hor, Connection[][] vert,
          IntersectedConnectionsFactory intersectedConnections) {
        Connection c = intersectedConnections.getConnectionB(x, y);
        c.setDir(0);
        hor[x - 1][y].setNext(c);
        c.setNext(hor[x][y]);
        return 1;
      }
    });

    // Intersection 1
    tiles.add(new Tile(PAppletProxy.loadImage(path + "intersection1.png")) {
      @Override
      public int setConnectionInputGoingUp(int x, int y, Connection[][] hor, Connection[][] vert,
          IntersectedConnectionsFactory intersectedConnections) {
        Connection c = intersectedConnections.getConnectionA(x, y);
        c.setDir(-Math.PI / 2);
        vert[x][y].setNext(c);
        c.setNext(vert[x][y - 1]);
        return 0;
      }

      @Override
      public int setConnectionInputGoingLeft(int x, int y, Connection[][] hor, Connection[][] vert,
          IntersectedConnectionsFactory intersectedConnections) {
        Connection c = intersectedConnections.getConnectionB(x, y);
        c.setDir(Math.PI);
        hor[x][y].setNext(c);
        c.setNext(hor[x - 1][y]);
        return 3;
      }

      @Override
      public int setConnectionInputGoingDown(int x, int y, Connection[][] hor, Connection[][] vert,
          IntersectedConnectionsFactory intersectedConnections) {
        Connection c = intersectedConnections.getConnectionA(x, y);
        c.setDir(Math.PI / 2);
        vert[x][y - 1].setNext(c);
        c.setNext(vert[x][y]);
        return 2;
      }

      @Override
      public int setConnectionInputGoingRight(int x, int y, Connection[][] hor, Connection[][] vert,
          IntersectedConnectionsFactory intersectedConnections) {
        Connection c = intersectedConnections.getConnectionB(x, y);
        c.setDir(0);
        hor[x - 1][y].setNext(c);
        c.setNext(hor[x][y]);
        return 1;
      }
    });

    return new Tileset(tiles.toArray(Tile[]::new));
  }

  // ===================================================================================================================
  // Tileset doubled
  // ===================================================================================================================
  public static Tileset getTilesetDoubled(int rrrr, int rlrl, int rlbb, int lrlr, int lbbr, int llll, int brlb,
      int bbrl, int bbbb) {
    ArrayList<Tile> tiles = new ArrayList<>();
    String path = Main.RESOURCE_PATH + "tilesets/doubled/";

    // rrrr
    for (int i = 0; i < rrrr; i++) {
      tiles.add(new Tile(PAppletProxy.loadImage(path + "rrrr.png")) {
        @Override
        public void setConnections(int x, int y, Connection[][] hor, Connection[][] vert) {
          vert[x * 2 + 1][y].setNext(hor[x][y * 2 + 1]);
          hor[x][y * 2].setNext(vert[x * 2 + 1][y - 1]);
          vert[x * 2][y - 1].setNext(hor[x - 1][y * 2]);
          hor[x - 1][y * 2 + 1].setNext(vert[x * 2][y]);
        }
      });
    }

    // rlrl
    for (int i = 0; i < rlrl; i++) {
      tiles.add(new Tile(PAppletProxy.loadImage(path + "rlrl.png")) {

        @Override
        public void setConnections(int x, int y, Connection[][] hor, Connection[][] vert) {
          vert[x * 2 + 1][y].setNext(hor[x][y * 2 + 1]);
          hor[x][y * 2].setNext(vert[x * 2][y]);
          vert[x * 2][y - 1].setNext(hor[x - 1][y * 2]);
          hor[x - 1][y * 2 + 1].setNext(vert[x * 2 + 1][y - 1]);
        }
      });
    }

    // rlbb
    for (int i = 0; i < rlbb; i++) {
      tiles.add(new Tile(PAppletProxy.loadImage(path + "rlbb.png")) {
        @Override
        public void setConnections(int x, int y, Connection[][] hor, Connection[][] vert) {
          vert[x * 2 + 1][y].setNext(hor[x][y * 2 + 1]);
          hor[x][y * 2].setNext(vert[x * 2][y]);
          vert[x * 2][y - 1].setNext(vert[x * 2 + 1][y - 1]);
          hor[x - 1][y * 2 + 1].setNext(hor[x - 1][y * 2]);
        }
      });
    }

    // lrlr
    for (int i = 0; i < lrlr; i++) {
      tiles.add(new Tile(PAppletProxy.loadImage(path + "lrlr.png")) {
        @Override
        public void setConnections(int x, int y, Connection[][] hor, Connection[][] vert) {
          vert[x * 2 + 1][y].setNext(hor[x - 1][y * 2]);
          hor[x][y * 2].setNext(vert[x * 2 + 1][y - 1]);
          vert[x * 2][y - 1].setNext(hor[x][y * 2 + 1]);
          hor[x - 1][y * 2 + 1].setNext(vert[x * 2][y]);
        }
      });
    }

    // lbbr
    for (int i = 0; i < lbbr; i++) {
      tiles.add(new Tile(PAppletProxy.loadImage(path + "lbbr.png")) {
        @Override
        public void setConnections(int x, int y, Connection[][] hor, Connection[][] vert) {
          vert[x * 2 + 1][y].setNext(hor[x - 1][y * 2]);
          hor[x][y * 2].setNext(hor[x][y * 2 + 1]);
          vert[x * 2][y - 1].setNext(vert[x * 2 + 1][y - 1]);
          hor[x - 1][y * 2 + 1].setNext(vert[x * 2][y]);
        }
      });
    }

    // llll
    for (int i = 0; i < llll; i++) {
      tiles.add(new Tile(PAppletProxy.loadImage(path + "llll.png")) {
        @Override
        public void setConnections(int x, int y, Connection[][] hor, Connection[][] vert) {
          Connection upIn = new Connection(new Vector(x, y - 0.2), Math.PI * 0.35);
          Connection upOut = new Connection(new Vector(x, y - 0.2), -Math.PI * 0.35);
          Connection leftIn = new Connection(new Vector(x - 0.2, y), -Math.PI * 0.15);
          Connection leftOut = new Connection(new Vector(x - 0.2, y), -Math.PI * 0.85);
          Connection downIn = new Connection(new Vector(x, y + 0.2), -Math.PI * 0.65);
          Connection downOut = new Connection(new Vector(x, y + 0.2), Math.PI * 0.65);
          Connection rightIn = new Connection(new Vector(x + 0.2, y), Math.PI * 0.85);
          Connection rightOut = new Connection(new Vector(x + 0.2, y), Math.PI * 0.15);
          IntersectedConnectionsFactory.createIntersection(upIn, upOut);
          IntersectedConnectionsFactory.createIntersection(leftIn, leftOut);
          IntersectedConnectionsFactory.createIntersection(downIn, downOut);
          IntersectedConnectionsFactory.createIntersection(rightIn, rightOut);

          vert[x * 2 + 1][y].setNext(downIn);
          downIn.setNext(leftOut);
          leftOut.setNext(hor[x - 1][y * 2]);
          hor[x][y * 2].setNext(rightIn);
          rightIn.setNext(downOut);
          downOut.setNext(vert[x * 2][y]);
          vert[x * 2][y - 1].setNext(upIn);
          upIn.setNext(rightOut);
          rightOut.setNext(hor[x][y * 2 + 1]);
          hor[x - 1][y * 2 + 1].setNext(leftIn);
          leftIn.setNext(upOut);
          upOut.setNext(vert[x * 2 + 1][y - 1]);
        }
      });
    }

    // brlb
    for (int i = 0; i < brlb; i++) {
      tiles.add(new Tile(PAppletProxy.loadImage(path + "brlb.png")) {
        @Override
        public void setConnections(int x, int y, Connection[][] hor, Connection[][] vert) {
          vert[x * 2 + 1][y].setNext(vert[x * 2][y]);
          hor[x][y * 2].setNext(vert[x * 2 + 1][y - 1]);
          vert[x * 2][y - 1].setNext(hor[x][y * 2 + 1]);
          hor[x - 1][y * 2 + 1].setNext(hor[x - 1][y * 2]);
        }
      });
    }

    // bbrl
    for (int i = 0; i < bbrl; i++) {
      tiles.add(new Tile(PAppletProxy.loadImage(path + "bbrl.png")) {
        @Override
        public void setConnections(int x, int y, Connection[][] hor, Connection[][] vert) {
          vert[x * 2 + 1][y].setNext(vert[x * 2][y]);
          hor[x][y * 2].setNext(hor[x][y * 2 + 1]);
          vert[x * 2][y - 1].setNext(hor[x - 1][y * 2]);
          hor[x - 1][y * 2 + 1].setNext(vert[x * 2 + 1][y - 1]);
        }
      });
    }

    // bbbb
    for (int i = 0; i < bbbb; i++) {
      tiles.add(new Tile(PAppletProxy.loadImage(path + "bbbb.png")) {
        @Override
        public void setConnections(int x, int y, Connection[][] hor, Connection[][] vert) {
          vert[x * 2 + 1][y].setNext(vert[x * 2][y]);
          hor[x][y * 2].setNext(hor[x][y * 2 + 1]);
          vert[x * 2][y - 1].setNext(vert[x * 2 + 1][y - 1]);
          hor[x - 1][y * 2 + 1].setNext(hor[x - 1][y * 2]);
        }
      });
    }

    return new Tileset(tiles.toArray(Tile[]::new));
  }

  // ===================================================================================================================
  // Tileset doubled with straights
  // ===================================================================================================================
  public static Tileset getTilesetDoubledStraights(int rrrr, int rsbr, int rlrl, int rlbb, int rrsb, int rssl,
      int lrlr, int lbbr, int lbsl, int lrss, int llbs, int llll, int bbbb, int brlb, int bsbs, int brrs, int bbrl,
      int bsll, int sbrr, int sslr, int sbsb, int sllb, int ssss, int slrs) {
    ArrayList<Tile> tiles = new ArrayList<>(
        Arrays.asList(getTilesetDoubled(rrrr, rlrl, rlbb, lrlr, lbbr, llll, brlb, bbrl, bbbb).getTiles()));
    String path = Main.RESOURCE_PATH + "tilesets/doubled_with_straights/";

    // rsbr
    for (int i = 0; i < rsbr; i++) {
      tiles.add(new Tile(PAppletProxy.loadImage(path + "rsbr.png")) {
        @Override
        public void setConnections(int x, int y, Connection[][] hor, Connection[][] vert) {
          vert[x * 2 + 1][y].setNext(hor[x][y * 2 + 1]);
          hor[x][y * 2].setNext(hor[x - 1][y * 2]);
          vert[x * 2][y - 1].setNext(vert[x * 2 + 1][y - 1]);
          hor[x - 1][y * 2 + 1].setNext(vert[x * 2][y]);
        }
      });
    }

    // rrsb
    for (int i = 0; i < rrsb; i++) {
      tiles.add(new Tile(PAppletProxy.loadImage(path + "rrsb.png")) {

        @Override
        public void setConnections(int x, int y, Connection[][] hor, Connection[][] vert) {
          vert[x * 2 + 1][y].setNext(hor[x][y * 2 + 1]);
          hor[x][y * 2].setNext(vert[x * 2 + 1][y - 1]);
          vert[x * 2][y - 1].setNext(vert[x * 2][y]);
          hor[x - 1][y * 2 + 1].setNext(hor[x - 1][y * 2]);
        }
      });
    }

    // rssl
    for (int i = 0; i < rssl; i++) {
      tiles.add(new Tile(PAppletProxy.loadImage(path + "rssl.png")) {
        @Override
        public void setConnections(int x, int y, Connection[][] hor, Connection[][] vert) {
          Connection left1 = new Connection(new Vector(x, y - 0.15), Math.PI);
          Connection left2 = new Connection(new Vector(x - 0.15, y - 0.15), Math.PI);
          Connection down1 = new Connection(new Vector(x - 0.15, y - 0.15), Math.PI / 2);
          Connection down2 = new Connection(new Vector(x - 0.15, y), Math.PI / 2);
          Connection right1 = new Connection(new Vector(x - 0.15, y), -Math.PI * 0.15);
          Connection right2 = new Connection(new Vector(x, y - 0.15), -Math.PI * 0.35);
          IntersectedConnectionsFactory.createIntersection(left1, right2);
          IntersectedConnectionsFactory.createIntersection(left2, down1);
          IntersectedConnectionsFactory.createIntersection(down2, right1);

          vert[x * 2 + 1][y].setNext(hor[x][y * 2 + 1]);
          hor[x][y * 2].setNext(left1);
          left1.setNext(left2);
          left2.setNext(hor[x - 1][y * 2]);
          vert[x * 2][y - 1].setNext(down1);
          down1.setNext(down2);
          down2.setNext(vert[x * 2][y]);
          hor[x - 1][y * 2 + 1].setNext(right1);
          right1.setNext(right2);
          right2.setNext(vert[x * 2 + 1][y - 1]);
        }
      });
    }

    // lbsl
    for (int i = 0; i < lbsl; i++) {
      tiles.add(new Tile(PAppletProxy.loadImage(path + "lbsl.png")) {
        @Override
        public void setConnections(int x, int y, Connection[][] hor, Connection[][] vert) {
          Connection up1 = new Connection(new Vector(x, y), -Math.PI * 0.65);
          Connection up2 = new Connection(new Vector(x - 0.15, y - 0.1), -Math.PI * 0.85);
          Connection down1 = new Connection(new Vector(x - 0.15, y - 0.1), Math.PI / 2);
          Connection down2 = new Connection(new Vector(x - 0.15, y + 0.1), Math.PI / 2);
          Connection right1 = new Connection(new Vector(x - 0.15, y + 0.1), -Math.PI * 0.15);
          Connection right2 = new Connection(new Vector(x, y), -Math.PI * 0.35);
          IntersectedConnectionsFactory.createIntersection(up1, right2);
          IntersectedConnectionsFactory.createIntersection(up2, down1);
          IntersectedConnectionsFactory.createIntersection(down2, right1);

          vert[x * 2 + 1][y].setNext(up1);
          up1.setNext(up2);
          up2.setNext(hor[x - 1][y * 2]);
          hor[x][y * 2].setNext(hor[x][y * 2 + 1]);
          vert[x * 2][y - 1].setNext(down1);
          down1.setNext(down2);
          down2.setNext(vert[x * 2][y]);
          hor[x - 1][y * 2 + 1].setNext(right1);
          right1.setNext(right2);
          right2.setNext(vert[x * 2 + 1][y - 1]);
        }
      });
    }

    // lrss
    for (int i = 0; i < lrss; i++) {
      tiles.add(new Tile(PAppletProxy.loadImage(path + "lrss.png")) {
        @Override
        public void setConnections(int x, int y, Connection[][] hor, Connection[][] vert) {
          Connection up1 = new Connection(new Vector(x, y + 0.15), -Math.PI * 0.65);
          Connection up2 = new Connection(new Vector(x - 0.15, y), -Math.PI * 0.85);
          Connection down1 = new Connection(new Vector(x - 0.15, y), Math.PI / 2);
          Connection down2 = new Connection(new Vector(x - 0.15, y + 0.15), Math.PI / 2);
          Connection right1 = new Connection(new Vector(x - 0.15, y + 0.15), 0);
          Connection right2 = new Connection(new Vector(x, y + 0.15), 0);
          IntersectedConnectionsFactory.createIntersection(up1, right2);
          IntersectedConnectionsFactory.createIntersection(up2, down1);
          IntersectedConnectionsFactory.createIntersection(down2, right1);

          vert[x * 2 + 1][y].setNext(up1);
          up1.setNext(up2);
          up2.setNext(hor[x - 1][y * 2]);
          hor[x][y * 2].setNext(vert[x * 2 + 1][y - 1]);
          vert[x * 2][y - 1].setNext(down1);
          down1.setNext(down2);
          down2.setNext(vert[x * 2][y]);
          hor[x - 1][y * 2 + 1].setNext(right1);
          right1.setNext(right2);
          right2.setNext(hor[x][y * 2 + 1]);
        }
      });
    }

    // llbs
    for (int i = 0; i < llbs; i++) {
      tiles.add(new Tile(PAppletProxy.loadImage(path + "llbs.png")) {
        @Override
        public void setConnections(int x, int y, Connection[][] hor, Connection[][] vert) {
          Connection up1 = new Connection(new Vector(x + 0.1, y + 0.15), -Math.PI * 0.65);
          Connection up2 = new Connection(new Vector(x, y), -Math.PI * 0.85);
          Connection left1 = new Connection(new Vector(x, y), Math.PI * 0.85);
          Connection left2 = new Connection(new Vector(x - 0.1, y + 0.15), Math.PI * 0.65);
          Connection right1 = new Connection(new Vector(x - 0.1, y + 0.15), 0);
          Connection right2 = new Connection(new Vector(x + 0.1, y + 0.15), 0);
          IntersectedConnectionsFactory.createIntersection(up1, right2);
          IntersectedConnectionsFactory.createIntersection(up2, left1);
          IntersectedConnectionsFactory.createIntersection(left2, right1);

          vert[x * 2 + 1][y].setNext(up1);
          up1.setNext(up2);
          up2.setNext(hor[x - 1][y * 2]);
          hor[x][y * 2].setNext(left1);
          left1.setNext(left2);
          left2.setNext(vert[x * 2][y]);
          vert[x * 2][y - 1].setNext(vert[x * 2 + 1][y - 1]);
          hor[x - 1][y * 2 + 1].setNext(right1);
          right1.setNext(right2);
          right2.setNext(hor[x][y * 2 + 1]);
        }
      });
    }

    // bsbs
    for (int i = 0; i < bsbs; i++) {
      tiles.add(new Tile(PAppletProxy.loadImage(path + "bsbs.png")) {
        @Override
        public void setConnections(int x, int y, Connection[][] hor, Connection[][] vert) {
          vert[x * 2 + 1][y].setNext(vert[x * 2][y]);
          hor[x][y * 2].setNext(hor[x - 1][y * 2]);
          vert[x * 2][y - 1].setNext(vert[x * 2 + 1][y - 1]);
          hor[x - 1][y * 2 + 1].setNext(hor[x][y * 2 + 1]);
        }
      });
    }

    // brrs
    for (int i = 0; i < brrs; i++) {
      tiles.add(new Tile(PAppletProxy.loadImage(path + "brrs.png")) {
        @Override
        public void setConnections(int x, int y, Connection[][] hor, Connection[][] vert) {
          vert[x * 2 + 1][y].setNext(vert[x * 2][y]);
          hor[x][y * 2].setNext(vert[x * 2 + 1][y - 1]);
          vert[x * 2][y - 1].setNext(hor[x - 1][y * 2]);
          hor[x - 1][y * 2 + 1].setNext(hor[x][y * 2 + 1]);
        }
      });
    }

    // bsll
    for (int i = 0; i < bsll; i++) {
      tiles.add(new Tile(PAppletProxy.loadImage(path + "bsll.png")) {
        @Override
        public void setConnections(int x, int y, Connection[][] hor, Connection[][] vert) {
          Connection left1 = new Connection(new Vector(x + 0.1, y - 0.15), Math.PI);
          Connection left2 = new Connection(new Vector(x - 0.1, y - 0.15), Math.PI);
          Connection down1 = new Connection(new Vector(x - 0.1, y - 0.15), Math.PI * 0.35);
          Connection down2 = new Connection(new Vector(x, y), Math.PI * 0.15);
          Connection right1 = new Connection(new Vector(x, y), -Math.PI * 0.15);
          Connection right2 = new Connection(new Vector(x + 0.1, y - 0.15), -Math.PI * 0.35);
          IntersectedConnectionsFactory.createIntersection(left1, right2);
          IntersectedConnectionsFactory.createIntersection(left2, down1);
          IntersectedConnectionsFactory.createIntersection(down2, right1);

          vert[x * 2 + 1][y].setNext(vert[x * 2][y]);
          hor[x][y * 2].setNext(left1);
          left1.setNext(left2);
          left2.setNext(hor[x - 1][y * 2]);
          vert[x * 2][y - 1].setNext(down1);
          down1.setNext(down2);
          down2.setNext(hor[x][y * 2 + 1]);
          hor[x - 1][y * 2 + 1].setNext(right1);
          right1.setNext(right2);
          right2.setNext(vert[x * 2 + 1][y - 1]);
        }
      });
    }

    // sbrr
    for (int i = 0; i < sbrr; i++) {
      tiles.add(new Tile(PAppletProxy.loadImage(path + "sbrr.png")) {
        @Override
        public void setConnections(int x, int y, Connection[][] hor, Connection[][] vert) {
          vert[x * 2 + 1][y].setNext(vert[x * 2 + 1][y - 1]);
          hor[x][y * 2].setNext(hor[x][y * 2 + 1]);
          vert[x * 2][y - 1].setNext(hor[x - 1][y * 2]);
          hor[x - 1][y * 2 + 1].setNext(vert[x * 2][y]);
        }
      });
    }

    // sslr
    for (int i = 0; i < sslr; i++) {
      tiles.add(new Tile(PAppletProxy.loadImage(path + "sslr.png")) {
        @Override
        public void setConnections(int x, int y, Connection[][] hor, Connection[][] vert) {
          Connection up1 = new Connection(new Vector(x + 0.15, y), -Math.PI / 2);
          Connection up2 = new Connection(new Vector(x + 0.15, y - 0.15), -Math.PI / 2);
          Connection left1 = new Connection(new Vector(x + 0.15, y - 0.15), Math.PI);
          Connection left2 = new Connection(new Vector(x, y - 0.15), Math.PI);
          Connection down1 = new Connection(new Vector(x, y - 0.15), Math.PI * 0.35);
          Connection down2 = new Connection(new Vector(x + 0.15, y), Math.PI * 0.15);
          IntersectedConnectionsFactory.createIntersection(up1, down2);
          IntersectedConnectionsFactory.createIntersection(up2, left1);
          IntersectedConnectionsFactory.createIntersection(left2, down1);

          vert[x * 2 + 1][y].setNext(up1);
          up1.setNext(up2);
          up2.setNext(vert[x * 2 + 1][y - 1]);
          hor[x][y * 2].setNext(left1);
          left1.setNext(left2);
          left2.setNext(hor[x - 1][y * 2]);
          vert[x * 2][y - 1].setNext(down1);
          down1.setNext(down2);
          down2.setNext(hor[x][y * 2 + 1]);
          hor[x - 1][y * 2 + 1].setNext(vert[x * 2][y]);
        }
      });
    }

    // sbsb
    for (int i = 0; i < sbsb; i++) {
      tiles.add(new Tile(PAppletProxy.loadImage(path + "sbsb.png")) {
        @Override
        public void setConnections(int x, int y, Connection[][] hor, Connection[][] vert) {
          vert[x * 2 + 1][y].setNext(vert[x * 2 + 1][y - 1]);
          hor[x][y * 2].setNext(hor[x][y * 2 + 1]);
          vert[x * 2][y - 1].setNext(vert[x * 2][y]);
          hor[x - 1][y * 2 + 1].setNext(hor[x - 1][y * 2]);
        }
      });
    }

    // sllb
    for (int i = 0; i < sllb; i++) {
      tiles.add(new Tile(PAppletProxy.loadImage(path + "sllb.png")) {
        @Override
        public void setConnections(int x, int y, Connection[][] hor, Connection[][] vert) {
          Connection up1 = new Connection(new Vector(x + 0.15, y + 0.1), -Math.PI / 2);
          Connection up2 = new Connection(new Vector(x + 0.15, y - 0.1), -Math.PI / 2);
          Connection left1 = new Connection(new Vector(x + 0.15, y - 0.1), Math.PI * 0.85);
          Connection left2 = new Connection(new Vector(x, y), Math.PI * 0.65);
          Connection down1 = new Connection(new Vector(x, y), Math.PI * 0.35);
          Connection down2 = new Connection(new Vector(x + 0.15, y + 0.1), Math.PI * 0.15);
          IntersectedConnectionsFactory.createIntersection(up1, down2);
          IntersectedConnectionsFactory.createIntersection(up2, left1);
          IntersectedConnectionsFactory.createIntersection(left2, down1);

          vert[x * 2 + 1][y].setNext(up1);
          up1.setNext(up2);
          up2.setNext(vert[x * 2 + 1][y - 1]);
          hor[x][y * 2].setNext(left1);
          left1.setNext(left2);
          left2.setNext(vert[x * 2][y]);
          vert[x * 2][y - 1].setNext(down1);
          down1.setNext(down2);
          down2.setNext(hor[x][y * 2 + 1]);
          hor[x - 1][y * 2 + 1].setNext(hor[x - 1][y * 2]);
        }
      });
    }

    // ssss
    for (int i = 0; i < ssss; i++) {
      tiles.add(new Tile(PAppletProxy.loadImage(path + "ssss.png")) {
        @Override
        public void setConnections(int x, int y, Connection[][] hor, Connection[][] vert) {
          Connection up1 = new Connection(new Vector(x + 0.15, y + 0.15), -Math.PI / 2);
          Connection up2 = new Connection(new Vector(x + 0.15, y - 0.15), -Math.PI / 2);
          Connection left1 = new Connection(new Vector(x + 0.15, y - 0.15), Math.PI);
          Connection left2 = new Connection(new Vector(x - 0.15, y - 0.15), Math.PI);
          Connection down1 = new Connection(new Vector(x - 0.15, y - 0.15), Math.PI / 2);
          Connection down2 = new Connection(new Vector(x - 0.15, y + 0.15), Math.PI / 2);
          Connection right1 = new Connection(new Vector(x - 0.15, y + 0.15), 0);
          Connection right2 = new Connection(new Vector(x + 0.15, y + 0.15), 0);
          IntersectedConnectionsFactory.createIntersection(up1, right2);
          IntersectedConnectionsFactory.createIntersection(up2, left1);
          IntersectedConnectionsFactory.createIntersection(left2, down1);
          IntersectedConnectionsFactory.createIntersection(down2, right1);

          vert[x * 2 + 1][y].setNext(up1);
          up1.setNext(up2);
          up2.setNext(vert[x * 2 + 1][y - 1]);
          hor[x][y * 2].setNext(left1);
          left1.setNext(left2);
          left2.setNext(hor[x - 1][y * 2]);
          vert[x * 2][y - 1].setNext(down1);
          down1.setNext(down2);
          down2.setNext(vert[x * 2][y]);
          hor[x - 1][y * 2 + 1].setNext(right1);
          right1.setNext(right2);
          right2.setNext(hor[x][y * 2 + 1]);
        }
      });
    }

    // slrs
    for (int i = 0; i < slrs; i++) {
      tiles.add(new Tile(PAppletProxy.loadImage(path + "slrs.png")) {
        @Override
        public void setConnections(int x, int y, Connection[][] hor, Connection[][] vert) {
          Connection up1 = new Connection(new Vector(x + 0.15, y + 0.15), -Math.PI / 2);
          Connection up2 = new Connection(new Vector(x + 0.15, y), -Math.PI / 2);
          Connection left1 = new Connection(new Vector(x + 0.15, y), Math.PI * 0.85);
          Connection left2 = new Connection(new Vector(x, y + 0.15), Math.PI * 0.65);
          Connection right1 = new Connection(new Vector(x, y + 0.15), 0);
          Connection right2 = new Connection(new Vector(x + 0.15, y + 0.15), 0);
          IntersectedConnectionsFactory.createIntersection(up1, right2);
          IntersectedConnectionsFactory.createIntersection(up2, left1);
          IntersectedConnectionsFactory.createIntersection(left2, right1);

          vert[x * 2 + 1][y].setNext(up1);
          up1.setNext(up2);
          up2.setNext(vert[x * 2 + 1][y - 1]);
          hor[x][y * 2].setNext(left1);
          left1.setNext(left2);
          left2.setNext(vert[x * 2][y]);
          vert[x * 2][y - 1].setNext(hor[x - 1][y * 2]);
          hor[x - 1][y * 2 + 1].setNext(right1);
          right1.setNext(right2);
          right2.setNext(hor[x][y * 2 + 1]);
        }
      });
    }

    return new Tileset(tiles.toArray(Tile[]::new));
  }
}
