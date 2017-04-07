package communicationclient;

import java.io.*;
import java.util.*;

import heuristic.Heuristic;
import heuristic.HeuristicHelper;
import level.*;
import level.Box;

public class CommunicationClient {
    private BufferedReader in;
    private List<Agent> agents = new ArrayList<>();
    private MsgHub msgHub = new MsgHub();
    private Strategy strategy;
    private Level level;

    public CommunicationClient() throws IOException {
        in = new BufferedReader(new InputStreamReader(System.in));
    }

    /**
     *
     */
    public boolean update() throws IOException {
        // assign a box to each charGoal
        HashSet<Goal> charGoals = this.level.getAllGoals();
        for (Goal g: charGoals) {
            HashSet<Box> goalBoxes = this.level.getBoxesByChar(Character.toUpperCase(g.getGoalChar()));
            // assign best box to the goal
            Box assigned = new ArrayList<>(goalBoxes).get(0);
            for (Box b: goalBoxes) {
                // if distance to goal is less, assign
                int assignedDistance = HeuristicHelper.manhattanDistance(
                        assigned.getRow(), assigned.getCol(), g.getRow(), g.getCol());
                int bDistance = HeuristicHelper.manhattanDistance(
                        b.getRow(), b.getCol(), g.getRow(), g.getCol());
                if (bDistance < assignedDistance)
                    assigned = b;
            }

            g.setGoalBox(assigned);
            assigned.setBoxGoal(g);
        }

        // each agent plans his subgoals
        for (Agent agent : agents) {
            agent.plan();
        }

        // each agent looks for the solution
        LinkedList<Node> agentSolution;
        List<LinkedList<Node>> solutions = new ArrayList<>();
        for (int i=0; i< agents.size(); i++) {
            agentSolution = agents.get(i).search();
            if (agentSolution == null) {
                System.err.println(this.strategy.searchStatus());
                System.err.println("Unable to solve level.");
                System.exit(0);
            } else {
                System.err.println("\nSummary for " + this.strategy.toString() + " for agent " + agents.get(i).getId() + ":");
                System.err.println("Found solution of length " + agentSolution.size());
                System.err.println(this.strategy.searchStatus());
                solutions.add(agentSolution);
            }
        }

        String jointAction = "";
        String response = "";
        while(!response.contains("false")) {
            // build joint action and progress iterator of solutions
            jointAction = "[";
            Node n;
            for (int i = 0; i < agents.size() - 1; i++) {
                n = solutions.get(i).pollFirst();
                if(n!=null)
                    jointAction += n.action.toString() + ",";
                else
                    jointAction += "NoOp,";
            }
            n = solutions.get(agents.size() - 1).pollFirst();
            if(n!=null)
                jointAction += n.action.toString() + "]";
            else
                jointAction += "NoOp]";
            // Place message in buffer
            System.out.println(jointAction);
            // Flush buffer
            System.out.flush();
            response = in.readLine();
        }
        System.err.format("Server responsed with %s to the inapplicable action: %s\n", response, jointAction);
        //System.err.format("%s was attempted in \n%s\n", act, n.toString());
        return false;

    }

    private Strategy getStrategy() {
        return this.strategy;
    }

    public void setStrategy(Strategy strategy) {
        this.strategy = strategy;
    }

    /**
     * Reads the map into memory and creates agents with the
     * shared instance of the msgHub.
     * The map is converted from the BufferedReader to an ArrayList
     * to avoid any problems when manipulating the map.
     */
    private void readMap() throws IOException {
        HashMap<Character, Color> colors = new HashMap<>();
        Color color;
        int MAX_COL = 0;
        int MAX_ROW = 0;
        int row = 0;
        ArrayList<String> map = new ArrayList<>();
        String line = in.readLine();
        
	    while(!line.equals("")) {
            map.add(line);
            if(line.length() > MAX_COL) MAX_COL = line.length();
            line = in.readLine();
            row++;
            MAX_ROW = row;
        }

        this.level = Level.createInstance(MAX_ROW, MAX_COL);

        System.err.println(" ");
        System.err.println("Printing scanned map");

        for (String lineInMap: map) {
            System.err.println(lineInMap);
	    }

        System.err.println(" ");

        row = 0;
        boolean colorLevel = false;
        for (String lineInMap: map) {
            // if line is a color declaration, MA level -> colors get mapped
            if (lineInMap.matches("^[a-z]+:\\s*[0-9A-Z](,\\s*[0-9A-Z])*\\s*$")) {
                colorLevel = true;
                lineInMap = lineInMap.replaceAll("\\s", "");
                color = Color.valueOf(lineInMap.split( ":" )[0]);
                for (String id : lineInMap.split(":")[1].split(","))
                    colors.put(id.charAt(0), color);
            } else {
                // if SA, map of colors is empty -> all colors set to blue.
                for (int col = 0; col < lineInMap.length(); col++) {
                    char chr = lineInMap.charAt(col);
                    if (chr == '+') { // Wall.
                        this.level.setWall(true, row, col);
                    } else if ('A' <= chr && chr <= 'Z') { // Box.
                        Box box = new Box(col, row, chr, Color.blue);
                        if(colorLevel) {
                            Color boxColor = colors.get(chr);
                            box.setColor(boxColor);
                        }
                        this.level.addBox(box);
                    } else if ('a' <= chr && chr <= 'z') { // Goal.
                        Goal goal = new Goal(col, row, chr);
                        this.level.addCharGoal(goal);
                    } else if (chr == ' ') {
                        // Free space.
                    }else if ('0' <= chr && chr <= '9') {
                        Agent newAgent = new Agent(chr, Color.blue, msgHub, this.strategy);
                        if(colorLevel) {
                            Color agentColor = colors.get(chr);
                            newAgent.setColor(agentColor);
                        }
                        newAgent.setAgentRow(row);
                        newAgent.setAgentCol(col);
                        agents.add(newAgent);
                        System.err.println("Agent " + newAgent.getId() + " created, Color is " + newAgent.getColor().toString());
                    }
                }
                row++;
            }
        }

        System.err.println("*--------------------------------------*");

    }

    /**
     * Starts the client and runs a infinite loop
     */
    public static void main(String[] args) {

        System.err.println("*--------------------------------------*");
        System.err.println("|     CommunicationClient started      |");
        System.err.println("*--------------------------------------*");
        try {
            CommunicationClient client = new CommunicationClient();
            Heuristic heuristic = new Heuristic.WeightedAStar(5);
            Strategy strategy = new StrategyBestFirst(heuristic);
            client.setStrategy(strategy);
            client.readMap();
            while(client.update())
                // when update returns false, we need to replan
                // TODO update beliefs
                ;

        } catch (IOException ex) {
            System.err.println("IOException thrown!");
        }
    }
}
