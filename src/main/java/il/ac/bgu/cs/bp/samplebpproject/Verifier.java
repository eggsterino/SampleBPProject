package il.ac.bgu.cs.bp.samplebpproject;

import il.ac.bgu.cs.bp.bpjs.analysis.BProgramSnapshotVisitedStateStore;
import il.ac.bgu.cs.bp.bpjs.analysis.DfsBProgramVerifier;
import il.ac.bgu.cs.bp.bpjs.analysis.ExecutionTraceInspections;
import il.ac.bgu.cs.bp.bpjs.analysis.listeners.PrintDfsVerifierListener;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.ResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.PrioritizedBSyncEventSelectionStrategy;

import java.text.MessageFormat;

public class Verifier {
  public static void main(String[] args) {
    // This will load the program file  <Project>/src/main/resources/HelloBPjsWorld.js
    final BProgram bprog = new ResourceBProgram("DiningPhilosophers.js");
    var ess = new PrioritizedBSyncEventSelectionStrategy();
    ess.setDefaultPriority(0);
    bprog.setEventSelectionStrategy(ess);
    DfsBProgramVerifier vfr = new DfsBProgramVerifier();
    vfr.setVisitedStateStore(new BProgramSnapshotVisitedStateStore());
    vfr.setMaxTraceLength(4000);
    vfr.setProgressListener(new PrintDfsVerifierListener());
    vfr.setIterationCountGap(500);
    vfr.setDebugMode(false);
    vfr.addInspection(ExecutionTraceInspections.DEADLOCKS);
    try {
      var res = vfr.verify(bprog);
      System.out.println(MessageFormat.format(
          "States = {0}\n" +
              "Edges = {1}\n" +
              "Time = {2}",
          res.getScannedStatesCount(), res.getScannedEdgesCount(), res.getTimeMillies()));
      if (res.isViolationFound()) {
        System.out.println(MessageFormat.format("Found violation: {0}", res.getViolation().get()));
        System.out.println(res.getViolation().get().decsribe());

        System.out.println("trace: "+res.getViolation().get().getCounterExampleTrace().getNodes());
      }else
        System.out.println("No violation found");
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }
}
