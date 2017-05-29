package communicationclient;

import communication.*;
import goal.*;
import graph.Vertex;
import level.*;
import plan.Conflict;
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
    private Goal latestGoal;
    private HashMap<Goal,LinkedList<Node>> oldSolutions = new HashMap<>();
    private HashSet<Vertex> limitedRessources;
    private ArrayList<ResourceRequest> thisAgentResourceRequests = new ArrayList<>();

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
        this.strategy.clearExplored();//Clear explored nodes for each new goal??
        this.latestGoal = goal; // If we need to place it back on the queue
        this.goalSolution = new LinkedList<>();
        this.potentialBoxes = new ArrayList<>();
        this.removedBoxes = new HashSet<>(); // Forget everything about the previously removed boxes
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
                return null;
            }
        }
        return this.goalSolution;
    }


    private LinkedList<Node> searchSubGoal(Goal subGoal) {
        System.err.println("Agent " + getId() + " started search for subgoal: "+subGoal.toString()+" with strategy " + this.strategy.toString());
        Node initialNode = new Node(subGoal, this);
        //TODO make sure position is updated
        HashSet<Box> allBoxes = Level.getInstance().getAllBoxes();
        this.potentialBoxes.removeAll(this.removedBoxes);
        for (Box b : allBoxes) {
            Box firstPotential = this.potentialBoxes.isEmpty() ? null : this.potentialBoxes.get(0);
            if ((this.potentialBoxes.isEmpty() || !firstPotential.equals(b)) && !this.removedBoxes.contains(b)){
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
        //Always add box to get to
        if(subGoal instanceof SubGoalMoveToBox){
            SubGoalMoveToBox sub = (SubGoalMoveToBox) subGoal;
            initialNode.addBox(sub.getBox());
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
            if(this.strategy.countFrontier()>100000){
                this.potentialBoxes=leafNode.getPotentialBoxes();
                return null;
            }
            leafNode = this.strategy.getAndRemoveLeaf();
            if (leafNode.isGoalState()) {
                plan = leafNode.extractPlan();
                if (plan.size() > 0) {
                    updateLevelInstance(plan);
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

    private void broadcastMessage(Message message) {
        MsgHub.getInstance().broadcast(message);
    }

    /**
     * Asks to other agents to free him
     * TODO only ask to same colour agents?
     * @param goal
     */
    public void callForHelp(Goal goal) {

        // Reset level instance so other agents can plan from our initialNode
        resetLevelInstance(this.getGoalSolution());

        for (Box b:this.getRemovedBoxes()) {
            LinkedList<Node> agentRequestCells = this.getGoalSolution();
            //Search for free space
            Goal freeAgent = new GoalFreeAgent(b,agentRequestCells, this);
            int newGoalPriority = goal.getPriority()-1 > 0 ? -1 : goal.getPriority()-1; // Trying to set it with lowest priority
            freeAgent.setPriority(newGoalPriority);//High priority
            MsgContent content = new MsgContent(agentRequestCells);
            Message freeMeRequest = new GoalMessage(MsgType.request,freeAgent, content,this.getId());
            this.broadcastMessage(freeMeRequest);
            this.checkReplies(freeMeRequest);
            planner.addGoal(freeAgent);
        }

        // Forget solution - we queue the goal
        this.setGoalSolution(new LinkedList<>());//Forget Solution

    }

    /**
     * Negotiate the goalSolution until all other agents agree
     */
    public void negotiateGoalSolution() {
        MsgHub msgHub = MsgHub.getInstance();
        int solutionAccepted = 0;
        ArrayList<ResourceRequest> resourceRequests = new ArrayList<>();
        int timestep = 0;
        //Add all nodes where the agent or a box is using a limited Resource TODO maybe put the ressource request at more than one timestep? maybe from first encounter of a limitedresource to the last encounter?
        for (Node n: this.goalSolution) {
            if (limitedRessources.contains(new Vertex(n.agentRow, n.agentCol))) {
                resourceRequests.add(new ResourceRequest(timestep+this.combinedSolution.size(), new Vertex(n.agentRow, n.agentCol)));//Actual timestep
            }
            if (n.action.actionType == Command.Type.Pull || n.action.actionType == Command.Type.Push){
                if (limitedRessources.contains(new Vertex(n.boxMovedRow,n.boxMovedCol)))  {
                    resourceRequests.add(new ResourceRequest(timestep+this.combinedSolution.size(), new Vertex(n.boxMovedRow, n.boxMovedCol)));//Actual timestep
                }
            }
            timestep++;
        }


        loop:while(solutionAccepted == 0){
            solutionAccepted = 1;
            for (Agent other: MsgHub.getInstance().getAllAgents()){
                if(other.getId() != this.getId()){
                    MsgContent content = new MsgContent(this.goalSolution);//New message each time as goalsolution might be updated
                    content.setResourceRequests(resourceRequests);
                    Message solutionAnnouncement = new Message(MsgType.announce, content, this.id);
                    solutionAnnouncement.setContentStart(this.combinedSolution.size());

                    msgHub.sendMessage(other.getId(), solutionAnnouncement);
                    int checkReplies = checkReplies(solutionAnnouncement);
                    if(checkReplies != 1){ // true
                        if(checkReplies == 2){
                            return; // Agent was asked to wait achieving this goal. Exit the negotiationmethod
                        }
                        solutionAccepted = 0; // Agent have been asked to change his solution. We need to rebroadcast to all agents from the start.
                        break;
                    }

                }
            }
        }

        this.combinedSolution.addAll(this.goalSolution);
        this.thisAgentResourceRequests.addAll(resourceRequests);
    }

    public int checkReplies(Message message) {
        Queue<Message> responses = MsgHub.getInstance().getResponses(message);

        boolean proposalAccepted = false;
        Message respToProposal;
        loop:for(Message resp : responses)
            switch (resp.getType()){
                case agree: // the other agent agrees with the proposed plan
                    break;
                case request://Another agent request to change solution of this agent
                    this.goalSolution = resp.getContent().getContent();
                    return 0;
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
                case await:
                    // In this case an agent needs to replan later.
                    this.goalSolution = new LinkedList<>(); // Don't add anything to the overall solution.
                    this.planner.addGoal(latestGoal);
                    resetLevelInstance(message.getContent().getContent()); // Reset the level to the state before the agent searched for a solution
                    return 2;
            }
        return 1;
    }


    private void updateLevelInstance(LinkedList<Node> plan) {
        Node finalNode = plan.getLast();
        this.agentRow = finalNode.agentRow;
        this.agentCol = finalNode.agentCol;
        updateBoxes(finalNode);
    }

    private void resetLevelInstance(LinkedList<Node> plan) {
        // In this method, we can pass in a plan. It will reset box and level information
        Node initialNode = plan.getFirst().parent;
        this.agentRow = initialNode.agentRow;
        this.agentCol = initialNode.agentCol;
        updateBoxes(initialNode);
    }
    private void updateBoxes(Node node){
        for (int row = 0; row < Level.getInstance().MAX_ROW; row++) {
            for (int col = 0; col < Level.getInstance().MAX_COL; col++) {
                if (node.boxes[row][col] != null) {
                    Box box =  node.boxes[row][col];
                    box.setRow(row) ;
                    box.setCol(col);
                }
            }
        }
    }

    private void sendResponse(char receiver, Message response){
        MsgHub.getInstance().sendMessage(receiver,response);
    }

    public void receiveMessage(Message message){
        MsgHub msgHub = MsgHub.getInstance();
        MsgContent content;
        switch (message.getType()){
            case announce: // for now: announcement of solution
                // check if there is a conflict
                // if yes, send back request
                LinkedList<Node> otherAgentSolution = message.getContent().getContent();

                ArrayList<ResourceRequest> otherAgentResourceRequest = message.getContent().getResourceRequests();
                ArrayList<ResourceRequest> conflictingRessources = new ArrayList<>();
                //TODO this might not be enough as we are not reserving for the full time but only at one timestep
                for (ResourceRequest request: thisAgentResourceRequests){//Check this agents requests against another agents
                    if(otherAgentResourceRequest.contains(request)){
                        conflictingRessources.add(request);
                    }
                }

                int solutionStart = message.getContentStart();
                content = new MsgContent(otherAgentSolution);
                Message response = new Message(MsgType.request, content, id); // Assuming no conflict
                if(conflictingRessources.size()>0){
                    int firstTimestepConflict = conflictingRessources.get(0).getTimestep();
                    int lastTimestepConflict = conflictingRessources.get(conflictingRessources.size()-1).getTimestep();
                    int waitTime = lastTimestepConflict-firstTimestepConflict+1;
                    for (int i = 0; i < waitTime;i++){
                        otherAgentSolution = padNoOpNodeAtIndex(otherAgentSolution, firstTimestepConflict-solutionStart);
                    }
                }

                Conflict conflict = checkForConflicts(otherAgentSolution,solutionStart); // Check for conflicts

                if(conflict!=null){ // Handle conflicts if they exist
                    //TODO: Hotfix for when agents are colliding at step 0
                    if (conflict.getTime() == 0 ){
                        otherAgentSolution = padNoOpNode(otherAgentSolution);
                         content = new MsgContent(otherAgentSolution);
                        response = new Message(MsgType.request, content, id);
                    }else {
                        loop:while (conflict != null) {
                            switch (conflict.getType()) {
                                case boxbox:
                                case agentbox:
                                case agentagent:
                                    //Make other agent wait one step
                                    //System.err.println(this.getId() + " tries to make " + otherAgentSolution.getFirst().agentId + " to pad NOOP ");
                                    otherAgentSolution = makeOtherAgentWait(otherAgentSolution);
                                    timeStepPlusOne(otherAgentResourceRequest);//Timeshift the resourceRequests as well
                                    conflict = checkForConflicts(otherAgentSolution,solutionStart);
                                    content = new MsgContent(otherAgentSolution);
                                    response = new Message(MsgType.request, content, id);
                                    break;
                                case agentInTheWay:
                                    // Create subgoalmoveouttheway and approve agents plan
                                    System.err.println("GoalMoveOutTheWay created for " + this.getId() +" after conflict with " + otherAgentSolution.getFirst().agentId);
                                    Goal moveOutTheWay = new GoalMoveOutTheWay(otherAgentSolution);
                                    moveOutTheWay.setPriority(1);
                                    moveOutTheWay.setAgent(this);
                                    planner.addGoal(moveOutTheWay);
                                    content = new MsgContent(otherAgentSolution);
                                    response = new Message(MsgType.await, content, id); // Return await, to make the other agent replan later
                                    break loop;
                                default:
                                    System.err.println("Could not determine the type of conflict");
                                    break;
                            }
                        }
                    }
                }else{//No conflict
				Message agree = new Message(MsgType.agree, null, id);
                msgHub.reply(message, agree);
				break;
					
				}
                msgHub.reply(message, response); // Send reply
                break;

            case request:
                GoalMessage msg = (GoalMessage) message;
                GoalFreeAgent goal = (GoalFreeAgent)msg.getGoal();
                if(goal.getBox().getBoxColor()==this.color){
                    //TODO maybe search for solution!
                    LinkedList<Node> proposedSolution = new LinkedList<>();
                    content = new MsgContent(proposedSolution);
                    Message proposeMessage = new GoalMessage(MsgType.propose,goal,content ,id);
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

    private Conflict checkForConflicts(LinkedList<Node> otherAgentSolution, int solutionStart) {

        ConflictDetector cd = new ConflictDetector(this);
        cd.addPlan(this.combinedSolution);

        return cd.checkPlan(otherAgentSolution, solutionStart);
    }

    private LinkedList<Node> makeOtherAgentWait(LinkedList<Node> oldSolution){
        return padNoOpNode(oldSolution);
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
    private LinkedList<Node> padNoOpNodeAtIndex(LinkedList<Node> oldSolution,int index) {
        LinkedList<Node> newSolution = new LinkedList<>(oldSolution);
        Node initialNode = oldSolution.get(index).parent;
        Node noOp = new Node(initialNode);
        noOp.setBoxes(initialNode.getBoxesCopy());
        noOp.agentRow = initialNode.agentRow;
        noOp.agentCol = initialNode.agentCol;
        noOp.action= new Command(Command.Type.NoOp, null,null);
        newSolution.add(index,noOp);
        return newSolution;
    }
    private void timeStepPlusOne(ArrayList<ResourceRequest> resourceRequests){
        for (ResourceRequest reguest: resourceRequests) {
            reguest.setTimestep(reguest.getTimestep()+1);
        }
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

    public Goal getLatestGoal() {
        return latestGoal;
    }

    public void setLatestGoal(Goal latestGoal) {
        this.latestGoal = latestGoal;
    }

    public void setLimitedRessources(HashSet<Vertex> limitedRessources) {
        this.limitedRessources = limitedRessources;
    }
}
