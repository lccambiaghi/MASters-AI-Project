package goal;

import communicationclient.Agent;
import communicationclient.Node;
import level.Box;
import level.Cell;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * Created by Salik on 10/05/2017.
 */
public class SubGoalMoveOutTheWay extends GoalMoveOutTheWay {
    HashSet<Cell> requestedCells = new HashSet<>();
    public SubGoalMoveOutTheWay(LinkedList<Node> requestedCells) {
        super(requestedCells);
        // Other agents initial position
        Node initialNode = requestedCells.get(0).parent;
        this.requestedCells.add(new Cell(initialNode.agentRow,initialNode.agentCol));
        boolean firstBox = true;
        for (Node n: requestedCells) {
            this.requestedCells.add(new Cell(n.agentRow,n.agentCol));
            if(n.boxMoved != null){
                // Add information of boxes to the requested cells
                this.requestedCells.add(new Cell(n.boxMovedRow,n.boxMovedCol));
                this.requestedCells.add(new Cell(n.oldBoxMovedRow,n.oldBoxMovedCol));

            }
        }
    }
    @Override
    public boolean isGoalSatisfied(Node node) {
        //agent not in path
        //if requested cells contains the cell in which the agent is standing then the goal is not satisfied.
        return !this.requestedCells.contains(new Cell(node.agentRow, node.agentCol));

    }

    @Override
    public Integer calculateHeuristic(Node n){
        //TODO Create Heuristics for optimal "parkingspot" maybe use graph :-)
        // TODO Make sure that the agent does not pull or push boxes, but only moves out the way
        Integer h=0;//BFS
        return h;
    }
    @Override
    public String toString() {
        return "SubGoalMoveOutTheWay";
    }
}
