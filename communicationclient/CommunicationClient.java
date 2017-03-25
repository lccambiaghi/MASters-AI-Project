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

        /**
         * Agent constructor
         * @param id : Agent id
         * @param color : Agent color
         * @param msghub : shared instance of msghub
         */
        public Agent(char id, String color, MsgHub msgHub, Strategy strategy) {
            System.err.println("Agent " + id + " with color " + color + " using strategy " + strategy.toString() + " created");
            _msgHub = msgHub;
            _color = color;
            _id = id;
            _strategy = strategy;
        }

        /**
         * Logic for agent actions
         * @return string with commands for agent
         */
        public String act() {
            return "Stub commands";
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

    private BufferedReader in = new BufferedReader(new InputStreamReader( System.in));
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
                if ('0' <= id && id <= '9')
                    agents.add(new Agent(id, colors.get(id), msgHub, strategy));
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
            // Got nowhere to write to probably
            System.err.println("Could not run the client");
        }
    }
}
