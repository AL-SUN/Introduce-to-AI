package controllers.Astar;

import controllers.depthfirst.Node;
import static controllers.limitdepthfirst.SingleLDFPlayer.ManhattanDis;
import core.game.Observation;
import core.game.StateObservation;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class SingleAstarPlayer{

    ElapsedCpuTimer searchTimer;
    //Past states for previous actions
    LinkedList<Node> pastList;

    //Create an A* player without timer and past.
    public SingleAstarPlayer() {
        super();
        searchTimer= null;
        pastList =new LinkedList<>();
    }

    public void setTimer(ElapsedCpuTimer elapsedTimer){
        searchTimer= elapsedTimer;
    }

    /**
     * Run A* to search an optimal action at limited time.
     * @param a_gameState current state of the game.
     * @return An action to execute.
     */
    public Types.ACTIONS aStar(StateObservation a_gameState) {
        ArrayList<Node> fringe = new ArrayList<>();
        LinkedList<Node> closed = new LinkedList<>();

        //Create root of the tree.
        Node root = new Node(a_gameState, null, null);
        fringe.add(root);
        pastList.add(root);        //加入到已经经过的状态列表

        int remainingLimit = 8;
        while (!fringe.isEmpty()) {
            //pick a node by A*
            fringe.sort(Comparator.comparingInt(this::f));
            Node cur = fringe.remove(0);
            closed.add(cur);

            if (cur.isGoal()||searchTimer.remainingTimeMillis()<=remainingLimit){
                //System.out.print(pastList.size()+"thStep search Node:"+count+" Depth:");
                //System.out.println(cur.depth);
                return cur.getNext();
            }
            else {
                for (Node child : cur.expand())
                    //没输，既未前次走过，也未本次搜过
                    if (!child.isLost()&&!child.in(pastList)&&!repeated(closed,child))
                        fringe.add(child);
            }
        }
        //System.out.println("NO FOUND");
        return null;
    }

    /**
     * Indicates if node is repeating one. If state same but depth(cost) less,
     * it isn't repeated and shouldn't be excluded.
     * @param list closed list
     * @param node node for evaluation .
     */
    public boolean repeated(List<Node> list, Node node){
        for (Node i : list) {
            if (node.state.equalPosition(i.state) && node.depth>=i.depth)
                return true;
        }
        return false;
    }

    /**
     * Evaluation function. This function evaluates the node.
     * @param node node for evaluation .
     * @return estimated total cost from node to the goal.
     */
    public int f(Node node){ return node.depth+heuristic(node.state);    }

    /** 拍脑袋函数写太差，会出现无法动弹的结果
     * Heuristic function. This function evaluates the state.
     * @param stateObs Observation of the current state.
     * @return a double stands cost.
     */
    public int heuristic(StateObservation stateObs) {

        if(stateObs.getGameWinner()== Types.WINNER.PLAYER_LOSES) //Avatar消失
            return Integer.MAX_VALUE;
        if(stateObs.getGameWinner()== Types.WINNER.PLAYER_WINS)
            return 0;

        ArrayList<Observation>[] fixedPositions = stateObs.getImmovablePositions();
        ArrayList<Observation>[] movingPositions = stateObs.getMovablePositions();

        Vector2d goalpos =null;

        for (ArrayList<Observation> itemlist:fixedPositions) {
            if (!itemlist.isEmpty() && itemlist.get(0).itype == 7)
                goalpos = itemlist.get(0).position; //目标的坐标
        }

        Vector2d Avatarpos =stateObs.getAvatarPosition();//Avatar的坐标

        int Avataritype=stateObs.getAvatarType();//Avatar的状态
        if (Avataritype==1) //not get key
        {
            Vector2d keypos = movingPositions[0].get(0).position; //钥匙的坐标
            return ManhattanDis(goalpos,keypos)+ManhattanDis(keypos,Avatarpos);
        }
        else //get key
            return ManhattanDis(goalpos,Avatarpos);
    }
}