package goal;

import communicationclient.Node;
import heuristic.HeuristicHelper;
import level.Box;
import level.CharCell;
import level.Level;

/**
 * Created by lucacambiaghi on 17/04/2017.
 */
public class SubGoalPushBox extends GoalBoxToChar{

    // inherits box and destination

    public SubGoalPushBox(Box box, CharCell destination){
        this.box = box;
        this.destination = destination;
    }

    @Override
    public boolean isGoalSatisfied(Node node) {
        int goalCharRow = destination.getRow();
        int goalCharCol = destination.getCol();
        char goalChar = destination.getLetter();

        Box[][] boxes = node.getBoxes();

        Box box = boxes[goalCharRow][goalCharCol];
        if (box!=null){
            char b = Character.toLowerCase(box.getBoxChar());
            if (b == goalChar) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Integer calculateHeuristic(Node n){
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
