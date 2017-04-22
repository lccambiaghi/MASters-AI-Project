package level;

import heuristic.CharCellComparator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.PriorityQueue;

/**
 * Created by salik on 19-04-2017.
 */
public class LevelAnalyzer {

    private ArrayList<CharCell> deadEnds = new ArrayList<>();
    private ArrayList<CharCell> corners = new ArrayList<>();
    private ArrayList<CharCell> oneWalls = new ArrayList<>();
    private ArrayList<CharCell> twoWalls = new ArrayList<>();
    private ArrayList<CharCell> noWalls = new ArrayList<>();
    private ArrayList<CharCell> allWalls = new ArrayList<>();
    private PriorityQueue<CharCell> cellPriorityQueue = new PriorityQueue<>(new CharCellComparator());

    /**
     * Method that analyzes how many walls are around the goals in a level
     * @param level level to analyze
     */
    public PriorityQueue<CharCell> analyze(Level level){
        HashSet<CharCell> goalCells = level.getAllCharCells();

        boolean[][] walls = level.getWalls();
        for (CharCell c:goalCells) {
            int row = c.getRow();
            int col = c.getCol();
            int numWalls = 0;
            if(walls[row][col-1]) numWalls++;
            if(walls[row][col+1]) numWalls++;
            if(walls[row-1][col]) numWalls++;
            if(walls[row+1][col]) numWalls++;
            switch (numWalls){
                case 0:
                    noWalls.add(c);
                    c.setNumWalls(0);
                    break;
                case 1:
                    oneWalls.add(c);
                    c.setNumWalls(1);
                    break;
                case 2:
                    if((walls[row-1][col] && walls[row][col-1])|| //Northwest corner
                            (walls[row-1][col] && walls[row][col+1])|| //Northeast corner
                            (walls[row+1][col] && walls[row][col+1])|| //Southeast corner
                            (walls[row+1][col] && walls[row][col-1])//Southwest corner
                            ){
                        corners.add(c);
                        c.setNumWalls(2);
                        c.setCorner(true);
                    }else{
                        twoWalls.add(c);
                        c.setNumWalls(2);
                    }
                    break;
                case 3:
                    deadEnds.add(c);
                    c.setNumWalls(3);
                    c.setDeadEnd(true);
                    break;
                case 4:
                    allWalls.add(c);
                    c.setNumWalls(4);
                    break;
            }
            c.calculatePriority();//Calculate priority of goalcell
            cellPriorityQueue.add(c);//Add to priorityqueue
        }
        return cellPriorityQueue;
    }
}
