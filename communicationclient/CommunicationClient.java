package communicationclient;

import java.io.*;
import java.util.*;
import communicationclient.Strategy.*;

public class CommunicationClient {

    public class Agent {
        private MsgHub _msgHub;
        private String _color;
        private char _id;
        private Strategy _strategy;
        private Node _initialState;

        /**
         * Agent constructor
         * Sets up the initial state for the agent
         * @param id : Agent id
         * @param color : Agent color
         * @param msghub : shared instance of msghub
         */
        public Agent(char id, String color, MsgHub msgHub, Strategy strategy, ArrayList<String> map) {
            System.err.println("Agent " + id + " with color " + color + " using strategy " + strategy.toString() + " created");
            _msgHub = msgHub;
            _color = color;
            _id = id;
            _strategy = strategy;

            setUpInitialState(map);
        }

        /**
         * Search for solution for agent
         * @return LinkedList with nodes for agent
         */
        public LinkedList<Node> search() throws IOException {
            Strategy strategy = getStrategy();
            System.err.println("Agent " + getId() + " started search with strategy " + strategy.toString());
            strategy.addToFrontier(getInitialState());

            int iterations = 0;
            while (true) {
                if (iterations == 1000) {
                    System.err.println(strategy.searchStatus());
                    iterations = 0;
                }

                if (strategy.frontierIsEmpty()) {
                    return null;
                }

                Node leafNode = strategy.getAndRemoveLeaf();

                System.err.println("Walls in leafNode " + leafNode.walls[2][1]);
                System.err.println("Agent location row: " + leafNode.agentRow + " col: " + leafNode.agentCol);

                if (leafNode.isGoalState()) {
                    return leafNode.extractPlan();
                }

                strategy.addToExplored(leafNode);
                for (Node n : leafNode.getExpandedNodes()) { // The list of expanded nodes is shuffled randomly; see Node.java.
                    if (!strategy.isExplored(n) && !strategy.inFrontier(n)) {
                        strategy.addToFrontier(n);
                    }
                }
                iterations++;
            }
        }

        private char getId() {
            return _id;
        }

        private Strategy getStrategy() {
            return _strategy;
        }

        private Node getInitialState() {
            return _initialState;
        }

        private void setInitialState(Node state) {
            _initialState = state;
        }

        /**
         * Sets up the initial state for the Agent
         * reads the map from serverMessages param.
         * All other agents and different colored boxes
         * are considered walls and other goals are
         * considered free spaces.
         * @param serverMessages
         * @throws IOException
         */
        private void setUpInitialState(ArrayList<String> map) {
            System.err.println("Setting up initial state for agent " + getId());
            int row = 0;

            for (String line: map) {

                // Skip lines specifying colors
                if (!line.matches("^[a-z]+:\\s*[0-9A-Z](,\\s*[0-9A-Z])*\\s*$")) {
                    setInitialState(new Node(null));

                    // Read lines specifying level layout
                    for (int col = 0; col < line.length(); col++) {
                        char chr = line.charAt(col);

                        if (chr == '+') { // Wall.
                            getInitialState().walls[row][col] = true;
                        } else if ('A' <= chr && chr <= 'Z') { // Box.
                            getInitialState().boxes[row][col] = chr;
                        } else if ('a' <= chr && chr <= 'z') { // Goal.
                            getInitialState().goals[row][col] = chr;
                        } else if (chr == ' ') {
                            // Free space.
                        } else if ('0' <= chr && chr <= '9') { // Agent.
                            if (chr == getId()) {
                                getInitialState().agentRow = row;
                                getInitialState().agentCol = col;
                            } else {
                                // other agents are considered walls
                                getInitialState().walls[row][col] = true;
                            }
                        }
                    }
                    row++;
                }
            }
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

    private BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    private List<Agent> agents = new ArrayList< Agent >();
    private MsgHub msgHub = new MsgHub();
    private Strategy _strategy;

    public CommunicationClient(Strategy strategy) throws IOException {
        _strategy = strategy;
        readMap(strategy);
    }

    public boolean update() throws IOException {
        Strategy strategy = getStrategy();
        LinkedList<Node> solution;

        for (Agent agent : agents) {
            solution = agent.search();
            if (solution == null) {
                System.err.println(strategy.searchStatus());
                System.err.println("Unable to solve level.");
                System.exit(0);
            } else {
                System.err.println("\nSummary for " + strategy.toString());
                System.err.println("Found solution of length " + solution.size());
                System.err.println(strategy.searchStatus());

                for (Node n : solution) {
                    String act = n.action.toString();
                    System.out.println(act);
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
    private void readMap(Strategy strategy) throws IOException {
        Map<Character, String> colors = new HashMap<Character, String>();
        String line, color;

        line = in.readLine();

        ArrayList<String> map = new ArrayList<String>();
        while(!line.equals("")) {
            map.add(line);
            line = in.readLine();
        }

        System.err.println(" ");
        System.err.println("Printing scanned map");

        for (String lineInMap: map) {
            System.err.println(lineInMap);
        }

        System.err.println(" ");

        for (String lineInMap: map) {
            if (lineInMap.matches("^[a-z]+:\\s*[0-9A-Z](,\\s*[0-9A-Z])*\\s*$")) {
                lineInMap = lineInMap.replaceAll("\\s", "");
                color = lineInMap.split( ":" )[0];

                for (String id : lineInMap.split(":")[1].split(","))
                    colors.put(id.charAt(0), color);

            } else {
                for (int i = 0; i < lineInMap.length(); i++) {
                    char id = lineInMap.charAt(i);
                    if ('0' <= id && id <= '9') {
                        agents.add(new Agent(id, colors.get(id), msgHub, strategy, map));
                    }
                }
            }
        }
    }

    /**
     * Starts the client and runs a infinit loop until
     * client returns false
     */
    public static void main(String[] args) {
        Strategy strategyBFS = new StrategyBFS();

        System.err.println("*--------------------------------------*");
        System.err.println("|     CommunicationClient started      |");
        System.err.println("*--------------------------------------*");

        try {
            CommunicationClient client = new CommunicationClient(strategyBFS);
            client.update();
        } catch (IOException ex) {
            System.err.println("IOException thrown!");
        }
    }
}
