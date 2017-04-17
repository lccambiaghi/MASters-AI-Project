package communicationclient;

import java.io.*;
import java.util.*;

import level.CharCell;
import heuristic.Heuristic;
import heuristic.HeuristicHelper;
import level.*;
import level.Box;

public class CommunicationClient {
    private BufferedReader in;
    private List<Agent> agents;
    private MsgHub msgHub = new MsgHub();
    private Strategy strategy;
    private LevelParser levelParser;
    private Level level;

    public CommunicationClient() throws IOException {
        in = new BufferedReader(new InputStreamReader(System.in));
    }

    /**
     *
     */
    public boolean update() throws IOException {
        this.level = Level.getInstance();

        // assign a box to each charCell
        HashSet<CharCell> charCells = this.level.getAllCharCells();
        for (CharCell cc: charCells) {
            HashSet<Box> goalBoxes = this.level.getBoxesByChar(Character.toUpperCase(cc.getLetter()));

            Box closest = cc.getClosestBox(goalBoxes);

            cc.setAssignedBox(closest);
            closest.setDestination(cc);
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
            client.levelParser = new LevelParser(strategy,true);
            client.levelParser.readMap();
            client.agents = client.levelParser.getAgents();
            //            client.readMap();
            while(client.update())
                // when update returns false, we need to replan
                // TODO update beliefs
                ;

        } catch (IOException ex) {
            System.err.println("IOException thrown!");
        }
    }
}
