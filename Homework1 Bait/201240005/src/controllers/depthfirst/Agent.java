package controllers.depthfirst;

import controllers.sampleMCTS.SingleMCTSPlayer;
import core.game.Observation;
import core.game.StateObservation;
import ontology.Types;
import tools.ElapsedCpuTimer;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Agent extends controllers.sampleRandom.Agent{

    protected List<Types.ACTIONS> searchedActionList;
    private SingleDFSPlayer dfsPlayer;

    /**
     * Public constructor with state observation and time due.
     * @param so state observation of the current game.
     * @param elapsedTimer Timer for the controller creation.
     */
    public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer){
        //Inits the agent extending sampleRandom.Agent.
        super(so,elapsedTimer);
        searchedActionList = null;

        //Create the player.
        dfsPlayer= new SingleDFSPlayer();
    }

    /**
     * Picks an action. This function is called every game step to request an
     * action from the player.
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

        /*printDebug(npcPositions,"npc");
        printDebug(fixedPositions,"fix");
        printDebug(movingPositions,"mov");
        printDebug(resourcesPositions,"res");
        printDebug(portalPositions,"por");
        System.out.println();*/

        Types.ACTIONS action = null;

        //DfsPlayer searches all actions at the first time.
        if (searchedActionList == null) {
            StateObservation stCopy = stateObs.copy();
            searchedActionList = dfsPlayer.DFS(stCopy);
        }

        //Choose action in turn from searched actions list.
        if (searchedActionList.size()>0) {
            action=searchedActionList.remove(0);
        }
        return action;
    }
}

