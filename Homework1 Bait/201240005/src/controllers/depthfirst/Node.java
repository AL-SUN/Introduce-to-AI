package controllers.depthfirst;

import core.game.StateObservation;
import ontology.Types;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Node {

    public StateObservation state;
    public Node parent;
    public Types.ACTIONS action;
    public int depth;

    /**
     * Create a Node in the tree.
     * Property includes state, parent node, action and depth,
     * cost unmentioned for it equals to depth in this case.
     * @param so state observation of current game.
     * @param parent parent node of current one.
     * @param action action taken in current state.
     */
    public Node(StateObservation so, Node parent, Types.ACTIONS action) {
        this.state = so;
        this.parent = parent;
        this.action = action;
        if(parent != null)
            this.depth = parent.depth+1;
        else
            this.depth = 0;
    }

    /**
     * Expand node to get all its children node.
     * @return the list of all its children node.
     */
    public ArrayList<Node> expand() {
        ArrayList<Node> successors = new ArrayList<>(4);
        ArrayList<Types.ACTIONS> actions = this.state.getAvailableActions();

        Collections.shuffle(actions);//随机打乱action,避免每次游戏的行动一样

        for (Types.ACTIONS step : actions) {
            StateObservation nextState = this.state.copy();
            nextState.advance(step);
            successors.add(new Node(nextState, this, step));
        }
        return successors;
    }

    /**
     * Get action path to reach current state observation.
     * @return ordered actions to execute.
     */
    public LinkedList<Types.ACTIONS> getPath() {
        Node n = this;
        LinkedList<Types.ACTIONS> path= new LinkedList<>();
        while(n.parent != null)
        {
            path.addFirst(n.action);
            n = n.parent;
        }
        return path;
    }

    public Types.ACTIONS getNext() {
        Node n = this;
        Node next=n;
        while(n.parent != null)
        {
            next=n;
            n = n.parent;
        }
        return next.action;
    }

    //Indicates if a node is in the given node list.
    public boolean in(List<Node> list){
        for (Node i : list) {
            if (this.state.equalPosition(i.state))
                return true;
        }
        return false;
    }

    //Indicates if node is the goal node.
    public boolean isGoal() {
        return this.state.getGameWinner() == Types.WINNER.PLAYER_WINS;
    }

    //Indicates if node loses the game.
    public boolean isLost() {
        return this.state.getGameWinner() == Types.WINNER.PLAYER_LOSES;
    }
}
