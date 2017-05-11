package goal;

import communicationclient.Agent;
import communicationclient.Node;
import level.Box;
import level.Cell;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by Salik on 10/05/2017.
 */
public class SubGoalMoveOutTheWay extends GoalMoveOutTheWay {
    ArrayList<Cell> requestedCells = new ArrayList<>();
    public SubGoalMoveOutTheWay(LinkedList<Node> requestedCells) {
        super(requestedCells);
        for (Node n: requestedCells) {
            this.requestedCells.add(new Cell(n.agentRow,n.agentCol));
        }
    }
    @Override
    public boolean isGoalSatisfied(Node node) {
        //agent not in path
        for (Cell c:this.requestedCells) {
            if (c.getRow()==node.agentRow && c.getCol() == node.agentCol) return false;
        }
        return true;
    }

    @Override
    public Integer calculateHeuristic(Node n){
        //TODO Create Heuristics for optimal "parkingspot" maybe use graph :-)
        Integer h=0;//BFS
        return h;
    }
    @Override
    public String toString() {
        return "SubGoalMoveOutTheWay";
    }
}
