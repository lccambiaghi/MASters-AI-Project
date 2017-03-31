package communicationclient;

import heuristic.FibonacciHeap;
import heuristic.Heuristic;

import java.util.HashSet;

/**
 * Created by salik on 31-03-2017.
 */
public class StrategyBestFirst extends Strategy {
    private Heuristic heuristic;
    private FibonacciHeap<Node> frontier;
    private HashSet<Node> frontierSet;
    public StrategyBestFirst(Heuristic h) {
        super();
        this.heuristic = h;
        frontier = new FibonacciHeap<>();
//			frontier = new PriorityQueue<>(heuristic);
        frontierSet = new HashSet<>();
        //throw new NotImplementedException();
    }

    @Override
    public Node getAndRemoveLeaf() {
        Node n = frontier.dequeueMin().getValue();
        frontierSet.remove(n);
        return n;
    }

    @Override
    public void addToFrontier(Node n) {
        double priority = heuristic.f(n);
        frontier.enqueue(n, priority);
        frontierSet.add(n);
    }

    @Override
    public int countFrontier() {
        return frontier.size();
    }

    @Override
    public boolean frontierIsEmpty() {
        return frontier.isEmpty();
    }

    @Override
    public void clearFrontier() {
        frontier = new FibonacciHeap<>();
        frontierSet.clear();
        heuristic.clearMap();
    }

    @Override
    public boolean inFrontier(Node n) {
        return frontierSet.contains(n);
    }

    @Override
    public String toString() {
        return "Best-first Search using " + this.heuristic.toString();
    }
}
