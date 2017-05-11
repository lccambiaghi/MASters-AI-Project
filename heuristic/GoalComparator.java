package heuristic;

import goal.Goal;

import java.util.Comparator;

/**
 * Created by lucacambiaghi on 03/05/2017.
 */
public class GoalComparator implements Comparator<Goal> {

    @Override
    public int compare(Goal o1, Goal o2) {
        return o1.getPriority() - o2.getPriority();
    }
}
