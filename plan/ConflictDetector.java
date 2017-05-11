package plan;

import communicationclient.Agent;
import communicationclient.Node;
import level.Box;

import java.util.HashMap;
import java.util.LinkedList;

public class ConflictDetector {
    //Map of where agents are at a time in their refineBoxToChar
    private HashMap<Integer, LinkedList<Box>> boxMap;
    private HashMap<Integer, Node> timeMap;

    public ConflictDetector(){
        boxMap = new HashMap<>();
        timeMap = new HashMap<>();
    }

    // return time of first occurring conflict
    public int checkPlan(LinkedList<Node> otherAgentPlan, int solutionStart){
        int conflictPoint = -1;
        for (int timeStep = 0; timeStep < otherAgentPlan.size(); timeStep++) {
            Node n = otherAgentPlan.get(timeStep);
            if(!timeMap.containsKey(timeStep + solutionStart)){ // Check from solutionstart in global plan
                return -1; // No conflict as agentPlan is not as long as otherAgentPlan
            }else{
                AgentPoint otherAgentPoint = new AgentPoint(n.agentRow, n.agentCol, n.agentId, n.action);

                if(collisionWithAgent(timeStep, solutionStart, n)){
                    conflictPoint = timeStep + solutionStart;
                    return conflictPoint;
                }

                if(collisionWithBox(timeStep, solutionStart, n)) {
                    conflictPoint = timeStep + solutionStart;
                    return conflictPoint;
                }

                // Was another agent at t-1 in the cell I now want to reach?
                if(timeStep > 0){
                    Node thisAgentNodeBefore = timeMap.get(timeStep + solutionStart - 1);
                    AgentPoint thisAgentPointBefore = new AgentPoint(thisAgentNodeBefore.agentRow, thisAgentNodeBefore.agentCol, thisAgentNodeBefore.agentId, thisAgentNodeBefore.action);
                    if(thisAgentPointBefore.equals(otherAgentPoint)){ // if there was an agent in the cell I now want to reach
                        conflictPoint = timeStep+solutionStart;
                        return conflictPoint;
                    }
                }
            }
        }

        return conflictPoint;
    }

    private boolean collisionWithAgent(Integer timeStep, Integer solutionStart, Node node){
        Node agentNodeCurrent = this.timeMap.get(timeStep + solutionStart);
        AgentPoint otherAgentPoint = new AgentPoint(node.agentRow, node.agentCol, node.agentId, node.action);
        AgentPoint thisAgentPoint = new AgentPoint(agentNodeCurrent.agentRow, agentNodeCurrent.agentCol, agentNodeCurrent.agentId, agentNodeCurrent.action);

        return otherAgentPoint.equals(thisAgentPoint);
    }

    private boolean collisionWithBox(Integer timeStep, Integer solutionStart, Node node){
        Node agentNodeCurrent = this.timeMap.get(timeStep + solutionStart);
        updateBoxMap(agentNodeCurrent.getBoxes());
        LinkedList<Box> boxList = this.boxMap.get(timeStep + solutionStart);
        AgentPoint agentPoint = new AgentPoint(node.agentRow, node.agentCol, node.agentId, node.action);
        for (Box box : boxList) {
            if (agentPoint.getAgentCol() == box.getCol() &&
                agentPoint.getAgentRow() == box.getRow()) {
                    return true;
            }
        }
        return false;
    }

    private void updateBoxMap(Box[][] boxes){
        LinkedList<Box> boxList = new LinkedList<>();

        for(int i = 0; i < boxes.length; i++){
            for(int j = 0; j < boxes[i].length; j++){
                if (boxes[i][j] != null){
                    Box box = new Box(j, i, boxes[i][j].getBoxChar(), boxes[i][j].getBoxColor());
                    boxList.add(box);
                }
            }
        }

        this.boxMap.put(this.boxMap.size(), boxList);
    }

    public void addPlan(LinkedList<Node> agentPlan){
        for (int i = 0; i < agentPlan.size(); i++) {
            Node n = agentPlan.get(i);
            timeMap.put(i, n);
            updateBoxMap(n.getBoxes());
        }
    }
}
