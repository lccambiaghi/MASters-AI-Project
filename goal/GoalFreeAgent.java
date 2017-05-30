package goal;

import communicationclient.Agent;
import communicationclient.Node;
import level.Box;
import level.Cell;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by lucacambiaghi on 17/04/2017.
 */
public class GoalFreeAgent extends Goal{
    protected Box box;
    protected LinkedList<Node> requestedCells;
    protected Agent agentToFree;

    private Goal toBox;
    private Goal toCell;

    public GoalFreeAgent(Box box, LinkedList<Node> requestedCells, Agent agentToFree){
        this.box=box;
        this.requestedCells = requestedCells;
        this.agentToFree = agentToFree;
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
            toCell = new SubGoalMoveBoxOutTheWay(this.box, this.requestedCells, agentToFree);
            subgoals.add(toBox);
            subgoals.add(toCell);
            toBox.setPriority(this.priority);
            toCell.setPriority(this.priority);
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
        return "GoalFreeAgent "+ agentToFree.getId();
    }

    public Box getBox() {
        return box;
    }

    public Agent getAgentToFree() {
        return agentToFree;
    }
}
