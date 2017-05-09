package communication;

import communicationclient.Node;
import goal.Goal;
import level.Box;

import java.util.LinkedList;

/**
 * Created by arhjo on 09/05/2017.
 */
public class GoalMessage extends Message {
    Goal goal;
    public GoalMessage(MsgType type, Goal goal, LinkedList<Node> content, char agentID) {
        super(type, content, agentID);
        this.goal = goal;
    }

    public Goal getGoal() {
        return goal;
    }
}
