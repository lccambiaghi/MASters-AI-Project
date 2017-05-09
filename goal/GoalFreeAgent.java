package goal;

import communicationclient.Agent;
import communicationclient.Node;
import level.Box;
import level.Cell;
import level.CharCell;

import java.util.LinkedList;

/**
 * Created by lucacambiaghi on 17/04/2017.
 */
public class GoalFreeAgent extends Goal{
    protected Box box;
    protected Cell destination;

    private Goal toBox;
    private Goal toCell;

    public GoalFreeAgent(Box box, Cell destination){
        this.box=box;
        this.destination=destination;
        subgoals = new LinkedList<>();
    }
    @Override
    public boolean isGoalSatisfied(Node node) {
        return toCell.isGoalSatisfied(node);
    }

    @Override
    public void refine() {
        if (!isRefined){
            toBox = new SubGoalMoveToBox(this.box);
            toCell = new SubGoalMoveBoxOutTheWay(this.box, this.destination);
            subgoals.add(toBox);
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

    public Box getBox() {
        return box;
    }
}
