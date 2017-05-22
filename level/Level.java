package level;

import communicationclient.Agent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Created by salik on 31-03-2017.
 */
public class Level {

    private static Level instance;
    private HashMap<Character, HashSet<CharCell>> goalsByChar;
    private HashMap<Character, HashSet<Box>> boxesByChar;
    private HashMap<Color, HashSet<Box>> boxesByColor;
    private HashSet<Box> allBoxes;
    private HashSet<CharCell> allCharCells;
    private boolean[][] walls;
    public int MAX_ROW;
    public int MAX_COL;
    private HashMap<Color, List<Agent>> agentsColorMap = new HashMap<>();
    private List<Agent> allAgents = new ArrayList<>();

    private Level (int MAX_ROW, int MAX_COL){
        this.walls = new boolean[MAX_ROW][MAX_COL];
        this.MAX_ROW = MAX_ROW;
        this.MAX_COL = MAX_COL;
        this.goalsByChar = new HashMap<>();
        this.boxesByChar = new HashMap<>();
        this.boxesByColor = new HashMap<>();
        this.allBoxes = new HashSet<>();
        this.allCharCells = new HashSet<>();
    }

    public static Level createInstance(int MAX_ROW, int MAX_COL) {
        if(instance == null) {
            instance = new Level(MAX_ROW, MAX_COL);
        }
        return instance;
    }

    public static Level getInstance(){
        return instance;
    }

    public void setWall(boolean wall, int row, int col){
        this.walls[row][col] = wall;
    }

    public boolean[][] getWalls() {
        return this.walls;
    }
    
    public HashSet<Box> getAllBoxes(){
        return this.allBoxes;
    }

    public void addCharCell(CharCell charCell){
        if (this.goalsByChar.containsKey(charCell.getLetter())){
            HashSet<CharCell> charSet = goalsByChar.get(charCell.getLetter());
            charSet.add(charCell);
            this.allCharCells.add(charCell);
        }else{
            HashSet<CharCell> charSet = new HashSet<>();
            charSet.add(charCell);
            this.allCharCells.add(charCell);
            this.goalsByChar.put(charCell.getLetter(), charSet);
        }
    }

    public void addBox(Box box){
        char boxChar = box.getBoxChar();
        Color boxColor = box.getBoxColor();
        this.allBoxes.add(box);
        if(boxesByChar.containsKey(boxChar)){
            HashSet<Box> boxesChar = this.boxesByChar.get(boxChar);
            boxesChar.add(box);
        }else{
            HashSet<Box> boxesChar = new HashSet<>();
            boxesChar.add(box);
            this.boxesByChar.put(boxChar, boxesChar);
        }
        if(boxesByColor.containsKey(boxColor)){
            HashSet<Box> boxesColor = this.boxesByColor.get(boxColor);
            boxesColor.add(box);
        }else{
            HashSet<Box> boxesColor = new HashSet<>();
            boxesColor.add(box);
            this.boxesByColor.put(boxColor, boxesColor);
        }
    }

    public void setAgentInColorMap(Agent agent) {
        if (this.agentsColorMap.containsKey(agent.getColor())){
            this.agentsColorMap.get(agent.getColor()).add(agent);
            allAgents.add(agent);
        }else{
            List<Agent> agentList = new ArrayList<>();
            agentList.add(agent);
            this.agentsColorMap.put(agent.getColor(), agentList);
            allAgents.add(agent);
        }
    }

    public List<Agent> getAllAgents (){
        return this.allAgents;
    }

    public HashMap<Color, List<Agent>> getAgentsColorMap() {
        return this.agentsColorMap;
    }

    public HashSet<Box> getBoxesByChar(Character chr){
        return boxesByChar.get(chr);
    }

    public HashSet<CharCell> getAllCharCells() {
        return allCharCells;
    }
}
