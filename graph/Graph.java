package graph;

import java.util.*;

/**
 * Created by salik on 25-04-2017.
 */
public class Graph {
    private HashSet<Vertex> vertices = new HashSet<>();
    private HashSet<Vertex> goalVertices = new HashSet<>();
    private HashSet<Vertex> boxVertices = new HashSet<>();
    private HashSet<Vertex> agentVertices = new HashSet<>();

    private HashMap<Vertex, HashSet<Edge>> edgesMap = new HashMap<>();
    private HashSet<Vertex> visitedVertices = new HashSet<>();
    private HashSet<Vertex> currentComponentVertices = new HashSet<>();
    private HashMap<Vertex, HashSet<Vertex>> boxesInComponent = new HashMap<>();
    private HashMap<Vertex, HashSet<Vertex>> agentsInComponent = new HashMap<>();

    private List<Edge> removedEdges = new ArrayList<>();
    private List<Vertex> limitedResources = new ArrayList<>();
    private List<Vertex> nonLimitedResources = new ArrayList<>();
    private int numberOfComponents = 0;


    public void addVertex(Vertex vertex){
        vertices.add(vertex);
    }

    /**
     * Creates goalVertices, boxVertices
     *
     * Creates edgesMap
     *
     * Keeps only vertices in components with goal cells (discard the white text which is not a cell, not reachable cells)
     */
    public Graph createGraph(){
        for (Vertex n1 :vertices) {
            if(n1.getGoalCell() != null) goalVertices.add(n1);
            if(n1.getBox() != null) boxVertices.add(n1);
            if(n1.getAgent() != null) agentVertices.add(n1);

            HashSet<Edge> n1Edges = new HashSet<>();
            edgesMap.put(n1,n1Edges);

            for (Vertex n2: vertices){
                if((n2.getRow() == n1.getRow() && Math.abs(n2.getCol()-n1.getCol()) == 1)
                        || (n2.getCol() == n1.getCol() && Math.abs(n2.getRow()-n1.getRow())==1)){//Add edge if n1 and n2 are adjacent
                    Edge e = new Edge(n1, n2);
                    n1Edges.add(e);
                }
            }
        }

        //Run DFS from all goalVertices to only consider the components of the level that matter.
        HashSet<Vertex> reachableBoxes = new HashSet<>();
        HashSet<Vertex> reachableAgents = new HashSet<>();
        for (Vertex goalVertex: goalVertices) {
            if (!visitedVertices.contains(goalVertex)){
                numberOfComponents++;
                currentComponentVertices = new HashSet<>();
                runDFS(goalVertex);
                reachableBoxes = new HashSet<>();
                reachableAgents = new HashSet<>();
                for (Vertex boxVertex: boxVertices) {
                    if(currentComponentVertices.contains(boxVertex)){
                        reachableBoxes.add(boxVertex);
                    }
                }
                for (Vertex agentVertex: agentVertices) {
                    if(currentComponentVertices.contains(agentVertex)){
                        reachableAgents.add(agentVertex);
                    }
                }
                boxesInComponent.put(goalVertex, reachableBoxes);
                agentsInComponent.put(goalVertex, reachableAgents);
            }else{
                boxesInComponent.put(goalVertex, reachableBoxes);
                agentsInComponent.put(goalVertex, reachableAgents);
            }
        }
        //Remove all vertices that can't be visited from any goal cell as they will be outside of the walkable map
        vertices.retainAll(visitedVertices);
        return this;
    }


    /**
     * It starts from a vertex
     * It gets all edges of the vertex
     * Then gets the vertex on the other side of the edge
     * Run DFS on that vertex
     */
    private void runDFS(Vertex startVertex){
        visitedVertices.add(startVertex);
        currentComponentVertices.add(startVertex);
        for (Edge e: edgesMap.get(startVertex)) {
            Vertex v2 = e.getTo();
            if(!visitedVertices.contains(v2)) {
                visitedVertices.add(v2);
                currentComponentVertices.add(v2);
                runDFS(v2);
            }
        }
    }

    /**
     * This method analyzes the graph and finds limited resources
     *
     * Go trough all vertices
     * Remove it and see if the graph gets divided when it is removed
     * If yes,
     */
    public void analyzeGraph(Graph graph, HashSet<Vertex> verticesToRemove){
        for (Vertex vertex : verticesToRemove) {
            int numberOfComponents = 0;//Start on zero as goalcell is counted as single component!
            ArrayList<Graph> components = new ArrayList<>();
            Graph newGraph = graph.getCopy();
            newGraph.removeVertex(vertex);
            newGraph.visitedVertices = new HashSet<>();
            newGraph.currentComponentVertices = new HashSet<>();

            Vertex startVertex = pickRandomVertex(newGraph);
            newGraph.runDFS(startVertex);//Run DFS on new graph

            for (Vertex u: graph.vertices) {
                if(!newGraph.visitedVertices.contains(u)){ //then it breaks the graph
                    numberOfComponents++;
                    if(vertex != u){
                        Graph newComponent = new Graph();
                        for (Vertex v : newGraph.currentComponentVertices){
                            newComponent.addVertex(v);
                        }
                        //TODO build graph
                        components.add(newComponent);
                        newGraph.currentComponentVertices = new HashSet<>();
                        newGraph.runDFS(u);//Only Run DFS on new graph if it is not trying to on the goalcell we removed.
                        if(newGraph.currentComponentVertices.size() > 0){
                            newComponent = new Graph();
                            for (Vertex v : newGraph.currentComponentVertices){
                                newComponent.addVertex(v);
                            }
                            //TODO build graph
                            components.add(newComponent);
                            newGraph.currentComponentVertices = new HashSet<>();
                        }
                    }
                }
            }

            //Insert the edges that were removed as Java points to the same object even when copying the verticies and edgesMap
            for (Edge e: newGraph.removedEdges) {
                graph.edgesMap.get(e.getFrom()).add(e);
            }

            int importantComponents = importantComponents(components);
            vertex.setGraphComponentsIfRemoved(numberOfComponents);
            vertex.setImportantComponetsIfRemoved(importantComponents);
            if(numberOfComponents > graph.numberOfComponents && importantComponents > 1) graph.limitedResources.add(vertex); //TODO: numberofcomponents > 1 and bothComponentsAreImportant(components)
            else graph.nonLimitedResources.add(vertex);
        }
    }

    private Vertex pickRandomVertex(Graph newGraph) {
        Vertex start = null;
        for (Vertex v: newGraph.getVertices()) {
            start = v;
            break;
        }
        return start;
    }

    private Graph getCopy(){
        Graph g = new Graph();
        g.setBoxVertices(new HashSet<>(this.getBoxVertices()));
        g.setGoalVertices(new HashSet<>(this.getGoalVertices()));
        HashSet<Vertex> newVerticies = new HashSet<>();
        newVerticies.addAll(this.getVertices());
        g.setVertices(newVerticies);
        g.setEdgesMap((HashMap<Vertex, HashSet<Edge>>) this.getEdgesMap().clone());
        return g;
    }

    private void removeVertex(Vertex v){
        vertices.remove(v);
        HashSet<Edge> v1Edges = edgesMap.get(v);
        for (Edge ed: v1Edges) {
            Vertex v2 = ed.getTo();
            HashSet<Edge> v2Edges = edgesMap.get(v2);
            Edge v2v1 = new Edge(v2,v);
            v2Edges.remove(v2v1);//Remove the edge from v2 -> v
            removedEdges.add(v2v1);
        }
        edgesMap.remove(v);//Remove all edgesMap from v -> ...
    }

    private int importantComponents(ArrayList<Graph> components) {
        int importantComponents = 0;
        for (Graph g : components){
            for(Vertex v : g.vertices){
                if (v.getBox() != null || v.getGoalCell() != null) {
                    importantComponents++;
                    break;
                }
            }
        }
        return importantComponents;
    }

    public void calculatePriority(Graph graph){
        Graph newGraph = graph.getCopy();
        int priority = 10;
        ArrayList<Vertex> removedGoals = new ArrayList<>();
        HashSet<Vertex> tmpRemovedGoals = new HashSet<>();
        HashSet<Vertex> tmpGoals = new HashSet<>();

        while(removedGoals.size() != graph.goalVertices.size()){
            for (Vertex goalVertex : newGraph.goalVertices) {
                if (newGraph.edgesMap.get(goalVertex).size() == 1){
                    goalVertex.getGoalCell().setPriority(priority);
                    tmpRemovedGoals.add(goalVertex);
                }else{
                    tmpGoals.add(goalVertex);
                }
            }
            for (Vertex rv:tmpRemovedGoals) {
                removedGoals.add(rv);
                newGraph.removeVertex(rv);
                newGraph.goalVertices.remove(rv);
            }
            priority+=10;
            int components = 100;
            Vertex removeVertex = null;
            for (Vertex tgv: tmpGoals) {
                analyzeGraph(newGraph, tmpGoals);
                if (components > tgv.getImportantComponetsIfRemoved()){
                    removeVertex = tgv;
                    components = tgv.getImportantComponetsIfRemoved();
                }

            }
            tmpGoals.clear();

            if (removeVertex != null) {
                removeVertex.getGoalCell().setPriority(priority);
                removedGoals.add(removeVertex);
                newGraph.removeVertex(removeVertex);
                newGraph.goalVertices.remove(removeVertex);
            }
            tmpRemovedGoals.clear();
            priority+=10;
        }

        for (Edge e: newGraph.removedEdges) {
            graph.edgesMap.get(e.getFrom()).add(e);
        }

    }

    public List<Vertex> getLimitedResources() {
        return limitedResources;
    }

    public List<Vertex> getNonLimitedResources() {
        return nonLimitedResources;
    }

    public HashSet<Vertex> getVertices() {
        return vertices;
    }

    public void setVertices(HashSet<Vertex> vertices) {
        this.vertices = vertices;
    }

    public HashMap<Vertex, HashSet<Edge>> getEdgesMap() {
        return edgesMap;
    }

    public void setEdgesMap(HashMap<Vertex, HashSet<Edge>> edgesMap) {
        this.edgesMap = edgesMap;
    }

    public HashSet<Vertex> getGoalVertices() {
        return goalVertices;
    }

    public void setGoalVertices(HashSet<Vertex> goalVertices) {
        this.goalVertices = goalVertices;
    }

    public HashSet<Vertex> getBoxVertices() {
        return boxVertices;
    }

    public void setBoxVertices(HashSet<Vertex> boxVertices) {
        this.boxVertices = boxVertices;
    }

    public HashMap<Vertex, HashSet<Vertex>> getBoxesInComponent() {
        return boxesInComponent;
    }

    public HashMap<Vertex, HashSet<Vertex>> getAgentsInComponent() {
        return agentsInComponent;
    }
}
