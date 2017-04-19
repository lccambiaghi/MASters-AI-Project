package plan;

import communication.Message;
import communication.MsgType;
import communicationclient.Agent;
import communicationclient.Node;
import communicationclient.Strategy;
import goal.Goal;
import heuristic.CharCellComparator;
import level.Box;
import level.CharCell;
import level.Level;
import level.LevelParser;

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
    private PriorityQueue<CharCell> charCellPriorityQueue;
    private LevelParser levelParser;

    public Planner(List<Agent> agentList, PriorityQueue<CharCell> priorityQueue, LevelParser levelParser) {
        this.agents = agentList;
        this.charCellPriorityQueue = priorityQueue;
        this.levelParser = levelParser;
    }

    public void analysisPhase() {
        Level level = Level.getInstance();

        // assign a box to each charCell
        HashSet<CharCell> charCells = level.getAllCharCells();
        for (CharCell cc: charCells) {
            HashSet<Box> goalBoxes = level.getBoxesByChar(Character.toUpperCase(cc.getLetter()));

            Box closest = cc.getClosestBox(goalBoxes);

            cc.setAssignedBox(closest);
            List<Agent> agentPriorityQueue =levelParser.getAgentsByColorMap().get(closest.getBoxColor());
            for (Agent a: agentPriorityQueue) {
                closest.setAssignedAgent(a);//Will override and the closest agent will get the box
            }
            closest.setDestination(cc);
        }
    }

    public void planningPhase(){
        PriorityQueue<CharCell> tmp = new PriorityQueue<>(new CharCellComparator());
        while(!charCellPriorityQueue.isEmpty()){
            CharCell goal = charCellPriorityQueue.poll();
            Agent agent = goal.getAssignedBox().getAssignedAgent();
            agent.setNumberOfGoals(agent.getNumberOfGoals()+1);
            agent.plan(goal);//Plan for a single goal at a time
            tmp.add(goal);

                //Message announcement = new Message(MsgType.inform, plan, agent.getId());
                //agent.broadcastSolution(announcement);

                //agent.getComplaints(announcement);


        }
        charCellPriorityQueue = tmp;//Save all the charcells so they are not lost after poll

    }

    public void searchingPhase() {

        solutions = new ArrayList<>();
        HashSet<Agent> hasSearched = new HashSet<>();
        while(!charCellPriorityQueue.isEmpty()){
            CharCell goal = charCellPriorityQueue.poll();//Get the highest priority CharCell
//            Agent agent = levelParser.getAgentsByColorMap().get(goal.getAssignedBox().getBoxColor()).peek();//Get the agent that has the same color as the box that matches the goalcell and has the fewest goals
            Agent agent = goal.getAssignedBox().getAssignedAgent();
            if (hasSearched.contains(agent)) continue;
            hasSearched.add(agent);
            LinkedList<Node> agentSolution = agent.search();
            if (agentSolution == null) {
                // TODO agent is stuck

                System.err.println(this.strategy.searchStatus());
                System.err.println("Agent " + agent.getId() + " is unable to complete his subgoals.");
                System.exit(0);
            } else {
                System.err.println("\nSummary for " + this.strategy.toString() + " for agent " + agent.getId() + ":");
                System.err.println("Found solution of length " + agentSolution.size());
                System.err.println(this.strategy.searchStatus());
                agent.setNumberOfGoals(agent.getNumberOfGoals()-1);

                // agentSolution found

                Message solutionAnnouncement = new Message(MsgType.inform, agentSolution, agent.getId());

                agent.broadcastSolution(solutionAnnouncement);

                agent.evaluateRequests(solutionAnnouncement);

                agentSolution = agent.getCombinedSolution();

                solutions.add(agentSolution);
        }

//        for (int i=0; i< agents.size(); i++) {
//                    Agent agent = agents.get(i);
//                    LinkedList<Node> agentSolution = agent.search();
//                    if (agentSolution == null) {
//                        // TODO agent is stuck
//
//                        System.err.println(this.strategy.searchStatus());
//                        System.err.println("Agent " + agents.get(i).getId() + " is unable to complete his subgoals.");
//                        System.exit(0);
//                    } else {
//                        System.err.println("\nSummary for " + this.strategy.toString() + " for agent " + agents.get(i).getId() + ":");
//                        System.err.println("Found solution of length " + agentSolution.size());
//                        System.err.println(this.strategy.searchStatus());
//
//                        // agentSolution found
//
//                        Message solutionAnnouncement = new Message(MsgType.inform, agentSolution, agent.getId());
//
//                        agent.broadcastSolution(solutionAnnouncement);
//
//                        agent.evaluateRequests(solutionAnnouncement);
//
//                        agentSolution = agent.getCombinedSolution();
//
//                        solutions.add(agentSolution);
//
//            }
        }


    }


    public List<LinkedList<Node>> getSolutions() {
        return solutions;
    }

    public void setStrategy(Strategy strategy) {
        this.strategy = strategy;
    }
}