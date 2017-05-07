package goal;

import communicationclient.Agent;
import communicationclient.Node;
import level.Box;
import level.CharCell;

import java.util.LinkedList;

/**
 * Created by lucacambiaghi on 17/04/2017.
 */
public class GoalBoxToChar extends Goal {

    // inherits | LinkedList<Goal> subgoals | from Goal

    protected Box box;
    protected CharCell destination;

    private Goal toBox;
    private Goal toChar;

    // empty constructor for subclasses
    public GoalBoxToChar(){}

    public GoalBoxToChar(Box box, CharCell destination){
        this.box = box;
        this.destination = destination;

        subgoals = new LinkedList<>();

    }

    @Override
    public void refine(){
        toBox = new SubGoalMoveToBox(this.box);
        toChar = new SubGoalPushBox(this.box, this.destination);
        subgoals.add(toBox);
        subgoals.add(toChar);
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
    public boolean isGoalSatisfied(Node node) {
        return toChar.isGoalSatisfied(node);
    }

    public Box getBox() {
        return box;
    }

}