package communicationclient;

import communication.Message;
import communication.MsgHub;
import communication.MsgType;
import communication.GoalMessage;
import goal.Goal;
import goal.GoalBoxToCell;
import goal.GoalFreeAgent;
import goal.SubGoalMoveBoxOutTheWay;
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

    private int numberOfGoals;
    private LinkedList<Node> allGoalSolution;
    private LinkedList<Node> goalSolution;
    private ArrayDeque<Goal> subGoals;
    private ArrayList<Box> potentialBoxes = new ArrayList<>();
    private HashSet<Box> removedBoxes = new HashSet<>();

    public Agent(char id, Strategy strategy, int row, int col) {
        this.subGoals = new ArrayDeque<>();
        this.allGoalSolution = new LinkedList<>();
        this.goalSolution = new LinkedList<>();
        this.color = Color.blue;
        this.id = id;
        this.strategy = strategy;
        this.agentRow = row;
        this.agentCol = col;
    }

    public void refineBoxToChar(GoalBoxToCell goal){
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
    public LinkedList<Node> searchGoal(Goal goal){
        this.goalSolution = new LinkedList<>();
        this.potentialBoxes = new ArrayList<>();
        goal.refine();
        int agentStuck = 0;
        LinkedList<Goal> subgoals = goal.getSubgoals();
        for(Goal subgoal : subgoals){
            LinkedList<Node> solution = searchSubGoal(subgoal);
            if (solution!=null){
                this.goalSolution.addAll(solution);
            }else{
                while(solution==null){
                    agentStuck++;
                    solution = searchSubGoal(subgoal);
                }
                this.goalSolution.addAll(solution);
                //Reset agent position to start
                this.agentRow = solution.getFirst().parent.agentRow;
                this.agentCol = solution.getFirst().parent.agentCol;
                return null;
            }
        }
        return this.goalSolution;
    }

    public LinkedList<Node> searchSubGoal(Goal subGoal) {
        System.err.println("Agent " + getId() + " started searchSubGoal with strategy " + this.strategy.toString());
        Node initialNode = new Node(subGoal, this);
        //TODO make sure position is updated
        HashSet<Box> allBoxes = Level.getInstance().getAllBoxes();
        for (Box b : allBoxes) {
            Box firstPotential = this.potentialBoxes.isEmpty() ? null : this.potentialBoxes.get(0);
            if (this.potentialBoxes.isEmpty() || !firstPotential.equals(b)){
                initialNode.addBox(b);
            }else{
                this.removedBoxes.add(b);
            }
        }
        if(subGoal instanceof SubGoalMoveBoxOutTheWay){
            SubGoalMoveBoxOutTheWay sub = (SubGoalMoveBoxOutTheWay) subGoal;
            initialNode.walls[sub.getAgentToFree().agentRow][sub.getAgentToFree().agentCol] = true;
        }

        initialNode.agentRow = this.agentRow;
        initialNode.agentCol = this.agentCol;

        this.strategy.clearFrontier();
        this.strategy.addToFrontier(initialNode);

        int iterations = 0;
        LinkedList<Node> plan;
        Node leafNode = null;
        while (true) {
            if (iterations == 1000) {
                System.err.println(this.strategy.searchStatus());
                iterations = 0;
            }
            if (this.strategy.frontierIsEmpty()) {
                this.potentialBoxes=leafNode.getPotentialBoxes();
                return null;
            }
            leafNode = this.strategy.getAndRemoveLeaf();
            if (leafNode.isGoalState()) {
                plan = leafNode.extractPlan();
                if (plan.size() > 0) {
                    Node goalState = plan.getLast();
                    this.agentRow = goalState.agentRow;
                    this.agentCol = goalState.agentCol;
                        for (int row = 0; row < Level.getInstance().MAX_ROW; row++) {
                            for (int col = 0; col < Level.getInstance().MAX_COL; col++) {
                                if (goalState.boxes[row][col] != null) {
                                    Box box = goalState.boxes[row][col];
                                    box.setRow(row);
                                    box.setCol(col);

                                }

                            }
                        }
                    }
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
        return plan;
    }

    public void broadcastMessage(Message message) {
        MsgHub.getInstance().broadcast(message);
    }

    public void evaluateMessage(Message message) {
        Queue<Message> requests = MsgHub.getInstance().getResponses(message);
        boolean proposalAccepted = false;
        Message response;
        for(Message request : requests)
            switch (request.getType()){
                case request://Another agent request to change solution of this agent

                    this.goalSolution = request.getContent();
                    response = new Message(MsgType.agree, null, id);
                    sendResponse(request, response);
                    break;
                case propose://Another agent propose to free this agent
                    if(!proposalAccepted){
                        GoalMessage msg = (GoalMessage) request;
                        GoalFreeAgent goal = (GoalFreeAgent)msg.getGoal();
                        response = new GoalMessage(MsgType.acceptProposal,goal, null, id);
                        sendResponse(request.getSender(),response);
                        proposalAccepted = true;
                    }else{
                        response = new Message(MsgType.rejectProposal, null, id);
                        sendResponse(request.getSender(),response);
                    }
                    break;
            }

    }

    private void sendResponse(Message request, Message response) {
        MsgHub.getInstance().broadcast(response);
    }
    private void sendResponse(char receiver, Message response){
        MsgHub.getInstance().sendMessage(receiver,response);
    }

    public void receiveAnnouncement(Message announcement){
        MsgHub msgHub = MsgHub.getInstance();

        switch (announcement.getType()){
            case inform: // for now: announcement of solution
                // check if there is a conflict
                // if yes, send back request

                LinkedList<Node> otherAgentSolution = announcement.getContent();
                int solutionStart = announcement.getContentStart();

                int conflictTime = checkForConflicts(otherAgentSolution,solutionStart);

                if (conflictTime > -1){
                    LinkedList<Node> newSolution = makeOtherAgentWait(otherAgentSolution, solutionStart);
                    Message requestedSolution = new Message(MsgType.request, newSolution, id);
                    msgHub.reply(announcement, requestedSolution);
                }
                break;
            case request:
                GoalMessage msg = (GoalMessage) announcement;
                GoalFreeAgent goal = (GoalFreeAgent)msg.getGoal();
                if(goal.getBox().getBoxColor()==this.color){
                    //TODO maybe search for solution!
                    LinkedList<Node> proposedSolution = new LinkedList<>();
                    Message proposeMessage = new GoalMessage(MsgType.propose,goal,proposedSolution ,id);
                    msgHub.reply(announcement, proposeMessage);
                }
                break;
            case acceptProposal:
                GoalMessage goalMessage = (GoalMessage) announcement;
                GoalFreeAgent freeAgent = (GoalFreeAgent)goalMessage.getGoal();
                freeAgent.setAgent(this);
                break;
            case rejectProposal:

                break;
        }

    }

    private int checkForConflicts(LinkedList<Node> otherAgentSolution, int solutionStart) {

        if(this.allGoalSolution.size() == 0)
            return -1;

        ConflictDetector cd = new ConflictDetector();

        cd.addPlan(this.allGoalSolution);

        return cd.checkPlan(otherAgentSolution, solutionStart);

    }

    private LinkedList<Node> makeOtherAgentWait(LinkedList<Node> oldSolution, int solutionStart){

        ConflictDetector cd = new ConflictDetector();

        cd.addPlan(allGoalSolution);

        int conflictTime = cd.checkPlan(oldSolution, solutionStart);

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
            conflictTime = cd.checkPlan(newSolution, solutionStart);
        }

        return newSolution;

    }
    public LinkedList<Node> getGoalSolution() {
        return goalSolution;
    }

    public void setGoalSolution(LinkedList<Node> goalSolution) {
        this.goalSolution = goalSolution;
    }

    public char getId() {
        return this.id;
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

    public HashSet<Box> getRemovedBoxes() {
        return removedBoxes;
    }

    public LinkedList<Node> getAllGoalSolution() {
        return allGoalSolution;
    }
}
