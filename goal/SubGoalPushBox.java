package goal;

import communicationclient.Command;
import communicationclient.Node;
import heuristic.Heuristic;
import heuristic.HeuristicHelper;
import level.Box;
import level.Cell;
import level.CharCell;

/**
 * Created by lucacambiaghi on 17/04/2017.
 */
public class SubGoalPushBox extends GoalBoxToCell {

    // inherits box and destination

    public SubGoalPushBox(Box box, Cell destination){
        this.box = box;
        this.destination = destination;
    }

    @Override
    public boolean isGoalSatisfied(Node node) {
        int goalCharRow = destination.getRow();
        int goalCharCol = destination.getCol();
        CharCell goalCharCell = (CharCell)destination;
        char goalChar = goalCharCell.getLetter();

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
        //TODO change box to node boxes
        //TODO Find box in node.
        int h = Integer.MAX_VALUE;
        Command action = n.action;
        int goalCharRow = destination.getRow();
        int goalCharCol = destination.getCol();
        int newBoxRow = 0;
        int newBoxCol = 0;
        //Only initialNode has not action
        if(action==null){
            newBoxRow = n.agentRow;
            newBoxCol = n.agentCol;
            h = HeuristicHelper.manhattanDistance(newBoxRow, newBoxCol, goalCharRow, goalCharCol);
            h += HeuristicHelper.goalCount(n);
            return h;
        }
        switch (action.actionType){
            case Move:
                    h=1000;
                break;
            case Pull:
                newBoxRow = n.boxMovedRow;
                newBoxCol = n.boxMovedCol;
                Box pulledBox = n.boxMoved;
                if(pulledBox.equals(this.box)){
                    h = HeuristicHelper.manhattanDistance(newBoxRow, newBoxCol, goalCharRow, goalCharCol);
                    h += HeuristicHelper.goalCount(n);
                    h += HeuristicHelper.keepRight(n);

                }else{
                    h = 10;
                    h += HeuristicHelper.goalCount(n);
                    h += HeuristicHelper.keepRight(n);
                }
                break;
            case Push:
                newBoxRow = n.boxMovedRow;
                newBoxCol = n.boxMovedCol;
                Box pushedBox = n.boxMoved;
                if(pushedBox.equals(this.box)){
                    h = HeuristicHelper.manhattanDistance(newBoxRow, newBoxCol, goalCharRow, goalCharCol);
                    h += HeuristicHelper.goalCount(n);
                    h += HeuristicHelper.keepRight(n);
                }
                else{
                    h=10;//Punish wrong box
                    h += HeuristicHelper.goalCount(n);
                    h += HeuristicHelper.keepRight(n);
                }

                break;
        }
        return h;
    }
    @Override
    public String toString() {
        return "SubGoalPushBox: "+ this.box.getBoxChar();
    }
}
