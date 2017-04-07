package heuristic;

import communicationclient.Node;
import communicationclient.StaticLevelItems;
import level.Box;
import level.Goal;
import level.Level;

import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Created by salik on 08-02-2017.
 */
public class HeuristicHelper {

    public static int manhattanDistance(int startRow, int startCol, int goalRow, int goalCol){
        return Math.abs(goalCol - startCol) + Math.abs(goalRow - startRow);
    }
    public static double euclideanDistance(int startRow, int startCol, int goalRow, int goalCol){
        return Math.sqrt(Math.pow(goalCol-startCol,2)+Math.pow(goalRow-startRow,2));
    }
    public static int goalCount(Node n){
        HashSet<Goal> goals = Level.getInstance().getAllGoals();
        int val = goals.size();
        for (Goal g: goals){
            char goalChar = g.getGoalChar();
            Box box = n.boxes[g.getRow()][g.getCol()];
            if (box!=null){
                char b = Character.toLowerCase(box.getBoxChar());
                if (b == goalChar) {
                   val--;
                }
            }
        }
        return val;
    }

}
