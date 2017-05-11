package plan;

import communicationclient.Node;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by salik on 07-04-2017.
 * Class to detect conflicts between agents plans
 */
public class ConflictDetector {
    //Map of where agents are at a time in their refineBoxToChar
    private HashMap<Integer,Node> timeMap;

    public ConflictDetector(){
        timeMap = new HashMap<>();
    }

    // return time of first occurring conflict
    public int checkPlan(LinkedList<Node> otherAgentPlan, int solutionStart){//TODO need to check for boxes as well in order to complete MAsimple4
        int conflictPoint = -1;
        for (int i=0; i < otherAgentPlan.size();i++) {
            Node n = otherAgentPlan.get(i);
            if(!timeMap.containsKey(i+solutionStart)){//Check from solutionstart in global plan
                return -1;//No conflict as agentPlan is not as long as otherAgentPlan
            }else{
                Node agentNodeCurrent = timeMap.get(i+solutionStart); // gets the map of agent points at that that time
                AgentPoint otherAgentPoint = new AgentPoint(n.agentRow,n.agentCol, n.agentId, n.action);
                AgentPoint thisAgentPoint = new AgentPoint(agentNodeCurrent.agentRow,agentNodeCurrent.agentCol,agentNodeCurrent.agentId,agentNodeCurrent.action);
                // Has another agent planned to move to the same point?
                if(otherAgentPoint.equals(thisAgentPoint)){
                    conflictPoint = i+solutionStart;
                    return conflictPoint;
                }
                // Was another agent at t-1 in the cell I now want to reach?
                if(i > 0){
                    Node thisAgentNodeBefore = timeMap.get(i+solutionStart-1);
                    AgentPoint thisAgentPointBefore = new AgentPoint(thisAgentNodeBefore.agentRow,thisAgentNodeBefore.agentCol,thisAgentNodeBefore.agentId,thisAgentNodeBefore.action);
                    if(thisAgentPointBefore.equals(otherAgentPoint)){ // if there was an agent in the cell I now want to reach
                        conflictPoint = i+solutionStart;
                        return conflictPoint;

                    }

                }

            }
        }

        return conflictPoint;
    }

    public void addPlan(LinkedList<Node> agentPlan){
        for (int i=0;i < agentPlan.size();i++) {
            Node n = agentPlan.get(i);
            timeMap.put(i,n);
        }
    }
}
