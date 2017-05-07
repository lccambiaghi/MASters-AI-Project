package goal;

import communicationclient.Agent;
import communicationclient.Node;

/**
 * Created by lucacambiaghi on 17/04/2017.
 */
public class GoalFreeAgent extends Goal{
    @Override
    public boolean isGoalSatisfied(Node node) {
        return false;
    }

    @Override
    public void refine() {

    }

    @Override
    public Agent getAgent() {
        return agent;
    }

    @Override
    public void setAgent(Agent agent) {
        super.agent = agent;
    }
}
