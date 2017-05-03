package communicationclient;

import communication.Message;
import communication.MsgHub;
import communication.MsgType;
import goal.Goal;
import goal.GoalBoxToChar;
import level.*;
import plan.ConflictDetector;

import java.util.*;

/**
 * Created by salik on 31-03-2017.
 */
public class Agent {

    private char id;
    private Color color;
    private int agentRow;
    private int agentCol;

    private Strategy strategy;
    private Node initialState;

    private int numberOfGoals;
    private LinkedList<Node> combinedSolution;
    private ArrayDeque<Goal> subGoals;

    public Agent(char id, Strategy strategy, int row, int col) {
        this.subGoals = new ArrayDeque<>();
        this.combinedSolution = new LinkedList<>();
        this.color = Color.blue;
        this.id = id;
        this.strategy = strategy;
        this.agentRow = row;
        this.agentCol = col;
    }

    public void refineBoxToChar(GoalBoxToChar goal){
        System.err.println("Agent " + this.id + " started planning");

        Box goalBox = goal.getBox();
        if (goalBox.getDestination() != null) { // if box has a goal assigned
            goal.refine();
            for(Goal subgoal : goal.getSubgoals()){
                this.subGoals.addLast(subgoal);
            }
        }
        System.err.println("Agent " + this.id + " planned " + subGoals.size() + "subgoals");
        //return subGoals;
    }

    public LinkedList<Node> search() {
        System.err.println("Agent " + getId() + " started search with strategy " + this.strategy.toString());
        Goal firstSub = this.subGoals.peekFirst();
        Node initialNode = new Node(firstSub, this);
        HashSet<Box> allBoxes = Level.getInstance().getAllBoxes();
        
        for (Box b: allBoxes) {
            initialNode.addBox(b);
        }

        initialNode.agentRow = this.agentRow;
        initialNode.agentCol = this.agentCol;
        setInitialState(initialNode);

        // TODO review loop
        while(!this.subGoals.isEmpty()){
            this.subGoals.pollFirst();
            this.strategy.clearFrontier();
            this.strategy.addToFrontier(getInitialState());
            int iterations = 0;

            while (true) {
                if (iterations == 1000) {
                    System.err.println(this.strategy.searchStatus());
                    iterations = 0;
                }

                if (this.strategy.frontierIsEmpty()) {
                    return null;
                }
                
                Node leafNode = this.strategy.getAndRemoveLeaf();

                if (leafNode.isGoalState()) {
                    LinkedList<Node> plan = leafNode.extractPlan();
                    if (plan.size() > 0){
                        Node goalState = plan.getLast();
                        this.agentRow = goalState.agentRow;
                        this.agentCol = goalState.agentCol;
                        Node newStart = new Node(subGoals.peekFirst(), this);
                        newStart.setBoxes(goalState.getBoxesCopy());
                        newStart.agentCol = goalState.agentCol;
                        newStart.agentRow = goalState.agentRow;
                        setInitialState(newStart);//Update initial state to where we end after this subgoal.
                    }else{
                        Node newStart = new Node(subGoals.peekFirst(), this);
                        newStart.agentRow = this.agentRow;
                        newStart.agentCol = this.agentCol;
                        newStart.setBoxes(leafNode.getBoxesCopy());
                        setInitialState(newStart);//Update initial state to where we end after this subgoal.
                    }
                    this.combinedSolution.addAll(plan);
                    break;
                }

                this.strategy.addToExplored(leafNode);
                for (Node n : leafNode.getExpandedNodes()) { // The list of expanded nodes is shuffled randomly; see Node.java.
                    if (!this.strategy.isExplored(n) && !this.strategy.inFrontier(n)) {
                        this.strategy.addToFrontier(n);
                    }
                    iterations++;
                }
            }
        }
        return this.combinedSolution;
    }

    public void broadcastSolution(Message solutionAnnouncement) {
        MsgHub.getInstance().broadcast(solutionAnnouncement);
    }

    public void evaluateRequests(Message solutionAnnouncement) {
        Queue<Message> requests = MsgHub.getInstance().getResponses(solutionAnnouncement);

        for(Message request : requests)
            if (request.getType() == MsgType.request){

                combinedSolution = request.getContent();

                Message response = new Message(MsgType.agree, null, id);

                sendResponse(request, response);

            }

    }

    private void sendResponse(Message request, Message response) {
        MsgHub.getInstance().broadcast(response);
    }

    public void receiveAnnouncement(Message announcement){
        MsgHub msgHub = MsgHub.getInstance();

        switch (announcement.getType()){
            case inform: // for now: announcement of solution
                // check if there is a conflict
                // if yes, send back request

                LinkedList<Node> otherAgentSolution = announcement.getContent();

                int conflictTime = checkForConflicts(otherAgentSolution);

                if (conflictTime > -1){
                    LinkedList<Node> newSolution = makeOtherAgentWait(otherAgentSolution);
                    Message requestedSolution = new Message(MsgType.request, newSolution, id);
                    msgHub.reply(announcement, requestedSolution);
                }

        }

    }

    private int checkForConflicts(LinkedList<Node> otherAgentSolution) {

        if(combinedSolution == null)
            return 0;

        ConflictDetector cd = new ConflictDetector();

        cd.addPlan(combinedSolution);

        return cd.checkPlan(otherAgentSolution);

    }

    private LinkedList<Node> makeOtherAgentWait(LinkedList<Node> oldSolution){

        ConflictDetector cd = new ConflictDetector();

        cd.addPlan(combinedSolution);

        int conflictTime = cd.checkPlan(oldSolution);

        LinkedList<Node> newSolution = new LinkedList<>(oldSolution);

        while (conflictTime > -1){
            System.err.println("Conflict found at "+conflictTime);

            Node n = oldSolution.getFirst();
            Node noOp = new Node(null);
            noOp.setBoxes(n.getBoxesCopy());
            noOp.agentRow = n.agentRow;
            noOp.agentCol = n.agentCol;
            noOp.action= new Command(Command.Type.NoOp, n.action.dir1,n.action.dir2);
            newSolution.addFirst(noOp);
            conflictTime = cd.checkPlan(newSolution);
        }

        return newSolution;

    }

    public LinkedList<Node> getCombinedSolution() {
        return combinedSolution;
    }

    public char getId() {
        return this.id;
    }

    private Node getInitialState() {
        return this.initialState;
    }

    private void setInitialState(Node state) {
        this.initialState = state;
    }

    public Color getColor() {
        return this.color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public int getAgentRow() {
        return this.agentRow;
    }

    public void setAgentRow(int agentRow) {
        this.agentRow = agentRow;
    }

    public int getAgentCol() {
        return this.agentCol;
    }

    public void setAgentCol(int agentCol) {
        this.agentCol = agentCol;
    }

    public int getNumberOfGoals() {
        return numberOfGoals;
    }

    public void setNumberOfGoals(int numberOfGoals) {
        this.numberOfGoals = numberOfGoals;
    }

    public Strategy getStrategy() {
        return strategy;
    }
}
