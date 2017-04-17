package communicationclient;

import goal.Goal;
import goal.GoalBoxToChar;
import level.*;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * Created by salik on 31-03-2017.
 */
public class Agent {
    private MsgHub msgHub;
    private Color color;
    private char id;
    private Strategy strategy;
    private Node initialState;
    private ArrayDeque<Goal> subGoals;
    private ArrayList<Command> solutionCommands;
    private int agentRow;
    private int agentCol;
    private LinkedList<Node> combinedSolution;

    /**
     * Agent constructor
     * Sets up the initial state for the agent
     * @param id : Agent id
     * @param color : Agent color. if Single Agent level, color is default to blue
     */
    public Agent(char id, Color color, Strategy strategy) {
//        System.err.println("Agent " + id + " created");
        this.subGoals = new ArrayDeque<>();
        //this.solutionCommands = new ArrayList<>();
        this.combinedSolution = new LinkedList<>();
        //this.msgHub = msgHub;
        this.color = color;
        this.id = id;
        this.strategy = strategy;
    }

    public void addSubGoal(Goal subgoal){
        this.subGoals.addLast(subgoal);
    }

    public ArrayList<Command> getCommands(){
        return this.solutionCommands;
    }

    public void plan(){
        System.err.println("Agent " + this.id + " started planning");

        HashSet<Box> agentBoxes = Level.getInstance().getBoxesByColor(this.color);

        for (Box b : agentBoxes) {
            if (b.getDestination() != null) { // if box has a goal assigned
                GoalBoxToChar goal = new GoalBoxToChar(b, b.getDestination());
                // TODO add to a list of goals
                goal.refine();
                for(Goal subgoal : goal.getSubgoals()){
                    addSubGoal(subgoal);
                }
                //CharCell toBoxSubGoal = new CharCell(b.getCol(), b.getRow(), GoalType.MoveToBox);
                //addSubGoal(toBoxSubGoal);
                //addSubGoal(b.getDestination());
            }
        }
        System.err.println("Agent " + this.id + " planned " + subGoals.size() + "subgoals");
    }

    /**
     * Search for solution for agent
     * @return LinkedList with nodes for agent
     */
    public LinkedList<Node> search() throws IOException {
        System.err.println("Agent " + getId() + " started search with strategy " + this.strategy.toString());
        Goal firstSub = this.subGoals.peekFirst();
        Node initialNode = new Node(firstSub, this.color);
        HashSet<Box> allBoxes = Level.getInstance().getAllBoxes();
        
        for (Box b: allBoxes) {
            initialNode.addBox(b);
        }

        initialNode.agentRow = this.agentRow;
        initialNode.agentCol = this.agentCol;
        setInitialState(initialNode);

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
                        Node newStart = new Node(subGoals.peekFirst(), this.color);
                        newStart.setBoxes(goalState.getBoxesCopy());
                        newStart.agentCol = goalState.agentCol;
                        newStart.agentRow = goalState.agentRow;
                        setInitialState(newStart);//Update initial state to where we end after this subgoal.
                    }else{
                        Node newStart = new Node(subGoals.peekFirst(), this.color);
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

    public void setMsgHub(MsgHub msgHub) {
        this.msgHub = msgHub;
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

    /**
     * Posts msgs to the msgHub
     */
    private void postMsg() {
        System.err.println("~~ Agent: " + this.id + " posted an msg");
    }

    /**
     * Reads msgs from the msgHub
     */
    private void readMsg() {
        System.err.println("~~ Agent: " + this.id + " read an msg");
    }
}
