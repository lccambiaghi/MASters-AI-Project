package goal;

import communicationclient.Agent;
import communicationclient.Node;

import java.util.LinkedList;

/**
 * Created by salik on 10-05-2017.
 */
public class GoalMoveOutTheWay extends Goal {

    protected LinkedList<Node> requestedCells;
    private Goal toCell;
    public GoalMoveOutTheWay(LinkedList<Node> requestedCells){
        this.requestedCells = requestedCells;
    }
    @Override
    public boolean isGoalSatisfied(Node node) {
        return toCell.isGoalSatisfied(node);
    }

    @Override
    public void refine() {
        if (!isRefined){
            toCell = new SubGoalMoveOutTheWay(this.requestedCells);
            subgoals.add(toCell);
        }
        isRefined=true;

    }

    @Override
    public Agent getAgent() {
        return agent;
    }

    @Override
    public void setAgent(Agent agent) {
        super.agent = agent;
    }

    @Override
    public String toString() {
        return null;
    }
}
