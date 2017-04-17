package goal;

import communicationclient.Node;

/**
 * Created by lucacambiaghi on 17/04/2017.
 */
public class SubGoalWait extends Goal{

    @Override
    public boolean isGoalSatisfied(Node node) {
        return false;
    }

    @Override
    void refine() {

    }
}
