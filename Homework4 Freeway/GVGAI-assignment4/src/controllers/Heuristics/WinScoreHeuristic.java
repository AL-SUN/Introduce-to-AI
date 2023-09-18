package controllers.Heuristics;

import core.game.Observation;
import core.game.StateObservation;
import ontology.Types;
import tools.Vector2d;

import java.util.ArrayList;

import static controllers.learningmodel.RLDataExtractor.ManDis;

/**
 * Created with IntelliJ IDEA.
 * User: ssamot
 * Date: 11/02/14
 * Time: 15:44
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class WinScoreHeuristic extends StateHeuristic {

    private static final double HUGE_NEGATIVE = -1000.0;
    private static final double HUGE_POSITIVE =  1000.0;

    double initialNpcCounter = 0;

    public WinScoreHeuristic(StateObservation stateObs) {

    }

    public double evaluateState(StateObservation stateObs) {
        boolean gameOver = stateObs.isGameOver();
        Types.WINNER win = stateObs.getGameWinner();
        double rawScore = stateObs.getGameScore();
        if(gameOver && win == Types.WINNER.PLAYER_LOSES) {
//            System.out.println("lose:HUGE_NEGATIVE");
            return HUGE_NEGATIVE;
        }
        if(gameOver && win == Types.WINNER.PLAYER_WINS){
//            System.out.println("win:HUGE_POSITIVE");
            return HUGE_POSITIVE;
        }

        int hp= stateObs.getAvatarHealthPoints();
        int time= stateObs.getGameTick();
        Vector2d Avatar_p = stateObs.getAvatarPosition();
        Vector2d Portal_p = new Vector2d(15 * 28.0, 28.0);    //目标位置，若目标不存在，假设在第一行中间
        if (stateObs.getPortalsPositions() != null) {
            for (ArrayList<Observation> l : stateObs.getPortalsPositions()) {
                if (l.size() > 0) {
                    Portal_p = l.get(0).position;
                } else
                    Portal_p = Avatar_p;
            }
        }
        double goalDis=ManDis(Portal_p,Avatar_p);

        return rawScore+hp*100-time-goalDis;
//        return rawScore;
    }


}


