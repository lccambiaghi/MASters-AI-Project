package plan;

import communicationclient.Agent;
import communicationclient.Node;
import level.Box;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.TreeMap;

public class ConflictDetector {
    //Map of where agents are at a time in their refineBoxToChar
    private TreeMap<Integer,Node> timeMap;
    private Agent owner;
    private HashMap<Integer, LinkedList<Box>> boxMap;

    public ConflictDetector(Agent owner){
        this.owner = owner;
        timeMap = new TreeMap<>();
        boxMap = new HashMap<>();
    }

    // return time of first occurring conflict
    public int checkPlan(LinkedList<Node> otherAgentPlan, int solutionStart){
        int conflictPoint = -1;
        for (int timeStep=0; timeStep < otherAgentPlan.size();timeStep++) {
            Node n = otherAgentPlan.get(timeStep);
            if(!timeMap.containsKey(timeStep+solutionStart)){//Check from solutionstart in global plan
                //Assumue last position of this agent
                Node tmp = new Node(null);
                tmp.agentRow = owner.getAgentRow();
                tmp.agentCol = owner.getAgentCol();
                if(!timeMap.isEmpty()) tmp = timeMap.get(timeMap.lastKey());
                Point otherAgentPoint = new Point(n.agentRow,n.agentCol);
                Point thisAgentPoint = new Point(tmp.agentRow,tmp.agentCol);
                // Has another agent planned to move to the same point?
                if(otherAgentPoint.equals(thisAgentPoint)){
                    conflictPoint = timeStep+solutionStart;
                    return conflictPoint;
                }
            }else{
                Point otherAgentPoint = new Point(n.agentRow, n.agentCol);

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
                    Point thisAgentPointBefore = new Point(thisAgentNodeBefore.agentRow, thisAgentNodeBefore.agentCol);
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
        Node agentNodeCurrent = timeMap.get(timeStep + solutionStart); // gets the map of agent points at that that time
        Point otherAgentPoint = new Point(node.agentRow, node.agentCol);
        Point thisAgentPoint = new Point(agentNodeCurrent.agentRow, agentNodeCurrent.agentCol);

        return otherAgentPoint.equals(thisAgentPoint);
    }

    private boolean collisionWithBox(Integer timeStep, Integer solutionStart, Node node){
        LinkedList<Box> boxList = this.boxMap.get(timeStep + solutionStart);
        Point agentPoint = new Point(node.agentRow, node.agentCol);

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
