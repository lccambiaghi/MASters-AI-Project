package level;

import heuristic.HeuristicHelper;

/**
 * Created by salik on 31-03-2017.
 */
public class Goal {
    private int col;
    private int row;
    private GoalType goalType;
    private char goalChar;
    private Box goalBox;

    public Goal(int col, int row, GoalType goalType){
        this.col = col;
        this.row = row;
        this.goalType = goalType;
    }

    public Goal(int col, int row, char goalChar){
        this.col = col;
        this.row = row;
        this.goalType = GoalType.BoxToGoal;
        this.goalChar = goalChar;
    }

    public Box getGoalBox() {
        return this.goalBox;
    }

    public void setGoalBox(Box goalBox) {
        int newDistance = HeuristicHelper.manhattanDistance(goalBox.getRow(), 
                                                            goalBox.getCol(), 
                                                            this.row, 
                                                            this.col);
        int oldDistance = Integer.MAX_VALUE;
        
        if(this.goalBox!=null){
            oldDistance = HeuristicHelper.manhattanDistance(this.goalBox.getRow(), 
                                                            this.goalBox.getCol(), 
                                                            this.row, 
                                                            this.col);
        }
        
        if (newDistance < oldDistance) this.goalBox = goalBox;
    }

    public char getGoalChar() {
        return goalChar;
    }

    public GoalType getGoalType() {
        return goalType;
    }

    public int getCol() {
        return col;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Goal goal = (Goal) o;

        if (col != goal.col) return false;
        return row == goal.row;
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
