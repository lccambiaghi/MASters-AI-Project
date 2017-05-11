package plan;

import communicationclient.Command;

/**
 * Created by salik on 07-04-2017.
 */
public class Point {
    private int agentRow;
    private int agentCol;
    public Point(int agentRow, int agentCol){
        this.agentRow=agentRow;
        this.agentCol=agentCol;
    }
    public int getAgentRow() {
        return agentRow;
    }

    public int getAgentCol() {
        return agentCol;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Point that = (Point) o;

        if (agentRow != that.agentRow) return false;
        return agentCol == that.agentCol;
    }

    @Override
    public int hashCode() {
        int result = agentRow;
        result = 31 * result + agentCol;
        return result;
    }
}
