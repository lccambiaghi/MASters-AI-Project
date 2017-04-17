package plan;

import communicationclient.Command;

/**
 * Created by salik on 07-04-2017.
 */
public class AgentPoint {
    private int agentRow;
    private int agentCol;
    private int agentId;
    private Command action;
    public AgentPoint(int agentRow, int agentCol, int agentId, Command action){
        this.agentRow=agentRow;
        this.agentCol=agentCol;
        this.agentId = agentId;
        this.action = action;
    }

    public int getAgentRow() {
        return agentRow;
    }

    public int getAgentCol() {
        return agentCol;
    }

    public int getAgentId() {
        return agentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AgentPoint that = (AgentPoint) o;

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
