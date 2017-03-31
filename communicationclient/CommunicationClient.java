package communicationclient;

import java.io.*;
import java.util.*;
import communicationclient.Strategy.*;
import heuristic.Heuristic;
import jdk.nashorn.internal.runtime.Debug;
import level.*;
import level.Box;

import javax.swing.*;

public class CommunicationClient {

//    public class Agent {
//        private MsgHub _msgHub;
//        private String _color;
//        private char _id;
//        private Strategy _strategy;
//        private Node _initialState;
//
//        /**
//         * Agent constructor
//         * Sets up the initial state for the agent
//         * @param id : Agent id
//         * @param color : Agent color
//         * @param msghub : shared instance of msghub
//         */
//        public Agent(char id, String color, MsgHub msgHub, Strategy strategy, ArrayList<String> map) {
//            System.err.println("Agent " + id + " with color " + color + " using strategy " + strategy.toString() + " created");
//            _msgHub = msgHub;
//            _color = color;
//            _id = id;
//            _strategy = strategy;
//
//            setUpInitialState(map);
//        }
//
//        /**
//         * Search for solution for agent
//         * @return LinkedList with nodes for agent
//         */
//        public LinkedList<Node> search() throws IOException {
//            Strategy strategy = getStrategy();
//            System.err.println("Agent " + getId() + " started search with strategy " + strategy.toString());
//            strategy.addToFrontier(getInitialState());
//
//            int iterations = 0;
//            while (true) {
//                if (iterations == 1000) {
//                    System.err.println(strategy.searchStatus());
//                    iterations = 0;
//                }
//
//                if (strategy.frontierIsEmpty()) {
//                    return null;
//                }
//
//                Node leafNode = strategy.getAndRemoveLeaf();
//
//                if (leafNode.isGoalState()) {
//                    return leafNode.extractPlan();
//                }
//
//                strategy.addToExplored(leafNode);
//                for (Node n : leafNode.getExpandedNodes()) { // The list of expanded nodes is shuffled randomly; see Node.java.
//                    if (!strategy.isExplored(n) && !strategy.inFrontier(n)) {
//                        strategy.addToFrontier(n);
//                    }
//                }
//                iterations++;
//            }
//        }
//
//        private char getId() {
//            return _id;
//        }
//
//        private Strategy getStrategy() {
//            return _strategy;
//        }
//
//        private Node getInitialState() {
//            return _initialState;
//        }
//
//        private void setInitialState(Node state) {
//            _initialState = state;
//        }
//
//        /**
//         * Sets up the initial state for the Agent
//         * reads the map from serverMessages param.
//         * All other agents and different colored boxes
//         * are considered walls and other goals are
//         * considered free spaces.
//         * @param serverMessages
//         * @throws IOException
//         */
//        private void setUpInitialState(ArrayList<String> map) {
//            System.err.println("Setting up initial state for agent " + getId());
//            int row = 0;
//            setInitialState(new Node(null));
//
//            for (String line: map) {
//
//                // Skip lines specifying colors
//                if (!line.matches("^[a-z]+:\\s*[0-9A-Z](,\\s*[0-9A-Z])*\\s*$")) {
//
//                    // Read lines specifying level layout
//                    for (int col = 0; col < line.length(); col++) {
//                        char chr = line.charAt(col);
//
//                        /**
//                         * TODO: Need to set other boxes as walls.
//                         */
//
//                        if (chr == '+') { // Wall.
//                            getInitialState().walls[row][col] = true;
//                        } else if ('A' <= chr && chr <= 'Z') { // Box.
//                            getInitialState().boxes[row][col] = chr;
//                        } else if ('a' <= chr && chr <= 'z') { // Goal.
//                            getInitialState().goals[row][col] = chr;
//                        } else if (chr == ' ') {
//                            // Free space.
//                        } else if ('0' <= chr && chr <= '9') { // Agent.
//                            if (chr == getId()) {
//                                getInitialState().agentRow = row;
//                                getInitialState().agentCol = col;
//                            } else {
//                                // other agents are considered walls
//                                getInitialState().walls[row][col] = true;
//                            }
//                        }
//                    }
//                    row++;
//                }
//            }
//            System.err.println("Initial state for agent " + getId() + " was successfully set up");
//            System.err.println(" ");
//        }
//
//        /**
//         * Posts msgs to the msgHub
//         */
//        private void postMsg() {
//            System.err.println("~~ Agent: " + _id + " posted an msg");
//        }
//
//        /**
//         * Reads msgs from the msgHub
//         */
//        private void readMsg() {
//            System.err.println("~~ Agent: " + _id + " read an msg");
//        }
//    }

    private BufferedReader in;
    private List<Agent> agents = new ArrayList<>();
    private MsgHub msgHub = new MsgHub();
    private Strategy _strategy;
    private Level _level;

    public CommunicationClient() throws IOException {
        //For Debugging
        FileInputStream fis = null;
        fis = new FileInputStream("levels/SAsoko1_12.lvl");
        in = new BufferedReader(new InputStreamReader(fis));
        //For live
//        in = new BufferedReader(new InputStreamReader(System.in));

    }

    /**
     * Each agent starts to search for how to solve the level
     * and sends the actions to the server.
     *
     * Actions should be combined here when working with
     * levels with multi agents.
     */
    public boolean update(Strategy strategy) throws IOException {
        _strategy = strategy;
        LinkedList<Node> solution;

        for (Agent agent : agents) {
            //Assign all goals a box
            HashSet<Goal> allGoals = _level.getAllGoals();
            for (Goal g: allGoals) {
                HashSet<Box> goalBoxes = _level.getBoxesByChar(Character.toUpperCase(g.getGoalChar()));
                for (Box b: goalBoxes) {
                    g.setGoalBox(b);
                    b.setBoxGoal(g);
                }
            }
            //Start on agent subgoals
            HashSet<Box> agentBoxes =_level.getBoxesByColor(agent.getColor());

            for (Box b: agentBoxes) {
                //Only go to boxes that have a goal
                if(b.getBoxGoal()!=null){
                    Goal subGoal= new Goal(b.getCol(),b.getRow(),GoalType.AgentToBox);
                    agent.addSubGoal(subGoal);
                    agent.addSubGoal(b.getBoxGoal());
                }
            }
            agent.setUpInitialState(_level);
            agent.
            solution = agent.search();
            if (solution == null) {
                System.err.println(strategy.searchStatus());
                System.err.println("Unable to solve level.");
                System.exit(0);
            } else {
                System.err.println("\nSummary for " + strategy.toString() + " for agent: " + agent.getId());
                System.err.println("Found solution of length " + solution.size());
                System.err.println(strategy.searchStatus());

                for (Node n : solution) {
                    String act = n.action.toString();
                    System.out.println(act);
                    //System.out.println("[Move(E), Move(E)]");
                    String response = in.readLine();
                    if (response.contains("false")) {
                        System.err.format("Server responsed with %s to the inapplicable action: %s\n", response, act);
                        System.err.format("%s was attempted in \n%s\n", act, n.toString());
                        break;
                    }
                }
            }
        }
        return true;
    }

    private Strategy getStrategy() {
        return _strategy;
    }

    /**
     * Reads the map into memory and creates agents with the
     * shared instance of the msgHub.
     * The map is converted from the BufferedReader to an ArrayList
     * to avoid any problems when manipulating the map.
     */
    private void readMap() throws IOException {
        HashMap<Character, Color> colors = new HashMap<>();
//        String line;//, color;
        Color color;
        int MAX_COL = 0;
        int MAX_ROW = 0;
        int row = 0;

//        line = in.readLine();
        ArrayList<String> map = new ArrayList<>();
        for (String line = in.readLine(); line != null; line = in.readLine()) {
            map.add(line);
            if(line.length() > MAX_COL) MAX_COL = line.length();
//            line = in.readLine();
            row++;
            MAX_ROW = row;
        }
//        while(line !=null && !line.equals("")) {
//            map.add(line);
//            if(line.length() > MAX_COL) MAX_COL = line.length();
//            line = in.readLine();
//            row++;
//			MAX_ROW = row;
//        }
        _level = Level.createInstance(MAX_ROW,MAX_COL);

        System.err.println(" ");
        System.err.println("Printing scanned map");

        for (String lineInMap: map) {
            System.err.println(lineInMap);
        }

        System.err.println(" ");

        /**
         * TODO: Create boxes and goals here from box and goals class
         */

        row = 0;
        for (String lineInMap: map) {
            if (lineInMap.matches("^[a-z]+:\\s*[0-9A-Z](,\\s*[0-9A-Z])*\\s*$")) {
                lineInMap = lineInMap.replaceAll("\\s", "");
                color = Color.valueOf(lineInMap.split( ":" )[0]);

                for (String id : lineInMap.split(":")[1].split(","))
                    colors.put(id.charAt(0), color);

            } else {
                for (int col = 0; col < lineInMap.length(); col++) {
                    char chr = lineInMap.charAt(col);
                    if (chr == '+') { // Wall.
                        _level.setWall(true,row,col);
                    } else if ('A' <= chr && chr <= 'Z') { // Box.
                        Color boxColor = colors.get(chr);
                        Box box = new Box(col,row,chr,boxColor);
                        _level.addBox(box);
//                        getInitialState().boxes[row][col] = chr;
                    } else if ('a' <= chr && chr <= 'z') { // Goal.
                        Goal goal = new Goal(col,row,chr);
                        _level.addCharGoal(goal);
//                        getInitialState().goals[row][col] = chr;
                    } else if (chr == ' ') {
                        // Free space.
                    }else if ('0' <= chr && chr <= '9') {
                        agents.add(new Agent(chr, colors.get(chr), msgHub, _level, _strategy));
                    }
                }
                row++;
            }
        }
    }

    /**
     * Starts the client and runs a infinit loop
     */
    public static void main(String[] args) {

        System.err.println("*--------------------------------------*");
        System.err.println("|     CommunicationClient started      |");
        System.err.println("*--------------------------------------*");
        try {
            CommunicationClient client = new CommunicationClient();
            client.readMap();
            Heuristic heuristic = new Heuristic.Greedy();
            Strategy strategy = new StrategyBestFirst(heuristic);
            client.update(strategy);
        } catch (IOException ex) {
            System.err.println("IOException thrown!");
        }
    }
}
