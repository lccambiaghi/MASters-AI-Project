package communicationclient;

import java.io.*;
import java.util.*;

import conflictsolver.ConflictDetector;
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
        ConflictDetector cf = new ConflictDetector();
        for (int i=0; i< agents.size(); i++) {
            agentSolution = agents.get(i).search();
            if (agentSolution == null) {
                System.err.println(this.strategy.searchStatus());
                System.err.println("Agent " + agents.get(i).getId() + " is unable to solve level.");
                System.exit(0);
            } else {
                System.err.println("\nSummary for " + this.strategy.toString() + " for agent " + agents.get(i).getId() + ":");
                System.err.println("Found solution of length " + agentSolution.size());
                System.err.println(this.strategy.searchStatus());
                int conflict = cf.checkPlan(agentSolution);
                int numConflicts = 0;
                while (conflict > -1){//Pad with NoOp
                    System.err.println("Conflict found at "+conflict);
                    Node n = agentSolution.getFirst();
                    Node noOp = new Node(null);
                    noOp.setBoxes(n.getBoxesCopy());
                    noOp.agentRow = n.agentRow;
                    noOp.agentCol = n.agentCol;
                    noOp.action= new Command(Command.Type.NoOp, n.action.dir1,n.action.dir2);
//                    agentSolution.add(conflict+1, noOp);
                    agentSolution.addFirst(noOp);
                    conflict = cf.checkPlan(agentSolution);
                }
                cf.addPlan(agentSolution);
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

            while(client.update())
                // when update returns false, we need to replan
                // TODO update beliefs
                ;

        } catch (IOException ex) {
            System.err.println("IOException thrown!");
        }
    }
}
