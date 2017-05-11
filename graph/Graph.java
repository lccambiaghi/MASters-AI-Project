package graph;

import level.CharCell;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by salik on 25-04-2017.
 */
public class Graph {
    private HashSet<Vertex> vertices = new HashSet<>();
    //private HashSet<Edge> edges = new HashSet<>();
    private HashMap<Vertex, HashSet<Edge>> edges = new HashMap<>();
    private HashSet<Vertex> goalVerticies = new HashSet<>();
    private List<Vertex> boxVerticies = new ArrayList<>();
    private HashSet<Vertex> visited = new HashSet<>();
    private HashSet<Vertex> componentVisited = new HashSet<>();
    private List<Edge> tmp = new ArrayList<>();
    private List<Vertex> limitedResources = new ArrayList<>();
    private List<Vertex> nonLimitedResources = new ArrayList<>();
    int numberOfComponents = 0;
    public List<Vertex> getNonLimitedResources() {
        return nonLimitedResources;
    }

    public void addVertex(Vertex vertex){
        vertices.add(vertex);
    }

    private Graph getCopy(){
        Graph g = new Graph();
        g.setBoxVerticies(new ArrayList<>(this.getBoxVerticies()));
        g.setGoalVerticies(new HashSet<>(this.getGoalVerticies()));
        HashSet<Vertex> newVerticies = new HashSet<>();
        newVerticies.addAll(this.getVertices());
        g.setVertices(newVerticies);
        g.setEdges((HashMap<Vertex, HashSet<Edge>>) this.getEdges().clone());
        return g;
    }
    public void createGraph(){
        //TODO build graph with edges
        for (Vertex n1 :vertices) {
            if(n1.getGoalCell() != null) goalVerticies.add(n1);
            if(n1.getBox() != null) boxVerticies.add(n1);
            HashSet<Edge> n1Edges = new HashSet<>();
            edges.put(n1,n1Edges);
            for (Vertex n2: vertices){
                if((n2.getRow() == n1.getRow() && Math.abs(n2.getCol()-n1.getCol()) == 1)
                        || (n2.getCol() == n1.getCol() && Math.abs(n2.getRow()-n1.getRow())==1)){//Add edge if n1 and n2 are adjacent
                    Edge e = new Edge(n1, n2);
                    n1Edges.add(e);
                }
            }
        }
        //Run DFS from all goalVerticies to see which part of the graph is connected to any goal.
        for (Vertex goalVertex: goalVerticies) {
            if (!visited.contains(goalVertex)){
                numberOfComponents++;
                runDFS(goalVertex);
            }
        }
        //Remove all verticies that can't be visited from any goalcell.
        vertices.retainAll(visited);
    }

    public List<Vertex> getLimitedResources() {
        return limitedResources;
    }

    /**
     * This method analyzes the graph and finds limited ressources
     */
    public void analyzeGraph(){
        for (Vertex vertex : vertices) {//Go through all verticies in the graph and find the ones that divide the graph when occupied.
            int numberOfComponents = 0;//Start on zero as goalcell is counted as single component!
            ArrayList<Graph> components = new ArrayList<>();
            Graph newGraph = this.getCopy();
            newGraph.removeVertex(vertex);
            newGraph.visited = new HashSet<>();
            newGraph.componentVisited = new HashSet<>();
            Vertex startVertex = null;
            for (Vertex v: newGraph.getVertices()) {
                startVertex = v;
                break;
            }
            newGraph.runDFS(startVertex);//Run DFS on new graph

            for (Vertex u: vertices) {
                if(!newGraph.visited.contains(u)){
                    Graph newComponent = new Graph();
                    for (Vertex v : newGraph.componentVisited){
                        newComponent.addVertex(v);
                    }
                    //TODO build graph
                    components.add(newComponent);
                    newGraph.componentVisited = new HashSet<>();
                    numberOfComponents++;
                    if(vertex != u) newGraph.runDFS(u);//Only Run DFS on new graph if it is not trying to on the goalcell we removed.
                }
            }
            //Insert the edges that were removed as Java points to the same object even when copying the verticies and edges
            for (Edge e: newGraph.tmp) {
                edges.get(e.getFrom()).add(e);
            }
            //Is it a goalVertex then update the CharCell
            if(goalVerticies.contains(vertex)){
                CharCell goalCell = vertex.getGoalCell();
                goalCell.setGraphComponentsIfFulfilled(numberOfComponents);
//                System.err.println("Removing goal: " + vertex.getGoalCell().getLetter() +" will make graph have "+ numberOfComponents +" components");
            }
            vertex.setGraphComponentsIfRemoved(numberOfComponents);
            if(numberOfComponents > this.numberOfComponents && componentsAreImportant(components)) limitedResources.add(vertex); //TODO: numberofcomponents > 1 and bothComponentsAreImportant(components)
            else nonLimitedResources.add(vertex);
        }
    }

    private boolean componentsAreImportant(ArrayList<Graph> components) {
        int importantComponents = 0;
        for (Graph g : components){
            for(Vertex v : g.vertices){
                if (v.getBox() != null || v.getGoalCell() != null) {
                    importantComponents++;
                    break;
                }
            }
        }
        return importantComponents > 1;
    }

    private void runDFS(Vertex startVertex){
        visited.add(startVertex);
        componentVisited.add(startVertex);
        for (Edge e: edges.get(startVertex)) {
            Vertex v2 = e.getTo();
            if(!visited.contains(v2)) {
                visited.add(v2);
                componentVisited.add(v2);
                runDFS(v2);
            }
        }
    }
    public HashSet<Vertex> getVertices() {
        return vertices;
    }

    public void setVertices(HashSet<Vertex> vertices) {
        this.vertices = vertices;
    }

    public HashMap<Vertex, HashSet<Edge>> getEdges() {
        return edges;
    }

    public void setEdges(HashMap<Vertex, HashSet<Edge>> edges) {
        this.edges = edges;
    }

    public HashSet<Vertex> getGoalVerticies() {
        return goalVerticies;
    }

    public void setGoalVerticies(HashSet<Vertex> goalVerticies) {
        this.goalVerticies = goalVerticies;
    }

    public List<Vertex> getBoxVerticies() {
        return boxVerticies;
    }

    public void setBoxVerticies(List<Vertex> boxVerticies) {
        this.boxVerticies = boxVerticies;
    }

    private void removeVertex(Vertex v){
        vertices.remove(v);
        HashSet<Edge> v1Edges = edges.get(v);
        for (Edge ed: v1Edges) {
            Vertex v2 = ed.getTo();
            HashSet<Edge> v2Edges = edges.get(v2);
            Edge v2v1 = new Edge(v2,v);
            v2Edges.remove(v2v1);//Remove the edge from v2 -> v
            tmp.add(v2v1);
        }
        edges.remove(v);//Remove all edges from v -> ...
    }
}
