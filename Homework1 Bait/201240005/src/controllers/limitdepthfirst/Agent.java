package controllers.limitdepthfirst;

import core.game.Observation;
import core.game.StateObservation;
import ontology.Types;
import tools.ElapsedCpuTimer;

import java.util.ArrayList;

public class Agent extends controllers.sampleRandom.Agent{

    private SingleLDFPlayer ldfPlayer;

    /**
     * Public constructor with state observation and time due.
     * @param so state observation of the current game.
     * @param elapsedTimer Timer for the controller creation.
     */
    public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer){
        super(so,elapsedTimer);
        //Create the player.
        ldfPlayer= new SingleLDFPlayer();
    }

    /**
     * Picks an action. This function is called every game step to request an action from the player.
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */
    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {

        ArrayList<Observation>[] npcPositions = stateObs.getNPCPositions();
        ArrayList<Observation>[] fixedPositions = stateObs.getImmovablePositions();
        ArrayList<Observation>[] movingPositions = stateObs.getMovablePositions();
        ArrayList<Observation>[] resourcesPositions = stateObs.getResourcesPositions();
        ArrayList<Observation>[] portalPositions = stateObs.getPortalsPositions();
        grid = stateObs.getObservationGrid();

        /*for(ArrayList<Observation> i :fixedPositions){
            System.out.println("fixedPositions:"+i.get(0).itype);
        }
        for(ArrayList<Observation> i :movingPositions){
            System.out.println("movingPositions:"+i.get(0).itype);
        }*/

        printDebug(npcPositions,"npc");
        printDebug(fixedPositions,"fix");
        printDebug(movingPositions,"mov");
        printDebug(resourcesPositions,"res");
        printDebug(portalPositions,"por");
        System.out.println();

        Types.ACTIONS action = null;
        StateObservation stCopy = stateObs.copy();

        double avgTimeTaken = 0;
        double acumTimeTaken = 0;
        long remaining = elapsedTimer.remainingTimeMillis();
        int numIters = 0;

        int remainingLimit = 10;
        while(remaining > 2*avgTimeTaken && remaining > remainingLimit)
        {
            ElapsedCpuTimer elapsedTimerIteration = new ElapsedCpuTimer();

            DLSResult searchResult=ldfPlayer.DLS(stCopy,numIters);
            action = searchResult.action;
            if (!searchResult.cutoff)
                return action;

            numIters++;
            acumTimeTaken += (elapsedTimerIteration.elapsedMillis()) ;
            remaining = elapsedTimer.remainingTimeMillis();
            //System.out.println(elapsedTimerIteration.elapsedMillis() + " --> " + acumTimeTaken + " (" + remaining + ")");
            avgTimeTaken  = acumTimeTaken/numIters;
        }
        //System.out.println("-- " + numIters + " -- ( " + avgTimeTaken + ")");
        return action;
    }

    public static class DLSResult {
        boolean cutoff;
        Types.ACTIONS action;

        public DLSResult(boolean cutoff, Types.ACTIONS action){
            this.cutoff=cutoff;
            this.action=action;
        }
    }
}
