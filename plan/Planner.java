package plan;

import communication.Message;
import communication.MsgType;
import communicationclient.Agent;
import communicationclient.Node;
import goal.Goal;
import goal.GoalBoxToChar;
import heuristic.GoalComparator;
import level.Color;
import level.Level;

import java.util.*;

public class Planner {
    private PriorityQueue<Goal> goalQueue;
    private HashMap<Integer, LinkedList<Node>> solutions;

    public Planner(PriorityQueue<Goal> priorityQueue) {
        this.goalQueue = priorityQueue;
    }

    public void refineInitialGoals(){
        PriorityQueue<Goal> tmp = new PriorityQueue<>(new GoalComparator());
        while(!goalQueue.isEmpty()){
            GoalBoxToChar goal = (GoalBoxToChar) goalQueue.poll();
            Agent agent = goal.getBox().getAssignedAgent();
            agent.setNumberOfGoals(agent.getNumberOfGoals()+1);

            agent.refineBoxToChar(goal); //Plan for a single goal at a time
            tmp.add(goal);
        }

        goalQueue = tmp;//Save all the charcells so they are not lost after poll
    }

    public void searchingPhase() {
        this.solutions = new HashMap<>();
//        HashSet<Agent> hasSearched = new HashSet<>();

        while(!goalQueue.isEmpty()){
            Goal goal = goalQueue.poll();
            Agent agent = goal.getAgent();
//            if (hasSearched.contains(agent)) continue;
//            hasSearched.add(agent);
            LinkedList<Node> agentSolution = agent.searchGoal(goal);
            if (agentSolution == null) {
                // TODO agent is stuck

                System.err.println(agent.getStrategy().searchStatus());
                System.err.println("Agent " + agent.getId() + " is unable to complete his subgoals.");
            } else {
                System.err.println("\nSummary for " + agent.getStrategy().toString() + " for agent " + agent.getId() + ":");
                System.err.println("Found solution of length " + agentSolution.size());
                System.err.println(agent.getStrategy().searchStatus());
                agent.setNumberOfGoals(agent.getNumberOfGoals()-1);

                // agentSolution found
                Message solutionAnnouncement = new Message(MsgType.inform, agentSolution, agent.getId());

                agent.broadcastSolution(solutionAnnouncement);

                agent.evaluateRequests(solutionAnnouncement);

                agentSolution = agent.getCombinedSolution(); // solution is updated after negotiation

                if(this.solutions.get(Character.getNumericValue(agent.getId()))!=null){
                    LinkedList<Node> agentCombinedSolution = this.solutions.get(Character.getNumericValue(agent.getId()));
                    agentCombinedSolution.addAll(agentSolution);
                    this.solutions.put(Character.getNumericValue(agent.getId()), agentCombinedSolution);
                }else{
                    LinkedList<Node> agentCombinedSolution = new LinkedList<>();
                    agentCombinedSolution.addAll(agentSolution);
                    this.solutions.put(Character.getNumericValue(agent.getId()), agentCombinedSolution);
                }

            }
        }
    }

    public List<LinkedList<Node>> getSolutions() {
        List<LinkedList<Node>> solutions = new LinkedList<>();
        Map<Integer, LinkedList<Node>> treeMap = new TreeMap<>(this.solutions);

        for (LinkedList<Node> solution : treeMap.values()) {
            solutions.add(solution);
        }
        return solutions;
    }
    private boolean agentsHasMoreSubgoals(){
        HashMap<Color, List<Agent>> agentsMap = Level.getInstance().getAgentsByColorMap();
        Collection<List<Agent>> agentLists = agentsMap.values();
        for (List<Agent> agentList: agentLists) {
            for (Agent agent: agentList) {
                if (agent.hasMoreSubgoals()) return true;
            }
        }
        return false;
    }
}
