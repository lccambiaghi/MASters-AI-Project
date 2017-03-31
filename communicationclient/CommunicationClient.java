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

    private BufferedReader in;
    private List<Agent> agents = new ArrayList<>();
    private MsgHub msgHub = new MsgHub();
    private Strategy _strategy;
    private Level _level;

    public CommunicationClient() throws IOException {
        //For Debugging
//        FileInputStream fis = null;
//        fis = new FileInputStream("levels/SAsoko3_48.lvl");
//        in = new BufferedReader(new InputStreamReader(fis));
        //For live
        in = new BufferedReader(new InputStreamReader(System.in));

    }

    /**
     * Each agent starts to search for how to solve the level
     * and sends the actions to the server.
     *
     * Actions should be combined here when working with
     * levels with multi agents.
     */
    public boolean update() throws IOException {
//        _strategy = strategy;
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
//            agent.setUpInitialState(_level);
            solution = agent.search();
            if (solution == null) {
                System.err.println(_strategy.searchStatus());
                System.err.println("Unable to solve level.");
                System.exit(0);
            } else {
                System.err.println("\nSummary for " + _strategy.toString() + " for agent: " + agent.getId());
                System.err.println("Found solution of length " + solution.size());
                System.err.println(_strategy.searchStatus());

                for (Node n : solution) {
                    String act = n.action.toString();
//                    System.err.println(n);
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



    public void setStrategy(Strategy _strategy) {
        this._strategy = _strategy;
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
        ArrayList<String> map = new ArrayList<>();
        String line = in.readLine();
        while(!line.equals("")) {
            map.add(line);
            if(line.length() > MAX_COL) MAX_COL = line.length();
            line = in.readLine();
            row++;
            MAX_ROW = row;
        }

//        for (String line = in.readLine(); line != null; line = in.readLine()) {
//            map.add(line);
//            if(line.length() > MAX_COL) MAX_COL = line.length();
////            line = in.readLine();
//            row++;
//            MAX_ROW = row;
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
                        Agent newAgent = new Agent(chr, colors.get(chr), msgHub, _strategy);
                        newAgent.setAgentRow(row);
                        newAgent.setAgentCol(col);
                        agents.add(newAgent);
                    }
                }
                row++;
            }
        }
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
            client.update();
        } catch (IOException ex) {
            System.err.println("IOException thrown!");
        }
    }
}
