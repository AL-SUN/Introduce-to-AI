package controllers.depthfirst;

import core.game.StateObservation;
import ontology.Types;

import java.util.*;

public class SingleDFSPlayer {

    //Creates DFS player with an empty action list.
    public SingleDFSPlayer(){   }

    /**
     * Runs DFS to search all actions at a time.
     * @param a_gameState current state of the game.
     * @return the action list to execute in turn in the game.
     */
    public List<Types.ACTIONS> DFS(StateObservation a_gameState) {
        Stack<Node> fringe = new Stack<>();
        LinkedList<Node> closed = new LinkedList<>();

        //Create root of the tree.
        Node root = new Node(a_gameState, null, null);
        fringe.push(root);

        while (!fringe.empty()) {
            Node cur = fringe.pop();
            closed.add(cur);

            if (cur.isGoal()) {
//                System.out.println("FOUND");
                return cur.getPath();
            }

            for (Node child : cur.expand())
            {
                if(!child.in(closed))
                    fringe.push(child);
            }
        }
        return new ArrayList<>();
    }
}
