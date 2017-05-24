package level;

import heuristic.HeuristicHelper;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by salik on 31-03-2017.
 */
public class CharCell extends Cell{
//    private int col;
//    private int row;
    //private GoalType goalType;
    private char letter;
    private Box assignedBox;
    private int priority = 0;
    private int graphComponentsIfFulfilled = 1;
    private int numWalls = 0;
    private boolean corner = false;
    private boolean deadEnd = false;

//    public CharCell(int col, int row, GoalType goalType){
//        this.col = col;
//        this.row = row;
//        this.goalType = goalType;
//    }

    public int getGraphComponentsIfFulfilled() {
        return graphComponentsIfFulfilled;
    }

    public void setGraphComponentsIfFulfilled(int graphComponentsIfFulfilled) {
        this.graphComponentsIfFulfilled = graphComponentsIfFulfilled;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
    public void calculatePriority(){//TODO include graphComponentsIfFulfilled
        priority = 100;
        priority -= numWalls;
        if(corner) priority--;//if corner give higher priority
        if (deadEnd) priority -=2;//If deadend give higher priority
    }

    public int getNumWalls() {
        return numWalls;
    }

    public void setNumWalls(int numWalls) {
        this.numWalls = numWalls;
    }

    public boolean isCorner() {
        return corner;
    }

    public void setCorner(boolean corner) {
        this.corner = corner;
    }

    public boolean isDeadEnd() {
        return deadEnd;
    }

    public void setDeadEnd(boolean deadEnd) {
        this.deadEnd = deadEnd;
    }

    public CharCell(int col, int row, char letter){
        super(row, col);
//        this.col = col;
//        this.row = row;
        //this.goalType = GoalType.PushBox;
        this.letter = letter;
    }

    public Box getAssignedBox() {
        return assignedBox;
    }

    public Box getClosestBox(HashSet<Box> boxes) {
        if(boxes==null) System.exit(-1);
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

                if (newDistance < oldDistance){
                    oldDistance = newDistance;
                    closestBox = b;
                }
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
