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
            GoalBoxToCell goal = (GoalBoxToCell) goalQueue.poll();
            Agent agent = goal.getBox().getAssignedAgent();
            agent.setNumberOfGoals(agent.getNumberOfGoals()+1);

            agent.refineBoxToChar(goal); //Plan for a single goal at a time
            tmp.add(goal);
        }

        goalQueue = tmp;//Save all the charcells so they are not lost after poll
    }

    public void searchingPhase() {
        this.solutions = new HashMap<>();
        for (List<Agent> agentList: Level.getInstance().getAgentsByColorMap().values()) {
            for (Agent a: agentList) {
                this.solutions.put(Character.getNumericValue(a.getId()), new LinkedList<>());
            }

        }
        while(!goalQueue.isEmpty()){
            Goal goal = goalQueue.poll();
            Agent agent = goal.getAgent();
            LinkedList<Node> agentSolution = agent.searchGoal(goal);

            if (agentSolution == null) {//Is checked in agent.searchGoal
                // TODO agent is stuck
                goalQueue.add(goal);//Add goal again
                for (Box b:agent.getRemovedBoxes()) {
                    LinkedList<Node> agentRequestCells = agent.getGoalSolution();
                    //Search for free space
                    //Cell destination = new Cell(5,10);
                    Goal freeAgent = new GoalFreeAgent(b,agentRequestCells, agent);
                    freeAgent.setPriority(0);//High priority

                    Message moveBoxRequest = new GoalMessage(MsgType.request,freeAgent, agentRequestCells,agent.getId());
                    agent.broadcastMessage(moveBoxRequest);
                    agent.evaluateMessage(moveBoxRequest);
                    goalQueue.add(freeAgent);
                    agent.setGoalSolution(new LinkedList<>());//Forget Solution
                }
                System.err.println(agent.getStrategy().searchStatus());
                System.err.println("Agent " + agent.getId() + " is unable to complete his subgoals.");
            } else {
                System.err.println("\nSummary for " + agent.getStrategy().toString() + " for agent " + agent.getId() + ":");
                System.err.println("Found solution of length " + agentSolution.size());
                System.err.println(agent.getStrategy().searchStatus());
                agent.setNumberOfGoals(agent.getNumberOfGoals()-1);

                // agentSolution found
                Message solutionAnnouncement = new Message(MsgType.inform, agentSolution, agent.getId());
                solutionAnnouncement.setContentStart(agent.getAllGoalSolution().size());

                agent.broadcastMessage(solutionAnnouncement);

                agent.evaluateMessage(solutionAnnouncement);

                /*Stuff below is outcommented. In the getSolutions() method, we fetch the global plans from the Agents.*/
//                agentSolution = agent.getGoalSolution(); // solution is updated after negotiation

//                if(this.solutions.get(Character.getNumericValue(agent.getId()))!=null){
//                    LinkedList<Node> agentCombinedSolution = this.solutions.get(Character.getNumericValue(agent.getId()));
//                    agentCombinedSolution.addAll(agentSolution);
//                    this.solutions.put(Character.getNumericValue(agent.getId()), agentCombinedSolution);
//                }else{
//                    LinkedList<Node> agentCombinedSolution = new LinkedList<>();
//                    agentCombinedSolution.addAll(agentSolution);
//                    this.solutions.put(Character.getNumericValue(agent.getId()), agentCombinedSolution);
//                }

            }
        }
    }

    public List<LinkedList<Node>> getSolutions() {
        this.solutions = new HashMap<>();
        for (List<Agent> agentList: Level.getInstance().getAgentsByColorMap().values()) {
            for (Agent a: agentList) {
                this.solutions.put(Character.getNumericValue(a.getId()), a.getAllGoalSolution());
            }

        }
        Map<Integer, LinkedList<Node>> treeMap = new TreeMap<>(this.solutions);
        List<LinkedList<Node>> solutions = new LinkedList<>();
        solutions.addAll(treeMap.values());
        return solutions;
    }
}
