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

    public void setCol(int col) {
        this.col = col;
    }

    public void setRow(int row) {
        this.row = row;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Box box = (Box) o;

        if (col != box.col) return false;
        if (row != box.row) return false;
        if (boxChar != box.boxChar) return false;
        return boxColor == box.boxColor;
    }

    @Override
    public int hashCode() {
        int result = col;
        result = 31 * result + row;
        result = 31 * result + (int) boxChar;
        result = 31 * result + (boxColor != null ? boxColor.hashCode() : 0);
        return result;
    }
}
