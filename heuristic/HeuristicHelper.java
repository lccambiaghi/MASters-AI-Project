package heuristic;

import communicationclient.Node;
import communicationclient.StaticLevelItems;

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

}
