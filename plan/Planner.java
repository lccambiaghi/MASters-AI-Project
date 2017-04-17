package plan;

import communication.Message;
import communication.MsgType;
import communicationclient.Agent;
import communicationclient.Node;
import communicationclient.Strategy;
import goal.Goal;
import level.Box;
import level.CharCell;
import level.Level;

import java.util.*;

/**
 * Analysis phase
 * Planning phase
 * Searching phase
 */
public class Planner {

    private List<Agent> agents;

    private Strategy strategy;

    private List<LinkedList<Node>> solutions;

    public Planner(List<Agent> agentList) {
        this.agents = agentList;
    }

    public void analysisPhase() {
        Level level = Level.getInstance();

        // assign a box to each charCell
        HashSet<CharCell> charCells = level.getAllCharCells();
        for (CharCell cc: charCells) {
            HashSet<Box> goalBoxes = level.getBoxesByChar(Character.toUpperCase(cc.getLetter()));

            Box closest = cc.getClosestBox(goalBoxes);

            cc.setAssignedBox(closest);
            closest.setDestination(cc);
        }
    }

    public void planningPhase(){
        for (Agent agent : agents) {
            ArrayDeque<Goal> agentPlan = agent.plan();

            //Message announcement = new Message(MsgType.inform, plan, agent.getId());
            //agent.broadcastSolution(announcement);

            //agent.getComplaints(announcement);

        }
    }

    public void searchingPhase() {

        solutions = new ArrayList<>();

        for (int i=0; i< agents.size(); i++) {
            Agent agent = agents.get(i);
            LinkedList<Node> agentSolution = agent.search();
            if (agentSolution == null) {
                // TODO agent is stuck

                System.err.println(this.strategy.searchStatus());
                System.err.println("Agent " + agents.get(i).getId() + " is unable to complete his subgoals.");
                System.exit(0);
            } else {
                System.err.println("\nSummary for " + this.strategy.toString() + " for agent " + agents.get(i).getId() + ":");
                System.err.println("Found solution of length " + agentSolution.size());
                System.err.println(this.strategy.searchStatus());

                // agentSolution found

                Message solutionAnnouncement = new Message(MsgType.inform, agentSolution, agent.getId());

                agent.broadcastSolution(solutionAnnouncement);

                agent.evaluateRequests(solutionAnnouncement);

                agentSolution = agent.getCombinedSolution();

                solutions.add(agentSolution);

            }
        }


    }

    public List<LinkedList<Node>> getSolutions() {
        return solutions;
    }

    public void setStrategy(Strategy strategy) {
        this.strategy = strategy;
    }
}
