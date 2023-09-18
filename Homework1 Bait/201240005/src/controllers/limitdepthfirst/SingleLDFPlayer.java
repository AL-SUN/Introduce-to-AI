package controllers.limitdepthfirst;

import controllers.depthfirst.Node;
import core.game.Observation;
import core.game.StateObservation;
import ontology.Types;
import tools.Vector2d;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Stack;

public class SingleLDFPlayer{

    //Create LDF player.
    public SingleLDFPlayer(){   }

    /**
     * Run LDF to search optimal action at limited depth.
     * @param a_gameState current state of the game.
     * @param limit maximum depth of the DFS.
     * @return An action to execute.
     */
    public Agent.DLSResult DLS(StateObservation a_gameState, int limit) {
        Stack<Node> fringe = new Stack<>();
        LinkedList<Node> closed = new LinkedList<>();
        Node optNode= null;

        //Create root of the tree.
        Node root = new Node(a_gameState, null, null);
        fringe.push(root);

        while (!fringe.empty()) {
            Node cur = fringe.pop();
            closed.add(cur);

            if (cur.isGoal()) {
                return new Agent.DLSResult(false, cur.getNext());
            }
            else if(cur.depth==limit){
//                System.out.println("cutoff");
                if(optNode==null|| heuristic(optNode.state)> heuristic(cur.state))
                    optNode=cur;
            }
            else {
                for (Node child : cur.expand()) {
                    if (!child.in(closed))
                        fringe.push(child);
                }
            }
        }

        assert optNode != null;
        return new Agent.DLSResult(true, optNode.getNext());
    }

    /** 拍脑袋函数写太差，会出现无法动弹的结果
     * Heuristic function. This function evaluates the state.
     * @param stateObs Observation of the current state.
     * @return a double stands cost.
     */
    public int heuristic(StateObservation stateObs){
        if(stateObs.getGameWinner()== Types.WINNER.PLAYER_LOSES) //Avatar消失,无法获得位置
            return Integer.MAX_VALUE;

        int cost;

        ArrayList<Observation>[] fixedPositions = stateObs.getImmovablePositions();
        ArrayList<Observation>[] movingPositions = stateObs.getMovablePositions();
        Vector2d goalpos = fixedPositions[1].get(0).position; //目标的坐标（第一关）
        Vector2d Avatarpos =stateObs.getAvatarPosition();//Avatar的坐标

        int Avataritype=stateObs.getAvatarType();//Avatar的状态 without key-1 with key-4
        if (Avataritype==1){  //not get key
            Vector2d keypos = movingPositions[0].get(0).position; //钥匙的坐标（第一关）
            cost=ManhattanDis(goalpos,keypos)+ManhattanDis(keypos,Avatarpos);//Avatar到钥匙+钥匙到目标

            /*箱子挡住钥匙
            for(Observation box :movingPositions[1]){        //箱子的坐标（若存在）（第一关）
                 if(box.position==keypos){
                    cost+=1;
                    break;
                 }
            }*/
        }
        else //get key
            cost=ManhattanDis(goalpos,Avatarpos);//Avatar到目标

        return cost;
    }

    /**
     * Calculate Manhattan Distance.
     * @param target position of the target.
     * @param start position of the starting point.
     */
    public static int ManhattanDis(Vector2d target,Vector2d start) {
        return (int)((Math.abs(target.x- start.x) + Math.abs(target.y- start.y))/50);
    }
}
