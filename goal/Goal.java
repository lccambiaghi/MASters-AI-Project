package goal;

import communicationclient.Agent;
import communicationclient.Node;
//import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.LinkedList;

/**
 * Goal is composed by a LinkedList of Goals
 * Goal is assigned to an agent
 * Goal gets created and then refined in subgoals.
 *
 * Every different Goal (e.g. GoalBoxToCell) extends this class,
 * inherits the protected attributes and overrides refine() and isGoalSatisfied()
 */
public abstract class Goal {

    protected LinkedList<Goal> subgoals;
    protected Agent agent;
    boolean isRefined;

    protected int priority;

    public abstract boolean isGoalSatisfied(Node node);

    public abstract void refine();

    public LinkedList<Goal> getSubgoals() {
        return subgoals;
    }

    // subgoals must override this method
    public Integer calculateHeuristic(Node n) {
        //throw new NotImplementedException();
        return 0;
    }

    public abstract Agent getAgent();
    public abstract void setAgent(Agent agent);
    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public abstract String toString();
}
