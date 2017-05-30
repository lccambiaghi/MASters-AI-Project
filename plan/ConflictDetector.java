package plan;

import communicationclient.Agent;
import communicationclient.Command;
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
    public Conflict checkPlan(LinkedList<Node> otherAgentPlan, int solutionStart){
        int conflictPoint;
        int sizeThisAgentPlan = timeMap.size();
        // HOTFIX FOR THE SITUATION IN WHICH AN AGENT ALREADY MOVED OUT OF THE WAY
        if(otherAgentPlan.size()==0) return null; // NO conflict

        int timeStepsToCheck = otherAgentPlan.size() > (sizeThisAgentPlan - solutionStart) ? otherAgentPlan.size() : (sizeThisAgentPlan - solutionStart); // If otherAgentPlan is longer than "the rest" (starting from solutionstart) of this agents plan, then go through all of otheragents plan else check the rest of this agents plan

        for (int timeStep=0; timeStep < timeStepsToCheck;timeStep++) {
            Node n = timeStep >= otherAgentPlan.size()  ? otherAgentPlan.getLast() : otherAgentPlan.get(timeStep);
            conflictPoint = timeStep+solutionStart;
            if(!timeMap.containsKey(timeStep+solutionStart)){//Check from solutionstart in global plan
                //Assumue last position of this agent
                Node tmp = new Node(null);
                tmp.agentRow = owner.getAgentRow();
                tmp.agentCol = owner.getAgentCol();
                if(!timeMap.isEmpty()) tmp = timeMap.get(timeMap.lastKey());
                Point otherAgentPoint = new Point(n.agentRow,n.agentCol);
                Point thisAgentPoint = new Point(tmp.agentRow,tmp.agentCol);
                if(n.boxMoved != null){
                    Point otherAgentBoxMoved = new Point(n.boxMovedRow,n.boxMovedCol);
                    Point otherAgentBoxMovedBefore = new Point(n.oldBoxMovedRow,n.oldBoxMovedCol);
                    if(thisAgentPoint.equals(otherAgentBoxMoved)||thisAgentPoint.equals(otherAgentBoxMovedBefore)){
                        return new Conflict(conflictPoint, Conflict.type.agentInTheWay,n,tmp);
                    }
                }
                // Has another agent planned to move to the same point?
                if(otherAgentPoint.equals(thisAgentPoint)){
                    return new Conflict(conflictPoint, Conflict.type.agentInTheWay,n,tmp);
                }
            }else{

                Point otherAgentPoint = new Point(n.agentRow, n.agentCol);
                if(collisionWithAgent(timeStep, solutionStart, n)){
                    return new Conflict(conflictPoint, Conflict.type.agentagent, n, timeMap.get(timeStep+solutionStart)); //TODO Make sure its not timestep + solutionstart - 1
                    //TODO Might also be a agentbox
                }

                if(owner.getColor() != n.agentColor && collisionWithBox(timeStep, solutionStart, n)) {
                    return new Conflict(conflictPoint, Conflict.type.agentbox, n, timeMap.get(timeStep+solutionStart));
                }

                Node thisAgentNode = timeMap.get(timeStep+solutionStart);
                Point thisAgentPoint = new Point(thisAgentNode.agentRow, thisAgentNode.agentCol);

                Node thisAgentNodeBefore = timeMap.get(timeStep+solutionStart).parent;
                Point thisAgentPointBefore = new Point(thisAgentNodeBefore.agentRow,thisAgentNodeBefore.agentCol);

                Node otherAgentNodeBefore = n.parent;
                Point otherAgentPointBefore = new Point(otherAgentNodeBefore.agentRow,otherAgentNodeBefore.agentCol);
                if(n.boxMoved != null){
                    Point otherAgentBoxMoved = new Point(n.boxMovedRow,n.boxMovedCol);
                    if(otherAgentBoxMoved.equals(thisAgentPoint) || otherAgentBoxMoved.equals(thisAgentPointBefore)){
                        return new Conflict(conflictPoint,Conflict.type.agentbox,n,thisAgentNode);
                    }
                }
                if(thisAgentNode.boxMoved != null){
                    Point thisAgentBoxMoved = new Point(thisAgentNode.boxMovedRow,thisAgentNode.boxMovedCol);
                    Point thisAgentBoxMovedBefore = new Point(thisAgentNode.oldBoxMovedRow,thisAgentNode.oldBoxMovedCol);

                    if(thisAgentBoxMoved.equals(otherAgentPointBefore)|| thisAgentBoxMoved.equals(otherAgentPoint)){
                        return new Conflict(conflictPoint,Conflict.type.agentbox,otherAgentNodeBefore,thisAgentNode);
                    }
                    if(thisAgentBoxMovedBefore.equals(otherAgentPoint)|| thisAgentBoxMovedBefore.equals(otherAgentPointBefore)){
                        return new Conflict(conflictPoint,Conflict.type.agentbox,otherAgentNodeBefore,thisAgentNode);
                    }
                }

                if(thisAgentPointBefore.equals(otherAgentPoint)){
                    return new Conflict(conflictPoint,Conflict.type.agentagent,n,thisAgentNodeBefore);
                }

                if(thisAgentPoint.equals(otherAgentPointBefore)){
                    return new Conflict(conflictPoint,Conflict.type.agentagent,otherAgentNodeBefore,thisAgentNode);
                }
                if(thisAgentPoint.equals(otherAgentPoint)){ // if there was an agent in the cell I now want to reach
                    return new Conflict(conflictPoint, Conflict.type.agentagent,n,thisAgentNode);//other agent moves into cell this agent was in
                }
            }
        }
        return null;//No Conflict
    }

    private boolean collisionWithAgent(Integer timeStep, Integer solutionStart, Node node){
        Node agentNodeCurrent = timeMap.get(timeStep + solutionStart); // gets the map of agent points at that that time
        Point otherAgentPoint = new Point(node.agentRow, node.agentCol);
        Node otherAgentNodeBefore = node.parent;

        Point otherAgentPointBefore = new Point(otherAgentNodeBefore.agentRow,otherAgentNodeBefore.agentCol);
        Point thisAgentPoint = new Point(agentNodeCurrent.agentRow, agentNodeCurrent.agentCol);
        Point thisAgentPointBefore = new Point(agentNodeCurrent.parent.agentRow, agentNodeCurrent.parent.agentCol);
        if(otherAgentPoint.equals(thisAgentPoint)){
            return otherAgentPoint.equals(thisAgentPoint);
        }else if (otherAgentPointBefore.equals(thisAgentPoint)){
            return  otherAgentPointBefore.equals(thisAgentPoint);
        }else if (node.action.actionType== Command.Type.Push){
            Point otherAgentBox = new Point(node.boxMovedRow, node.boxMovedCol);
            if(otherAgentBox.equals(thisAgentPoint)||otherAgentBox.equals(thisAgentPointBefore)) return true;
        }else if(agentNodeCurrent.action.actionType == Command.Type.Push){
            Point thisAgentBoxPoint = new Point(agentNodeCurrent.boxMovedRow,agentNodeCurrent.boxMovedCol);
            if(thisAgentBoxPoint.equals(otherAgentPoint) || thisAgentBoxPoint.equals(otherAgentPointBefore)) return true;
        }
        return false; // No collision

    }

    private boolean collisionWithBox(Integer timeStep, Integer solutionStart, Node node){
        LinkedList<Box> boxList = this.boxMap.get(timeStep + solutionStart);
        Point otherAgentPoint = new Point(node.agentRow, node.agentCol);
//        if(node.action.actionType == Command.Type.Push){
//            Point boxPoint = new Point(node.boxMovedRow, node.boxMovedCol);
//            if(boxPoint.equals(otherAgentPoint)) return true; //TODO Remove this, we check if a box from the otheragents node hits the otheragent (which does not make sense)
//        }

        // Check that otherAgent does not push a box into the box that this agent is moving
        Node thisAgentNode = timeMap.get(timeStep + solutionStart);
        Point thisAgentMovedBoxBefore = new Point(thisAgentNode.oldBoxMovedRow,thisAgentNode.oldBoxMovedCol);
        if(node.action.actionType == Command.Type.Push){
            Point otherAgentBoxPoint = new Point(node.boxMovedRow,node.boxMovedCol);
            if(otherAgentBoxPoint.equals(thisAgentMovedBoxBefore)) return true;
        }
        for (Box box : boxList) {
            if (otherAgentPoint.getAgentCol() == box.getCol() &&
                otherAgentPoint.getAgentRow() == box.getRow() && box.getBoxColor() == owner.getColor()) {
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
