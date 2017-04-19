package plan;

import communicationclient.Node;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by salik on 07-04-2017.
 * Class to detect conflicts between agents plans
 */
public class ConflictDetector {
    //Map of where agents are at a time in their plan
    private HashMap<Integer, HashMap<AgentPoint, Node>> timeMap;

    public ConflictDetector(){
        timeMap = new HashMap<>();
    }

    // return time of first occurring conflict
    public int checkPlan(LinkedList<Node> agentPlan){//TODO need to check for boxes as well in order to complete MAsimple4
        int conflictPoint = -1;
        for (int i=0; i < agentPlan.size();i++) {
            Node n = agentPlan.get(i);
            if(!timeMap.containsKey(i)){
                HashMap<AgentPoint, Node> agentPoints = new HashMap<>();
                AgentPoint agentPoint = new AgentPoint(n.agentRow,n.agentCol, n.agentId, n.action);
                agentPoints.put(agentPoint, n);
                timeMap.put(i,agentPoints);
            }else{
                HashMap<AgentPoint, Node> agentPointsCurrent = timeMap.get(i); // gets the map of agent points at that that time
                AgentPoint agentPoint = new AgentPoint(n.agentRow,n.agentCol, n.agentId, n.action);

                // Has another agent planned to move to the same point?
                if(agentPointsCurrent.containsKey(agentPoint)){
                    conflictPoint = i;
                    return conflictPoint;
                }

                // Was another agent at t-1 in the cell I now want to reach?
                if(i > 0){
                    HashMap<AgentPoint, Node> agentPointsBefore = timeMap.get(i-1);
                    Node before = agentPointsBefore.get(agentPoint);

                    if(before !=null){ // if there was an agent in the cell I now want to reach
                        // Has he moved out of the cell?
                        // If yes, it is not a conflict
                        // TODO

                        //Is he going to bump into me?
                        //if(Command.isOpposite(before.action.dir1,n.action.dir1)){
                        // TODO
                        //}

                        conflictPoint = i;
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
            if(!timeMap.containsKey(i)){
                HashMap<AgentPoint, Node> agentPoints = new HashMap<>();
                AgentPoint agentPoint = new AgentPoint(n.agentRow,n.agentCol, n.agentId, n.action);
                agentPoints.put(agentPoint, n);
                timeMap.put(i,agentPoints);
            }else{
                HashMap<AgentPoint, Node> agentPointsCurrent = timeMap.get(i);
                AgentPoint agentPoint = new AgentPoint(n.agentRow,n.agentCol, n.agentId, n.action);
                agentPointsCurrent.put(agentPoint,n);
            }
        }
    }
}
