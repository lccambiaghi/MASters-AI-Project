package graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Created by salik on 25-04-2017.
 */
public class Graph {
    private HashSet<Vertex> vertices = new HashSet<>();
    //private HashSet<Edge> edges = new HashSet<>();
    private HashMap<Vertex, HashSet<Edge>> edges = new HashMap<>();
    private List<Vertex> goalVerticies = new ArrayList<>();
    private List<Vertex> boxVerticies = new ArrayList<>();
    private HashSet<Vertex> visited = new HashSet<>();
    private List<Edge> tmp = new ArrayList<>();
    public void addVertex(Vertex vertex){
        vertices.add(vertex);
    }
    public Graph getCopy(){
        Graph g = new Graph();
        g.setBoxVerticies(new ArrayList<>(this.getBoxVerticies()));
        g.setGoalVerticies(new ArrayList<>(this.getGoalVerticies()));
        g.setVertices((HashSet<Vertex>) this.getVertices().clone());
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
                if((n2.getRow() == n1.getRow() && Math.abs(n2.getCol()-n1.getCol()) == 1)|| (n2.getCol() == n1.getCol() && Math.abs(n2.getRow()-n1.getRow())==1)){
                    Edge e = new Edge(n1, n2);
                    n1.addEdge(n2);
                    n1Edges.add(e);
                }
            }
        }
    }
    public void analyzeGraph(){
        for (Vertex goal : goalVerticies) {
            int numberOfComponents = 0;//Start on zero as goalcell is counted as single component!
            Graph newGraph = this.getCopy();
            newGraph.removeVertex(goal);
            visited = new HashSet<>();
            Vertex startVertex = null;
            for (Vertex v: newGraph.getVertices()) {
                startVertex = v;
                break;
            }
            runDFS(startVertex);
            for (Vertex u: vertices) {
                if(!visited.contains(u)){
                    numberOfComponents++;
                    runDFS(u);
                }
            }
            for (Edge e:tmp) {
                edges.get(e.getFrom()).add(e);
            }
            System.out.println(numberOfComponents);
        }
    }

    private void runDFS(Vertex startVertex){
        visited.add(startVertex);
        for (Edge e: edges.get(startVertex)) {
            Vertex v2 = e.getTo();
            if(!visited.contains(v2)) {
                visited.add(v2);
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

    public List<Vertex> getGoalVerticies() {
        return goalVerticies;
    }

    public void setGoalVerticies(List<Vertex> goalVerticies) {
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
            v2Edges.remove(v2v1);
            tmp.add(v2v1);
            //v2.getEdges().remove(v2v1);
        }
        edges.remove(v);
    }
}
