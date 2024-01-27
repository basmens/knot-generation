package nl.basmens.utils.concurrent;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import nl.basmens.Main;
import nl.basmens.utils.io.ResultExporter;
import processing.data.JSONArray;
import processing.data.JSONObject;

public final class PerformanceTimer {
  private static final Node ROOT = new Node("ROOT");
  private static final ConcurrentHashMap<Thread, LinkedList<Node>> THREAD_SEGMENT_STACK = new ConcurrentHashMap<>();

  private final LinkedList<Node> segmentStack;
  private final Thread ownerThread;
  private final Node functionTimer;
  private final long startTime;

  private Node currentSegment;
  private long lastTimeStamp;

  public PerformanceTimer(Class<?> c, String timerName, String firstSegment) {
    if (!Main.PROFILE_PERFORMANCE) {
      this.startTime = 0;
      this.ownerThread = null;
      this.segmentStack = null;
      this.functionTimer = null;
      return;
    }

    timerName = c.getName() + " - " + timerName;

    ownerThread = Thread.currentThread();
    segmentStack = THREAD_SEGMENT_STACK.computeIfAbsent(ownerThread, (Thread t) -> {
      LinkedList<Node> stack = new LinkedList<>();
      stack.push(ROOT);
      return stack;
    });
    functionTimer = segmentStack.peek().getChild(timerName);
    segmentStack.push(functionTimer.getChild(firstSegment));

    this.startTime = System.nanoTime();
    this.currentSegment = functionTimer.getChild(firstSegment);
    this.lastTimeStamp = this.startTime;
  }

  public PerformanceTimer(Class<?> c, String timerName) {
    this(c, timerName, timerName);
  }

  public void nextSegment(String nextSegmentName) {
    if (!Main.PROFILE_PERFORMANCE) {
      return;
    }

    if (Thread.currentThread() != ownerThread) {
      throw new RuntimeException("PerformanceTimer must be used in the same thread as the one who created it");
    }

    // Add duration
    long now = System.nanoTime();
    currentSegment.addDuration(now - lastTimeStamp);

    // Update variables
    segmentStack.pop();
    if (nextSegmentName != null) {
      lastTimeStamp = now;
      currentSegment = functionTimer.getChild(nextSegmentName);
      segmentStack.push(currentSegment);
    }
  }

  public void stop() {
    if (!Main.PROFILE_PERFORMANCE) {
      return;
    }

    if (Thread.currentThread() != ownerThread) {
      throw new RuntimeException("PerformanceTimer must be used in the same thread as the one who created it");
    }
    
    if (currentSegment != null) {
      nextSegment(null);
      currentSegment = null;
      functionTimer.addDuration(System.nanoTime() - startTime);
    }
  }


  public static void flushData() {
    if (!Main.PROFILE_PERFORMANCE) {
      return;
    }

    try {
      String p = Paths.get(ResultExporter.class.getResource("/").toURI()).toAbsolutePath().toString();
      p = p.substring(0, p.length() - "target/classes".length());
      p += "logs/performance.json";
      File f = new File(p);

      JSONArray childrenArray = new JSONArray();
      ROOT.children.entrySet().forEach((Entry<String, Node> e) -> childrenArray.append(e.getValue().toJson()));
      
      JSONObject result = new JSONObject();
      result.setJSONArray("root", childrenArray);
      
      result.save(f, "indent=2");
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }
  }

  // ===================================================================================================================
  // Node
  // ===================================================================================================================
  // Node alternates representing function timers and segments of a function timer
  private static class Node {
    protected final Map<String, Node> children = Collections.synchronizedMap(new HashMap<>());
    public final String name;

    private long totalDuration;
    private int hitCount;

    public Node(String name) {
      this.name = name;
    }

    public Node getChild(String name) {
      return children.computeIfAbsent(name, Node::new);
    }

    public synchronized void addDuration(long duration) {
      totalDuration += duration;
      hitCount++;
    }

    public JSONObject toJson() {
      JSONArray childrenArray = new JSONArray();
      children.entrySet().forEach((Entry<String, Node> e) -> childrenArray.append(e.getValue().toJson()));

      JSONObject result = new JSONObject();
      result.setString("name", name);
      result.setLong("total duration", totalDuration);
      result.setInt("hit count", hitCount);
      result.setJSONArray("children", childrenArray);
      return result;
    }
  }
}
