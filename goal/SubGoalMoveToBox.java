package goal;

import communicationclient.Node;
import heuristic.HeuristicHelper;
import level.Box;

/**
 * Created by lucacambiaghi on 17/04/2017.
 */
public class SubGoalMoveToBox extends GoalBoxToCell {

    //inherits box and destination

    public SubGoalMoveToBox(Box box) {
        this.box = box;
    }

    @Override
    public boolean isGoalSatisfied(Node node) {

        int agentRow = node.getAgentRow();
        int agentCol = node.getAgentCol();

        int boxRow = box.getRow();
        int boxCol = box.getCol();

        if((Math.abs(agentRow-boxRow) == 1 && Math.abs(agentCol-boxCol) == 0) ||
                (Math.abs(agentCol-boxCol) == 1 && Math.abs(agentRow-boxRow) == 0)) return true;
        return false;
    }

    @Override
    public Integer calculateHeuristic(Node n){

        int agentRow = n.getAgentRow();
        int agentCol = n.getAgentCol();

        int boxRow = box.getRow();
        int boxCol = box.getCol();

        Integer h;
        h = HeuristicHelper.manhattanDistance(agentRow,agentCol, boxRow, boxCol);
//        h += HeuristicHelper.goalCount(n);
        return h;
    }

}
