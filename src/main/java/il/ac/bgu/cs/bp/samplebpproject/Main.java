package il.ac.bgu.cs.bp.samplebpproject;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.ResourceBProgram;
import il.ac.bgu.cs.bp.samplebpproject.levelCrossing.LevelCrossingMain;
import il.ac.bgu.cs.bp.statespacemapper.MapperResult;
import il.ac.bgu.cs.bp.statespacemapper.StateSpaceMapper;
import il.ac.bgu.cs.bp.statespacemapper.jgrapht.exports.DotExporter;
import il.ac.bgu.cs.bp.statespacemapper.jgrapht.exports.GoalExporter;
import org.jgrapht.nio.DefaultAttribute;

import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collectors;

public class Main {
  public static void main(String[] args) throws Exception {
    if(args.length<1) {
      System.out.println("The first argument must include the js filename");
    }
    if(args[0].startsWith("levelCrossing/")) {
      LevelCrossingMain.main(args);
    } else {
      mainForNonLC(args);
    }
  }
  public static void mainForNonLC(String[] args) throws Exception {
    // This will load the program file  <Project>/src/main/resources/HelloBPjsWorld.js
//    final BProgram bprog = new ResourceBProgram("DiningPhilosophers.js");
    final BProgram bprog = new ResourceBProgram("HelloBPjsWorld.js");
    var runName = bprog.getName();

    // You can use a different EventSelectionStrategy, for example:
    /* var ess = new PrioritizedBSyncEventSelectionStrategy();
    bprog.setEventSelectionStrategy(ess); */
    var mpr = new StateSpaceMapper();
    var res = mpr.mapSpace(bprog);
    System.out.println("// completed mapping the states graph");
    System.out.println(res.toString());

    System.out.println("// Export to GraphViz...");
    var outputDir = "exports";
    var pathDot = Paths.get(outputDir, runName + ".dot").toString();
    var pathGoal = Paths.get(outputDir, runName + ".gff").toString();

    var dotExporter = new DotExporter(res, pathDot, runName);
    dotExporter.setVertexAttributeProvider(v ->
        Map.of("hash", DefaultAttribute.createAttribute(v.hashCode()))
    );
    dotExporter.export();

    var goalExporter = new GoalExporter(res, pathGoal, runName);
    goalExporter.export();

    printAllPaths(res);

    System.out.println("// done");
  }

  /**
   * Generate all paths. See {@link il.ac.bgu.cs.bp.statespacemapper.jgrapht.AllDirectedPaths} for all the possible algorithm configurations.
   */
  public static void printAllPaths(MapperResult res) {
    System.out.println("// Generated paths:");

    var allDirectedPathsAlgorithm = res.createAllDirectedPathsBuilder()
        .setSimplePathsOnly(true)
        .setIncludeReturningEdgesInSimplePaths(true)
        .setLongestPathsOnly(false)
        .build();
    var graphPaths = allDirectedPathsAlgorithm.getAllPaths();
    var eventPaths = MapperResult.GraphPaths2BEventPaths(graphPaths)
        .stream()
        .map(l -> l.stream()
            .map(BEvent::toString)
            .map(s -> s.replaceAll("\\[BEvent name:([^]]+)\\]", "$1"))
            .collect(Collectors.joining(", ")))
        .distinct()
        .sorted()
        .collect(Collectors.joining("\n"));
    System.out.println(eventPaths);
  }
}
