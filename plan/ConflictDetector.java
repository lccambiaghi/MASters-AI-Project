package plan;

import communicationclient.Agent;
import communicationclient.Node;
import level.Box;

import java.util.HashMap;
import java.util.LinkedList;

public class ConflictDetector {
    //Map of where agents are at a time in their refineBoxToChar
    private HashMap<Integer, HashMap<AgentPoint, Node>> timeMap;
    private HashMap<Integer, LinkedList<Box>> boxMap;

    public ConflictDetector(){
        boxMap = new HashMap<>();
        timeMap = new HashMap<>();
    }

    // return time of first occurring conflict
    public int checkPlan(LinkedList<Node> agentPlan){
        int conflictPoint = -1;
        for (int timeStep = 0; timeStep < agentPlan.size(); timeStep++) {
            Node n = agentPlan.get(timeStep);
            if(!timeMap.containsKey(timeStep)){
                HashMap<AgentPoint, Node> agentPoints = new HashMap<>();
                AgentPoint agentPoint = new AgentPoint(n.agentRow, n.agentCol, n.agentId, n.action);
                agentPoints.put(agentPoint, n);
                timeMap.put(timeStep, agentPoints);
                updateBoxMap(n.getBoxes());
            }else{
                HashMap<AgentPoint, Node> agentPointsCurrent = timeMap.get(timeStep); // gets the map of agent points at that that time
                AgentPoint agentPoint = new AgentPoint(n.agentRow, n.agentCol, n.agentId, n.action);

                // Two agents colliding
                if(agentPointsCurrent.containsKey(agentPoint)){
                    conflictPoint = timeStep;
                    return conflictPoint;
                }

                if(collisionWithBox(agentPoint, timeStep, n)) {
                    return timeStep;
                }

                // Was another agent at t-1 in the cell I now want to reach?
                if(timeStep > 0){
                    HashMap<AgentPoint, Node> agentPointsBefore = timeMap.get(timeStep-1);
                    Node before = agentPointsBefore.get(agentPoint);

                    if(before !=null){
                        // if there was an agent in the cell I now want to reach
                        // Has he moved out of the cell?
                        // If yes, it is not a conflict
                        // TODO

                        //Is he going to bump into me?
                        //if(Command.isOpposite(before.action.dir1,n.action.dir1)){
                        // TODO
                        //}

                        conflictPoint = timeStep;
                        return conflictPoint;
                    }
                }
            }
        }
        addPlan(agentPlan);

        return conflictPoint;
    }

    private void createTimeMap(){

    }

    private void updateBoxMap(Box[][] boxes){
        LinkedList<Box> boxList = new LinkedList<>();
        for(Box[] row : boxes) {
            for(Box box : row) {
                if (box != null)
                    boxList.add(box);
            }
        }
        this.boxMap.put(this.boxMap.size(), boxList);
    }

    private boolean collisionWithBox(AgentPoint agentPoint, Integer timeStep, Node node){
        LinkedList<Box> boxList = this.boxMap.get(timeStep);
        for (Box box : boxList) {
            if (agentPoint.getAgentCol() == box.getCol() &&
                agentPoint.getAgentRow() == box.getRow() &&
                node.getAgentColor() != box.getBoxColor())
                System.err.println("Collision with box detected");
                return false;
        }
        return false;
    }

    private void addPlan(LinkedList<Node> agentPlan){
        for (int i=0;i < agentPlan.size();i++) {
            Node n = agentPlan.get(i);
            if(!timeMap.containsKey(i)){
                HashMap<AgentPoint, Node> agentPoints = new HashMap<>();
                AgentPoint agentPoint = new AgentPoint(n.agentRow, n.agentCol, n.agentId, n.action);
                agentPoints.put(agentPoint, n);
                timeMap.put(i,agentPoints);
            }else{
                HashMap<AgentPoint, Node> agentPointsCurrent = timeMap.get(i);
                AgentPoint agentPoint = new AgentPoint(n.agentRow, n.agentCol, n.agentId, n.action);
                agentPointsCurrent.put(agentPoint, n);
            }
        }
    }
}
