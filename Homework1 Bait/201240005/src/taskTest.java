import controllers.depthfirst.Node;
import core.ArcadeMachine;
import core.competition.CompetitionParameters;
import tools.Vector2d;
import static controllers.limitdepthfirst.SingleLDFPlayer.ManhattanDis;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;

import static controllers.limitdepthfirst.SingleLDFPlayer.ManhattanDis;

public class taskTest {
    public static void main(String[] args) {

        String depthfirstController = "controllers.Astar.Agent";

        boolean visuals = true; // set to false if you don't want to see the game
        int seed = new Random().nextInt(); // seed for random


        /****** Task 1 ******/
        CompetitionParameters.ACTION_TIME = 100; // set to the time that allow you to do the depth first search
        ArcadeMachine.runOneGame("examples/gridphysics/bait.txt", "examples/gridphysics/bait_lvl2.txt", true, depthfirstController, null, seed, false);

    }
}
