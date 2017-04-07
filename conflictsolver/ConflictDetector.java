package conflictsolver;

import communicationclient.Command;
import communicationclient.Node;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * Created by salik on 07-04-2017.
 * Class to detect conflicts between agents plans
 */
public class ConflictDetector {
    //Map of where agents are at a time in their plan
    private HashMap<Integer, HashMap<AgentPoint, Node>> timeMap;

    public ConflictDetector (){
        timeMap = new HashMap<>();
    }

//    public boolean isConflict(){
//
//    }
    public int checkPlan(LinkedList<Node> agentPlan){
        int conflictPoint = -1;
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

                if(i > 0){
                    HashMap<AgentPoint, Node> agentPointsBefore = timeMap.get(i-1);
                    Node before = agentPointsBefore.get(agentPoint);
                    if(before !=null){
                        //Are they trying to cross each other?
                        if(Command.isOpposite(before.action.dir1,n.action.dir1)){
                            conflictPoint = i;
                            return conflictPoint;
                        }
                    }
                }
                //Are they trying to go to same space at same time.
                if(agentPointsCurrent.containsKey(agentPoint)){
                        conflictPoint = i;
                        return conflictPoint;
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
