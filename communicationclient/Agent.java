package communicationclient;

import communication.Message;
import communication.MsgHub;
import communication.MsgType;
import communication.GoalMessage;
import goal.*;
import level.*;
import plan.ConflictDetector;
import plan.Planner;

import java.util.*;

public class Agent {

    private char id;
    private Color color;
    private int agentRow;
    private int agentCol;
    private Strategy strategy;
    private Planner planner;
    private int numberOfGoals;

    private LinkedList<Node> combinedSolution;
    private LinkedList<Node> goalSolution;
    private ArrayDeque<Goal> subGoals;
    private ArrayList<Box> potentialBoxes = new ArrayList<>();
    private HashSet<Box> removedBoxes = new HashSet<>();

    public Agent(char id, Strategy strategy, int row, int col) {
        this.subGoals = new ArrayDeque<>();
        this.combinedSolution = new LinkedList<>();
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

                Node initialPosition = this.goalSolution.getFirst().parent;

                this.agentRow = initialPosition.agentRow;
                this.agentCol = initialPosition.agentCol;

                for (int row = 0; row < Level.getInstance().MAX_ROW; row++) {
                    for (int col = 0; col < Level.getInstance().MAX_COL; col++) {
                        if (initialPosition.boxes[row][col] != null) {
                            Box box =  initialPosition.boxes[row][col];
                            box.setRow(row) ;
                            box.setCol(col);

                        }

                    }
                }

                return null;
            }
        }
        return this.goalSolution;
    }

    public LinkedList<Node> searchSubGoal(Goal subGoal) {
        System.err.println("Agent " + getId() + " started search for subgoal: "+subGoal.toString()+" with strategy " + this.strategy.toString());
        Node initialNode = new Node(subGoal, this);
        //TODO make sure position is updated
        HashSet<Box> allBoxes = Level.getInstance().getAllBoxes();
        for (Box b : allBoxes) {
            Box firstPotential = this.potentialBoxes.isEmpty() ? null : this.potentialBoxes.get(0);
            if (this.potentialBoxes.isEmpty() || !firstPotential.equals(b)){
                initialNode.addBox(b);
            }else{
                this.removedBoxes.add(b); //TODO We also remove the box in all future searches. This should not happen - remember to reset potentialboxes
            }
        }
        /*
        We are adding a wall to this nodes walls array at the position where the other agent is stuck
        This is done so that we don't risk finding a solution that goes through the agent that we need to
        free.
        */
        if(subGoal instanceof SubGoalMoveBoxOutTheWay){
            SubGoalMoveBoxOutTheWay sub = (SubGoalMoveBoxOutTheWay) subGoal;
            initialNode.addWall(sub.getAgentToFree().agentRow, sub.getAgentToFree().agentCol);
            //initialNode.getWalls()[sub.getAgentToFree().agentRow][sub.getAgentToFree().agentCol] = true;
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
            }
            iterations++;
        }
        return plan;
    }

    public void broadcastMessage(Message message) {
        MsgHub.getInstance().broadcast(message);
    }

    /**
     * Negotiate the goalSolution until all other agents agree
     */
    public void negotiateGoalSolution() {
        MsgHub msgHub = MsgHub.getInstance();
        boolean solutionAccepted = false;

        while(!solutionAccepted){
            solutionAccepted = true;

            for (Agent other: MsgHub.getInstance().getAllAgents()){
                if(other.getId() != this.getId()){
                    Message solutionAnnouncement = new Message(MsgType.announce, goalSolution, this.id);
                    solutionAnnouncement.setContentStart(this.combinedSolution.size());

                    msgHub.sendMessage(other.getId(), solutionAnnouncement);
                    if(checkReplies(solutionAnnouncement) != true){
                        solutionAccepted = false;
                        break;
                    }

                }
            }
        }

        this.combinedSolution.addAll(this.goalSolution);
    }

    public boolean checkReplies(Message message) {
        Queue<Message> responses = MsgHub.getInstance().getResponses(message);

        boolean proposalAccepted = false;
        Message respToProposal;
        for(Message resp : responses)
            switch (resp.getType()){
                case agree: // the other agent agrees with the proposed plan
                    break;
                case request://Another agent request to change solution of this agent
                    this.goalSolution = resp.getContent();
                    return false;
                case propose://Another agent propose to free this agent
                    if(!proposalAccepted){
                        GoalMessage msg = (GoalMessage) resp;
                        GoalFreeAgent goal = (GoalFreeAgent)msg.getGoal();
                        respToProposal = new GoalMessage(MsgType.acceptProposal,goal, null, id);
                        sendResponse(resp.getSender(),respToProposal);
                        proposalAccepted = true;
                    }else{
                        respToProposal = new Message(MsgType.rejectProposal, null, id);
                        sendResponse(resp.getSender(),respToProposal);
                    }
                    break;
            }

        return true;

    }

    private void sendResponse(char receiver, Message response){
        MsgHub.getInstance().sendMessage(receiver,response);
    }

    public void receiveMessage(Message message){
        MsgHub msgHub = MsgHub.getInstance();

        switch (message.getType()){
            case announce: // for now: announcement of solution
                // check if there is a conflict
                // if yes, send back request

                LinkedList<Node> otherAgentSolution = message.getContent();
                int solutionStart = message.getContentStart();

                int conflictTime = checkForConflicts(otherAgentSolution,solutionStart);

                //TODO: remove Hotfix for when agents are colliding at step 0
                if (conflictTime == 0 ){
                    otherAgentSolution = padNoOpNode(otherAgentSolution);
                }

                if (conflictTime > -1){
                    if (conflictTime >= combinedSolution.size()){
                        Goal moveOutTheWay = new GoalMoveOutTheWay(otherAgentSolution);
                        moveOutTheWay.setPriority(1);
                        moveOutTheWay.setAgent(this);
                        planner.addGoal(moveOutTheWay);

                        Message solutionAccepted = new Message(MsgType.request, otherAgentSolution, id);
                        msgHub.reply(message, solutionAccepted);

                    }else{
                        LinkedList<Node> newSolution = makeOtherAgentWait(otherAgentSolution, solutionStart);
                        Message requestedSolution = new Message(MsgType.request, newSolution, id);
                        msgHub.reply(message, requestedSolution);
                    }

                }else{
                    //Message solutionAccepted = new Message(MsgType.request, otherAgentSolution, id);
                    Message agree = new Message(MsgType.agree, null, id);
                    msgHub.reply(message, agree);
                }
                break;
            case request:
                GoalMessage msg = (GoalMessage) message;
                GoalFreeAgent goal = (GoalFreeAgent)msg.getGoal();
                if(goal.getBox().getBoxColor()==this.color){
                    //TODO maybe search for solution!
                    LinkedList<Node> proposedSolution = new LinkedList<>();
                    Message proposeMessage = new GoalMessage(MsgType.propose,goal,proposedSolution ,id);
                    msgHub.reply(message, proposeMessage);
                }
                break;
            case acceptProposal:
                GoalMessage goalMessage = (GoalMessage) message;
                GoalFreeAgent freeAgent = (GoalFreeAgent)goalMessage.getGoal();
                freeAgent.setAgent(this);
                break;
            case rejectProposal:

                break;
        }

    }

    private int checkForConflicts(LinkedList<Node> otherAgentSolution, int solutionStart) {

        ConflictDetector cd = new ConflictDetector(this);
        cd.addPlan(this.combinedSolution);

        return cd.checkPlan(otherAgentSolution, solutionStart);
    }

    private LinkedList<Node> makeOtherAgentWait(LinkedList<Node> oldSolution, int solutionStart){

        ConflictDetector cd = new ConflictDetector(this);
        cd.addPlan(combinedSolution);

        int conflictTime = cd.checkPlan(oldSolution, solutionStart);
        LinkedList<Node> newSolution = new LinkedList<>(oldSolution);


        while (conflictTime > -1){
//            System.err.println("Conflict found at " + conflictTime + " between " + this.getId() + " and " + oldSolution.getFirst().agentId + "in cell otheragentNode\n" + newSolution.get(solutionStart+conflictTime) + " and thisAgentNode\n" +this.getCombinedSolution().get(solutionStart+conflictTime) );
            System.err.println("Conflict found at " + conflictTime + " between " + this.getId() + " and " + oldSolution.getFirst().agentId);
            newSolution = padNoOpNode(oldSolution);
            conflictTime = cd.checkPlan(newSolution, solutionStart);
            oldSolution = newSolution;
        }

        return newSolution;
    }

    private LinkedList<Node> padNoOpNode(LinkedList<Node> oldSolution) {
        LinkedList<Node> newSolution = new LinkedList<>(oldSolution);
        Node initialNode = oldSolution.getFirst().parent;
        Node noOp = new Node(initialNode);
        noOp.setBoxes(initialNode.getBoxesCopy());
        noOp.agentRow = initialNode.agentRow;
        noOp.agentCol = initialNode.agentCol;
        noOp.action= new Command(Command.Type.NoOp, null,null);
        newSolution.addFirst(noOp);
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

    public int getAgentCol() {
        return this.agentCol;
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

    public LinkedList<Node> getCombinedSolution() {
        return combinedSolution;
    }

    public void setPlanner(Planner planner) {
        this.planner = planner;
    }

}
