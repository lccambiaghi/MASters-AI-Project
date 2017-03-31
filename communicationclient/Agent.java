package communicationclient;

import level.Box;
import level.Color;
import level.Goal;
import level.Level;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * Created by salik on 31-03-2017.
 */
public class Agent {
    private MsgHub _msgHub;
    private Color _color;
    private char _id;
    private Strategy _strategy;
    private Node _initialState;
    private ArrayDeque<Goal> subGoals;
    private ArrayList<Command> solutionCommands;

    /**
     * Agent constructor
     * Sets up the initial state for the agent
     * @param id : Agent id
     * @param color : Agent color
     * @param msgHub : shared instance of msghub
     */
    public Agent(char id, Color color, MsgHub msgHub, Level level, Strategy strategy) {
        System.err.println("Agent " + id + " with color " + color + " created");
        subGoals = new ArrayDeque<>();
        solutionCommands = new ArrayList<>();
        _msgHub = msgHub;
        _color = color;
        _id = id;
        _strategy = strategy;
//        setUpInitialState(level);//Todo change to Level and only do once
    }
    public void addSubGoal(Goal subGoal){
        subGoals.add(subGoal);
    }
    public ArrayList<Command> getCommands(){
        return solutionCommands;
    }
    /**
     * Search for solution for agent
     * @return LinkedList with nodes for agent
     */
    public LinkedList<Node> search() throws IOException {
        System.err.println("Agent " + getId() + " started search with strategy " + _strategy.toString());
        _strategy.addToFrontier(getInitialState());

        int iterations = 0;
        while (true) {
            if (iterations == 1000) {
                System.err.println(_strategy.searchStatus());
                iterations = 0;
            }

            if (_strategy.frontierIsEmpty()) {
                return null;
            }
            Node leafNode = _strategy.getAndRemoveLeaf();

            if (leafNode.isGoalState()) {
                LinkedList<Node> plan = leafNode.extractPlan();
                setInitialState(plan.getLast());//Update initial state to where we end after this subgoal.
                return plan;
            }

            _strategy.addToExplored(leafNode);
            for (Node n : leafNode.getExpandedNodes()) { // The list of expanded nodes is shuffled randomly; see Node.java.
                if (!_strategy.isExplored(n) && !_strategy.inFrontier(n)) {
                    _strategy.addToFrontier(n);
                }
            }
            iterations++;
        }
    }

    public char getId() {
        return _id;
    }

//    private Strategy getStrategy() {
//        return _strategy;
//    }

    private Node getInitialState() {
        return _initialState;
    }

    private void setInitialState(Node state) {
        _initialState = state;
    }

    public Color getColor() {
        return _color;
    }

    /**
     * Sets up the initial state for the Agent
     * reads the map from serverMessages param.
     * All other agents and different colored boxes
     * are considered walls and other goals are
     * considered free spaces.
     * @param level
     * @throws IOException
     */
    public void setUpInitialState(Level level) {
        System.err.println("Setting up initial state for agent " + getId());
        Node initialNode = new Node(subGoals.pop(),_color);
        HashSet<Box> allBoxes = level.getAllBoxes();

        for (Box b: allBoxes) {
            initialNode.addBox(b);
        }
        setInitialState(initialNode);
        System.err.println("Initial state for agent " + getId() + " was successfully set up");
        System.err.println(" ");
    }

    /**
     * Posts msgs to the msgHub
     */
    private void postMsg() {
        System.err.println("~~ Agent: " + _id + " posted an msg");
    }

    /**
     * Reads msgs from the msgHub
     */
    private void readMsg() {
        System.err.println("~~ Agent: " + _id + " read an msg");
    }
}