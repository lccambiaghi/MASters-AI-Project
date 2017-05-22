package plan;

import communication.Message;
import communication.MsgType;
import communication.GoalMessage;
import communicationclient.Agent;
import communicationclient.Node;
import goal.Goal;
import goal.GoalBoxToCell;
import goal.GoalFreeAgent;
import heuristic.GoalComparator;
import level.Box;
import level.CharCell;
import level.Level;

import java.util.*;

public class Planner {
    private PriorityQueue<Goal> goalQueue;
    private HashMap<Integer, LinkedList<Node>> solutions;

    public Planner(PriorityQueue<Goal> priorityQueue) {
        this.goalQueue = priorityQueue;
    }

    public void searchingPhase() {
        this.solutions = new HashMap<>();

        for (List<Agent> agentList: Level.getInstance().getAgentsColorMap().values())
            for (Agent a: agentList)
                this.solutions.put(Character.getNumericValue(a.getId()), new LinkedList<>());

        while(!goalQueue.isEmpty()){
            Goal goal = goalQueue.poll();
            Agent agent = goal.getAgent();
            LinkedList<Node> goalSolution = agent.searchGoal(goal);
            if (goalSolution == null) {//Is checked in agent.searchGoal
                // We add one to make sure that this goal will happen next when putting it back on the queue.
                // TODO remove hotfix and improve logic
                goal.setPriority(goal.getPriority()-1);
                goalQueue.add(goal);//Add goal again

                for (Box b:agent.getRemovedBoxes()) {
                    LinkedList<Node> agentRequestCells = agent.getGoalSolution();
                    //Search for free space
                    //Cell destination = new Cell(5,10);
                    Goal freeAgent = new GoalFreeAgent(b,agentRequestCells, agent);
                    freeAgent.setPriority(0);//High priority

                    //TODO move logic in Agent class
                    Message moveBoxRequest = new GoalMessage(MsgType.request,freeAgent, agentRequestCells,agent.getId());
                    agent.broadcastMessage(moveBoxRequest);
                    agent.checkReplies(moveBoxRequest);
                    goalQueue.add(freeAgent);
                    agent.setGoalSolution(new LinkedList<>());//Forget Solution
                }
                System.err.println(agent.getStrategy().searchStatus());
                System.err.println("Agent " + agent.getId() + " is unable to complete his subgoals.");
            } else {
                System.err.println("\nSummary for " + agent.getStrategy().toString() + " for agent " + agent.getId() + ":");
                System.err.println("Found solution of length " + goalSolution.size());
                System.err.println(agent.getStrategy().searchStatus());
                agent.setNumberOfGoals(agent.getNumberOfGoals()-1);

                // goalSolution found

                // Start negotiation
                // The agent who found a solution has to check with each agent until his plan is approved
                agent.negotiateGoalSolution();

                // Negotiation is over: his goalSolution is approved by everyone and he added to his combinedSolution
            }
        }
    }

    public List<LinkedList<Node>> getSolutions() {
        this.solutions = new HashMap<>();
        for (List<Agent> agentList: Level.getInstance().getAgentsColorMap().values()) {
            for (Agent a: agentList) {
                this.solutions.put(Character.getNumericValue(a.getId()), a.getCombinedSolution());
            }

        }
        Map<Integer, LinkedList<Node>> treeMap = new TreeMap<>(this.solutions);
        List<LinkedList<Node>> solutions = new LinkedList<>();
        solutions.addAll(treeMap.values());
        return solutions;
    }

    public void addGoal(Goal g){
        goalQueue.add(g);
    }
}
