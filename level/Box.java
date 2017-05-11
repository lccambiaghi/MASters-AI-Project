package level;

import communicationclient.Agent;
import heuristic.HeuristicHelper;

public class Box {
    private int col;
    private int row;
    private char boxChar;
    private Color boxColor;
    private CharCell Destination;
    private Agent assignedAgent;
    // if SA level, color is default to blue
    public Box(int col, int row, char boxChar, Color boxColor) {
        this.col = col;
        this.row = row;
        this.boxChar = boxChar;
        this.boxColor = boxColor;
    }

    public char getBoxChar() {
        return this.boxChar;
    }

    public Color getBoxColor() {
        return this.boxColor;
    }


    public void setColor(Color color) {
        this.boxColor= color;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public void setDestination(CharCell charCell){
        int newDistance = HeuristicHelper.manhattanDistance(this.row, 
                                                            this.col, 
                                                            charCell.getRow(),
                                                            charCell.getCol());
        int oldDistance = Integer.MAX_VALUE;

        if(this.Destination != null){
            oldDistance = HeuristicHelper.manhattanDistance(this.row, 
                                                            this.col, 
                                                            this.Destination.getRow(),
                                                            this.Destination.getCol());
        }

        if (newDistance < oldDistance) this.Destination = charCell;
    }

    public Agent getAssignedAgent() {
        return assignedAgent;
    }

    public void setAssignedAgent(Agent assignedAgent) {
        int newDistance = HeuristicHelper.manhattanDistance(this.row,
                this.col,
                assignedAgent.getAgentRow(),
                assignedAgent.getAgentCol());
        int oldDistance = Integer.MAX_VALUE;

        if(this.assignedAgent != null){
            oldDistance = HeuristicHelper.manhattanDistance(this.row,
                    this.col,
                    this.assignedAgent.getAgentRow(),
                    this.assignedAgent.getAgentCol());
        }

        if (newDistance < oldDistance) this.assignedAgent = assignedAgent;
    }

    public CharCell getDestination() {
        return this.Destination;
    }

    public int getCol() {
        return this.col;
    }

    public int getRow() {
        return this.row;
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
