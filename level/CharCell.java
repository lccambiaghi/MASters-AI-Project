package level;

import heuristic.HeuristicHelper;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by salik on 31-03-2017.
 */
public class CharCell {
    private int col;
    private int row;
    //private GoalType goalType;
    private char letter;
    private Box assignedBox;

//    public CharCell(int col, int row, GoalType goalType){
//        this.col = col;
//        this.row = row;
//        this.goalType = goalType;
//    }

    public CharCell(int col, int row, char letter){
        this.col = col;
        this.row = row;
        //this.goalType = GoalType.PushBox;
        this.letter = letter;
    }

    public Box getClosestBox(HashSet<Box> boxes) {

        int oldDistance = Integer.MAX_VALUE;

        if(this.assignedBox !=null){
            oldDistance = HeuristicHelper.manhattanDistance(this.assignedBox.getRow(),
                    this.assignedBox.getCol(),
                    this.row,
                    this.col);
        }

        Box closestBox = null;

        for (Box b: boxes)
            if (b.getDestination() == null){ // consider only boxes not already assigned
                int newDistance = HeuristicHelper.manhattanDistance(b.getRow(), b.getCol(), this.row, this.col);

                if (newDistance < oldDistance)
                   closestBox = b;
            }

        return closestBox;

    }

    public void setAssignedBox(Box assignedBox) {
        this.assignedBox = assignedBox;
    }

    public char getLetter() {
        return letter;
    }

    public int getCol() {
        return col;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CharCell charCell = (CharCell) o;

        if (col != charCell.col) return false;
        return row == charCell.row;
    }

    @Override
    public int hashCode() {
        int result = col;
        result = 31 * result + row;
        return result;
    }

    public int getRow() {
        return row;
    }

}
