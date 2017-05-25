package heuristic;

import communicationclient.Node;
import level.CharCell;
import level.Box;
import level.Level;

import java.util.HashSet;

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
        HashSet<CharCell> charCells = Level.getInstance().getAllCharCells();
        int val = charCells.size();
        for (CharCell g: charCells){
            char goalChar = g.getLetter();
            Box box = n.boxes[g.getRow()][g.getCol()];
            if (box!=null){
                char b = Character.toLowerCase(box.getBoxChar());
                if (b == goalChar) {
                   val--;
                }
            }
        }
        return val*15;
    }

    public static int keepRight(Node n){
        if(n.action==null) return 0;
        switch (n.action.dir1){
            case W:
                if(n.getWalls()[n.agentRow-1][n.agentCol]) return 0;
                break;
            case S:
                if(n.getWalls()[n.agentRow][n.agentCol-1]) return 0;
                break;
            case E:
                if(n.getWalls()[n.agentRow+1][n.agentCol]) return 0;
                break;
            case N:
                if(n.getWalls()[n.agentRow][n.agentCol+1]) return 0;
                break;
        }
        return 2;
    }

}
