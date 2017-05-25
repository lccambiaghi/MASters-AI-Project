package communicationclient;

import java.io.*;
import java.util.*;

import communication.MsgHub;
import goal.Goal;
import heuristic.Heuristic;
import level.*;
import plan.Planner;

public class CommunicationClient {
    private BufferedReader in;
    private LevelParser levelParser;
    private Planner planner;
    private LevelAnalyzer levelAnalyzer;

    private CommunicationClient() throws IOException {
        in = new BufferedReader(new InputStreamReader(System.in));
    }

    private void solve() throws IOException {
        planner.searchingPhase(levelParser.getGraph());

        List<LinkedList<Node>> solutions = planner.getSolutions();

        String jointAction = "";
        String response = "";

        // If our plan succeeds, the server will output 'success' and terminate the client
        // Else if we send an inapplicable action, its response will contain false and the server will wait for another line
        // TODO: If our client wrongly believes that all goals are achieved, it will keep sending NoOps for all agents and the process will keep on forever.
        int error = -1;
        while(!response.contains("false")) {
            // build joint action and progress iterator of solutions
            jointAction = "[";
            Node n;
            for (int i = 0; i < solutions.size() - 1; i++) {
                n = solutions.get(i).pollFirst();
                if(n != null)
                    jointAction += n.action.toString() + ",";
                else
                    jointAction += "NoOp,";
            }
            n = solutions.get(solutions.size() - 1).pollFirst();
            if(n!=null)
                jointAction += n.action.toString() + "]";
            else
                jointAction += "NoOp]";
            // Place message in buffer
            System.out.println(jointAction);
            // Flush buffer
            System.out.flush();
            if(!jointAction.contains("Pull") || !jointAction.contains("Push") || !jointAction.contains("Move")){
                jointAction = "";
            }
            response = in.readLine();
            error++;
        }
        System.err.format("Server responded with %s to the inapplicable action: %s a timestep: %s\n", response, jointAction, error);
        // Here we could react after the response contains 'false'. Now we kill the client.
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
//            Heuristic heuristic = new Heuristic.Greedy();
          Heuristic heuristic = new Heuristic.WeightedAStar(5);
//          Heuristic heuristic = new Heuristic.AStar();
            Strategy strategy = new StrategyBestFirst(heuristic);

            client.levelParser = new LevelParser(strategy,true);
            client.levelParser.readMap();

            client.levelAnalyzer = new LevelAnalyzer(client.levelParser.getGraph());
//            client.levelAnalyzer.analyzeWalls();
            client.levelAnalyzer.assignBoxesToCells();
            //TODO Specify what comparator the queue should use.

            PriorityQueue<Goal> priorityGoals = client.levelAnalyzer.createInitialGoals();

            client.planner = new Planner(priorityGoals);

            //TODO: This is a hotfix. We don't like that agents have a reference to the planner. They should might communicate to the planner instead.
            for (List<Agent> agentList: Level.getInstance().getAgentsColorMap().values()) {
                for (Agent a: agentList) {
                    a.setPlanner(client.planner);

                }
            }

            //MsgHub.createInstance(Level.getInstance());
            MsgHub.createInstance();

            client.solve();

        } catch (IOException ex) {
            System.err.println("IOException thrown!");
        }
    }
}
