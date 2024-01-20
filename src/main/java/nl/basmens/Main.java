package nl.basmens;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import nl.basmens.generation.KnotGenerationPipeline;
import nl.basmens.generation.Tileset;
import nl.basmens.generation.TilesetGenerator;
import nl.basmens.generation.analyzers.GridAnalyzerBasic;
import nl.basmens.generation.analyzers.GridAnalyzerDouble;
import nl.basmens.generation.generators.GridGeneratorBasic;
import nl.basmens.generation.generators.GridGeneratorDouble;
import nl.basmens.rendering.KnotRenderer;
import nl.basmens.utils.concurrent.PerformanceTimer;
import nl.basmens.utils.io.ResultExporter;
import nl.benmens.processing.PApplet;
import nl.benmens.processing.PAppletProxy;
import processing.core.PGraphics;
import processing.opengl.PGraphicsOpenGL;

public class Main extends PApplet {
  public static final String RESOURCE_PATH;
  public static final boolean SAVE_RESULTS = true;
  public static final boolean SAVE_TRICOLORABILITY = false;
  public static final boolean SAVE_KNOT_DETERMINANT = true;
  public static final boolean SAVE_ALEXANDER_POLYNOMIAL = true;
  public static final boolean MULTI_THREAD = true;
  private static final Tilesets TILESET = Tilesets.UNWEIGHTED;
  public static final boolean KEEP_DRAWABLE_KNOTS = false; // Preformance
  public static final long MAX_CALC_TIME_PER_INVARIANT = 500_000_000L; // In nanos
  public static final long TARGET_KNOT_COUNT = 1_000_000_000L;
  // Used to set the seed; ignore warning if no seed is given
  public static final Supplier<Random> RANDOM_FACTORY = () -> new Random(10);

  public final KnotRenderer knotRenderer = new KnotRenderer(true, true, false);
  private int size = 20;
  private int imgRes = 7;

  private enum Tilesets {
    BASIC(TilesetGenerator::getTilesetBasicFour),
    UNWEIGHTED(() -> TilesetGenerator.getTilesetDoubled(1, 1, 1, 1, 1, 1, 1, 1, 1)),
    WEIGHTED_HIGH(() -> TilesetGenerator.getTilesetDoubled(0, 1, 1, 1, 1, 0, 1, 1, 2)),
    WEIGHTED_LOW(() -> TilesetGenerator.getTilesetDoubled(1, 0, 0, 0, 0, 1, 0, 0, 2)),
    EXPANDED_UNWEIGHTED(() -> TilesetGenerator.getTilesetDoubledStraights(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1)),
    EXPANDED_WEIGHTED(() -> TilesetGenerator.getTilesetDoubledStraights(1, 1, 2, 1, 1, 1, 0, 2, 1, 1, 1, 0, 1, 2, 0, 1,
        0, 1, 1, 1, 1, 1, 0, 0));

    private final Supplier<Tileset> tilesetSupplier;
    private Tileset tileset;

    Tilesets(Supplier<Tileset> tilesetSupplier) {
      this.tilesetSupplier = tilesetSupplier;
    }

    public Tileset getTileset() {
      if (tileset == null) {
        tileset = tilesetSupplier.get();
      }
      return tileset;
    }
  }

  private KnotGenerationPipeline[] knotGenerationPipelines = new KnotGenerationPipeline[MULTI_THREAD ? 21 : 1];
  private ExecutorService threadPool = Executors.newFixedThreadPool(9);

  static {
    String path = "";
    try {
      URL resource = Main.class.getResource("/");
      path = Paths.get(resource.toURI()).toAbsolutePath().toString() + FileSystems.getDefault().getSeparator();
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }
    RESOURCE_PATH = path;
  }

  // ===================================================================================================================
  // Functionality
  // ===================================================================================================================
  private void startKnotGenerations() {
    for (int i = 0; i < knotGenerationPipelines.length; i++) {
      if (MULTI_THREAD) {
        size = i == 20 ? 3000 : (10 * (1 + i));
      }

      String fileName = TILESET.toString().toLowerCase(Locale.ENGLISH) + "/" + size + "x" + size;

      if (TILESET == Tilesets.BASIC) {
        knotGenerationPipelines[i] = new KnotGenerationPipeline(TILESET.getTileset(), size, size,
            GridGeneratorBasic::new, GridAnalyzerBasic::new, fileName);
      } else {
        knotGenerationPipelines[i] = new KnotGenerationPipeline(TILESET.getTileset(), size, size,
            GridGeneratorDouble::new, GridAnalyzerDouble::new, fileName);
      }

      startGenerationCycle(i);
    }
  }

  private void startGenerationCycle(int index) {
    knotRenderer.setKnotBeingViewed(0);

    if (MULTI_THREAD) {
      threadPool.execute(knotGenerationPipelines[index]);
    } else {
      knotGenerationPipelines[index].run();
    }
  }

  // ===================================================================================================================
  // Native processing functions for lifecycle
  // ===================================================================================================================
  @Override
  public void settings() {
    PAppletProxy.setSharedApplet(this);

    if (MULTI_THREAD) {
      size(300, 300, P2D);
    } else {
      size(1920, 1080, P2D);
    }
  }

  @Override
  public void setup() {
    // A workaround for noSmooth() not being compatible with P2D
    ((PGraphicsOpenGL) g).textureSampling(3);
    surface.setLocation(0, 0);

    if (MULTI_THREAD) {
      noLoop();
      new Thread(() -> {
        println();
        println("Starting...");
        startKnotGenerations();

        println();
        println("Awaiting termination...");
        threadPool.shutdown();
        try {
          threadPool.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException e) {
          stopKnotGenerationPipelines();
          e.printStackTrace();
        }

        println();
        println("Flushing data...");
        ResultExporter.saveAll();
        PerformanceTimer.flushData();
        println("Flushed data");

        println();
        println("Exiting");
        exit();
      }).start();
    } else {
      startKnotGenerations();
    }
  }

  @Override
  public void draw() {
    if (!MULTI_THREAD) {
      knotRenderer.display(knotGenerationPipelines[0], width, height);
    }
  }

  // ===================================================================================================================
  // Events
  // ===================================================================================================================
  @Override
  public void mousePressed() {
    if (!MULTI_THREAD) {
      for (int i = 0; i < knotGenerationPipelines.length; i++) {
        startGenerationCycle(i);
      }
    }
  }

  @Override
  public void keyPressed() {
    if (key == 'w') {
      knotRenderer
          .setKnotBeingViewed((knotRenderer.getKnotBeingViewed() + 1) % knotGenerationPipelines[0].getKnots().size());

    } else if (key == 's') {
      knotRenderer
          .setKnotBeingViewed((knotRenderer.getKnotBeingViewed() - 1 + knotGenerationPipelines[0].getKnots().size())
              % knotGenerationPipelines[0].getKnots().size());

    } else if (key == 'f') {
      if (MULTI_THREAD) {
        println();
        println("Finishing...");
        stopKnotGenerationPipelines();
      } else {
        println();
        println("Flushing data...");
        if (SAVE_RESULTS) {
          ResultExporter.saveAll();
        }
        PerformanceTimer.flushData();
        println("Flushed data");

        exit();
      }
    } else if (key == 'z' && !MULTI_THREAD) {
      println("Saving...");
      saveKnotImage();
      println("Saved");
    }
  }

  private void stopKnotGenerationPipelines() {
    threadPool.shutdown();
    // Terminate threads
    for (KnotGenerationPipeline p : knotGenerationPipelines) {
      p.stop();
    }
  }

  public void saveKnotImage() {
    // Init save
    PGraphics p = createGraphics(knotGenerationPipelines[0].getGridW() * imgRes,
        knotGenerationPipelines[0].getGridH() * imgRes);
    p.beginDraw();
    p.background(0);

    // Draw Tiles
    p.imageMode(CORNER);
    for (int x = 0; x < knotGenerationPipelines[0].getGridW(); x++) {
      for (int y = 0; y < knotGenerationPipelines[0].getGridH(); y++) {
        p.image(knotGenerationPipelines[0].getGenerator().getTileAtPos(x, y).img, x * imgRes, y * imgRes, imgRes,
            imgRes);
      }
    }

    // Save
    p.endDraw();
    String path = RESOURCE_PATH.substring(0, RESOURCE_PATH.length() - "target/classes/".length());
    p.save(path + "results/gen result.png");
  }

  // ===================================================================================================================
  // Main function
  // ===================================================================================================================
  public static void main(String[] passedArgs) {
    if (passedArgs != null) {
      PApplet.main(new Object() {
      }.getClass().getEnclosingClass(), passedArgs);
    } else {
      PApplet.main(new Object() {
      }.getClass().getEnclosingClass());
    }
  }
}
