package goal;

import communicationclient.Node;
import heuristic.HeuristicHelper;
import level.Box;
import level.Cell;
import level.CharCell;

/**
 * Created by arhjo on 09/05/2017.
 */
public class SubGoalMoveBoxOutTheWay extends GoalFreeAgent {
    public SubGoalMoveBoxOutTheWay(Box box, Cell destination) {
        super(box, destination);
    }
    @Override
    public boolean isGoalSatisfied(Node node) {
        int goalCharRow = destination.getRow();
        int goalCharCol = destination.getCol();
        Box[][] boxes = node.getBoxes();

        Box box = boxes[goalCharRow][goalCharCol];
        if (box!=null){
            return this.box == box;
        }
        return false;
    }

    @Override
    public Integer calculateHeuristic(Node n){
        //TODO change box to node boxes
        int boxRow = box.getRow();
        int boxCol = box.getCol();
        int goalCharRow = destination.getRow();
        int goalCharCol = destination.getCol();

        Integer h;
        h = HeuristicHelper.manhattanDistance(boxRow, boxCol, goalCharRow, goalCharCol);
        h += HeuristicHelper.goalCount(n);

        return h;
    }
}
