package goal;

import communicationclient.Node;
//import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.LinkedList;

public abstract class Goal {

    protected LinkedList<Goal> subgoals;

    public abstract boolean isGoalSatisfied(Node node);

    abstract void refine();

    public LinkedList<Goal> getSubgoals() {
        return subgoals;
    }

    // subgoals must override this method
    public Integer calculateHeuristic(Node n) {
        //throw new NotImplementedException();
        return 0;
    }
}
