package graph;

import communicationclient.Agent;
import level.Box;
import level.CharCell;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by salik on 25-04-2017.
 */
public class Vertex {
    private int row;
    private int col;
    private int graphComponentsIfRemoved = 1;
    private int importantComponetsIfRemoved = 1;
    private boolean satisfied;
    private CharCell goalCell = null;
    private Box box = null;
    private Agent agent = null;

    public Vertex(int row, int col){
        this.row = row;
        this.col = col;
    }
    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }


    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public int getGraphComponentsIfRemoved() {
        return graphComponentsIfRemoved;
    }

    public void setGraphComponentsIfRemoved(int graphComponentsIfRemoved) {
        this.graphComponentsIfRemoved = graphComponentsIfRemoved;
    }

    public int getImportantComponetsIfRemoved() {
        return importantComponetsIfRemoved;
    }

    public void setImportantComponetsIfRemoved(int importantComponetsIfRemoved) {
        this.importantComponetsIfRemoved = importantComponetsIfRemoved;
    }

    public boolean isSatisfied(){
        return satisfied;
    }
    public void setSatisfied(boolean value){
        satisfied = value;
    }
    public void setGoalCell(CharCell c){
        goalCell = c;
    }
    public CharCell getGoalCell(){
        return goalCell;
    }
    public void setBox(Box b){
        box = b;
    }
    public Box getBox(){
        return box;
    }

    public Agent getAgent() {
        return agent;
    }

    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Vertex vertex = (Vertex) o;

        if (row != vertex.row) return false;
        return col == vertex.col;
    }

    @Override
    public int hashCode() {
        int result = row;
        result = 31 * result + col;
        return result;
    }
}
