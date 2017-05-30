package plan;

import communication.Message;
import communication.MsgType;
import communication.GoalMessage;
import communicationclient.Agent;
import communicationclient.Node;
import goal.Goal;
import goal.GoalBoxToCell;
import goal.GoalFreeAgent;
import graph.Graph;
import heuristic.GoalComparator;
import level.Box;
import level.CharCell;
import level.Level;

import java.util.*;

public class Planner {
    private PriorityQueue<Goal> goalQueue;
    private HashMap<Integer, LinkedList<Node>> solutions;
    private ArrayList<Goal> completedGoals;

    public Planner(PriorityQueue<Goal> priorityQueue) {
        this.goalQueue = priorityQueue;
    }

    public void searchingPhase(Graph graph) {
        this.solutions = new HashMap<>();
        this.completedGoals = new ArrayList<>();

        for (List<Agent> agentList: Level.getInstance().getAgentsColorMap().values())
            for (Agent a: agentList){
                this.solutions.put(Character.getNumericValue(a.getId()), new LinkedList<>());
                a.setLimitedRessources(graph.getLimitedResources());
            }


        while(!goalQueue.isEmpty()){
            Goal goal = goalQueue.poll();
            Agent agent = goal.getAgent();
            if(isGoalAlreadySatisfied(goal, agent)) continue;
            LinkedList<Node> goalSolution = agent.searchGoal(goal);
            if (goalSolution == null) {//Is checked in agent.searchGoal
                // We add one to make sure that this goal will happen next when putting it back on the queue.
                // TODO remove hotfix and improve logic
                //goal.setPriority(goal.getPriority()-1);
                goalQueue.add(goal);//Add goal again

                agent.callForHelp(goal); // Pass in the goal so that we can set the priority of the newly created goal to be lower

                //System.err.println(agent.getStrategy().searchStatus());
                //System.err.println("Agent " + agent.getId() + " has called for help.");
            } else {
                //System.err.println("\nSummary for " + agent.getStrategy().toString() + " for agent " + agent.getId() + ": "+goal.toString());
                //System.err.println("Found solution of length " + goalSolution.size());
                //System.err.println(agent.getStrategy().searchStatus());
                agent.setNumberOfGoals(agent.getNumberOfGoals()-1);

                // goalSolution found

                // Start negotiation
                // The agent who found a solution has to check with each agent until his plan is approved
                agent.negotiateGoalSolution();
                // Negotiation is over: his goalSolution is approved by everyone and he added to his combinedSolution
                //TODO has this goal destroyed any other goals already completed
                    if(goal instanceof GoalBoxToCell){//Don't add move out the way goals
                        Node n = new Node(goal, agent);
                        //TODO make sure position is updated
                        HashSet<Box> allBoxes = Level.getInstance().getAllBoxes();
                        for (Box b : allBoxes) {
                                n.addBox(b);
                        }
                        if (!completedGoals.contains(goal)) completedGoals.add(goal);
                        for (Goal completedGoal: completedGoals) {
                            if (!completedGoal.isGoalSatisfied(n) && !goalQueue.contains(completedGoal)) goalQueue.add(completedGoal);
                        }
                    }

            }
        }
    }

    private boolean isGoalAlreadySatisfied(Goal goal, Agent agent) {
        if(goal instanceof GoalBoxToCell){
            Node n = new Node(goal, agent);
            //TODO make sure position is updated
            HashSet<Box> allBoxes = Level.getInstance().getAllBoxes();
            for (Box b : allBoxes) {
                n.addBox(b);
            }
            if(goal.isGoalSatisfied(n)){
                completedGoals.add(goal);
                return true;
            }
        }
        return false;
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
