package level;

import heuristic.HeuristicHelper;

/**
 * Created by salik on 31-03-2017.
 */
public class Box {
    private int col;
    private int row;
    private char boxChar;
    private Color boxColor;
    private Goal boxGoal;

    public Box(int col, int row, char boxChar, Color boxColor) {
        this.col = col;
        this.row = row;
        this.boxChar = boxChar;
        this.boxColor = boxColor;
    }

    public char getBoxChar() {
        return boxChar;
    }

    public Color getBoxColor() {
        return boxColor;
    }
    public void setBoxGoal(Goal goal){
        int newDistance = HeuristicHelper.manhattanDistance(this.row, this.col, goal.getRow(),goal.getCol());
        int oldDistance = Integer.MAX_VALUE;
        if(this.boxGoal!=null){
            oldDistance = HeuristicHelper.manhattanDistance(this.row, this.col, this.boxGoal.getRow(), this.boxGoal.getCol());
        }
        if (newDistance < oldDistance) this.boxGoal = goal;
    }

    public Goal getBoxGoal() {
        return boxGoal;
    }

    public int getCol() {
        return col;
    }
    public int getRow() {
        return row;
    }

}
