package plan;

import communicationclient.Node;

/**
 * Created by arhjo on 22/05/2017.
 */
public class Conflict {
    private int time;
    public enum type {boxbox, agentbox, agentagent, agentInTheWay}
    private type type;
    private Node otherAgentNode;
    private Node thisAgentNode;
    private int thisAgentId;
    private int otherAgentId;

    public Conflict(int time, type t,Node otherAgentNode,Node thisAgentNode){
        this.time = time;
        this.type = t;
        this.otherAgentNode = otherAgentNode;
        this.thisAgentNode = thisAgentNode;

    }

    public int getTime() {
        return time;
    }

    public Conflict.type getType() {
        return type;
    }

    public Node getOtherAgentNode() {
        return otherAgentNode;
    }

    public Node getThisAgentNode() {
        return thisAgentNode;
    }

    public int getThisAgentId() {
        return thisAgentId;
    }

    public int getOtherAgentId() {
        return otherAgentId;
    }
}
