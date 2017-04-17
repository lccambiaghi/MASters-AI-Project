package goal;

import communicationclient.Node;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.LinkedList;

/**
 * Created by lucacambiaghi on 17/04/2017.
 */
public abstract class Goal {

    protected LinkedList<Goal> subgoals;

    public abstract boolean isGoalSatisfied(Node node);

    abstract void refine();

    public LinkedList<Goal> getSubgoals() {
        return subgoals;
    }

    // subgoals must override this method
    public Integer calculateHeuristic(Node n) {
        throw new NotImplementedException();
    }
}
