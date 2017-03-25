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
         * @param id : Agent id
         * @param color : Agent color
         * @param msghub : shared instance of msghub
         */
        public Agent(char id, String color, MsgHub msgHub, Strategy strategy, BufferedReader serverMessages) {
            System.err.println("Agent " + id + " with color " + color + " using strategy " + strategy.toString() + " created");
            _msgHub = msgHub;
            _color = color;
            _id = id;
            _strategy = strategy;
        
            try {
                setUpInitialState(serverMessages);
            } catch (IOException e) {
                System.err.println("Could not create agent");
            }
        }

        /**
         * Logic for agent actions
         * @return string with commands for agent
         */
        public String act() {
            return "Stub commands";
        }

        private char getId() {
            return _id;
        }

        private Node getInitialState() {
            return _initialState;
        }

        private void setInitialState(Node state) {
            _initialState = state;
        }

        private void setUpInitialState(BufferedReader serverMessages) throws IOException {
            String line;

            // Skip lines specifying colors
            while ((line = in.readLine()).matches("^[a-z]+:\\s*[0-9A-Z](,\\s*[0-9A-Z])*\\s*$")) { }

            int row = 0;
            boolean agentFound = false;
            setInitialState(new Node(null));

            // Read lines specifying level layout
            while (!line.equals("")) {
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
                    } else if (chr == getId()) { // Agent.
                        if (agentFound) {
                            // other agents are considered walls
                            getInitialState().walls[row][col] = true;
                        } else {
                            System.err.println("Error, read invalid level character: " + (int) chr);
                            System.exit(1);
                        }
                        agentFound = true;
                        getInitialState().agentRow = row;
                        getInitialState().agentCol = col;
                    }
                }
                line = serverMessages.readLine();
                row++;
            }
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

    public CommunicationClient(Strategy strategy) throws IOException {
        readMap(strategy);
    }

    /**
     * Reads the map into memory and creates agents with the
     * shared instance of the msgHub
     */
    private void readMap(Strategy strategy) throws IOException {
        Map<Character, String> colors = new HashMap<Character, String>();
        String line, color;

        // Read lines specifying colors
        while ((line = in.readLine()).matches("^[a-z]+:\\s*[0-9A-Z](,\\s*[0-9A-Z])*\\s*$")) {
            line = line.replaceAll("\\s", "");
            color = line.split( ":" )[0];

            for (String id : line.split(":")[1].split(","))
                colors.put(id.charAt(0), color);
        }

        // Read lines specifying level layout
        while (!line.equals("")) {
            for (int i = 0; i < line.length(); i++) {
                char id = line.charAt(i);
                if ('0' <= id && id <= '9') {
                    agents.add(new Agent(id, colors.get(id), msgHub, strategy, in));
                }
            }

            line = in.readLine();
        }
    }

    public boolean update() throws IOException {
        String jointAction = "[";

        for (int i = 0; i < agents.size() - 1; i++)
            jointAction += agents.get(i).act() + ",";

        jointAction += agents.get(agents.size() - 1).act() + "]";

        // Place message in buffer
        System.out.println(jointAction);

        // Flush buffer
        System.out.flush();

        // Disregard these for now, but read or the server stalls when its output buffer gets filled!
        String percepts = in.readLine();
        if (percepts == null)
            return false;

        return true;
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
            while (client.update()) {};

        } catch (IOException e) {
            System.err.println("Could not run the client");
        }
    }
}
