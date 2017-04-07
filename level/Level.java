package level;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by salik on 31-03-2017.
 */
public class Level {

    private static Level instance;
    private HashMap<Character, HashSet<Goal>> goalsByChar;
    private HashMap<GoalType, HashSet<Goal>> goalsByType;
    private HashMap<Character, HashSet<Box>> boxesByChar;
    private HashMap<Color, HashSet<Box>> boxesByColor;
    private HashSet<Box> allBoxes;
    private HashSet<Goal> allGoals;
    private boolean[][] walls;
    public int MAX_ROW;
    public int MAX_COL;
    private int NUM_GOALS = 0;

    private Level (int MAX_ROW, int MAX_COL){
        this.walls = new boolean[MAX_ROW][MAX_COL];
        this.MAX_ROW = MAX_ROW;
        this.MAX_COL = MAX_COL;
        this.goalsByChar = new HashMap<>();
        this.goalsByType = new HashMap<>();
        this.boxesByChar = new HashMap<>();
        this.boxesByColor = new HashMap<>();
        this.allBoxes = new HashSet<>();
        this.allGoals = new HashSet<>();
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

    public void addCharGoal(Goal goal){
        if (this.goalsByChar.containsKey(goal.getGoalChar())){
            HashSet<Goal> charSet = goalsByChar.get(goal.getGoalChar());
            charSet.add(goal);
            this.allGoals.add(goal);
        }else{
            HashSet<Goal> charSet = new HashSet<>();
            charSet.add(goal);
            this.allGoals.add(goal);
            this.goalsByChar.put(goal.getGoalChar(), charSet);
        }

        if (goalsByType.containsKey(GoalType.BoxToGoal)){
            HashSet<Goal> typeSet = goalsByType.get(GoalType.BoxToGoal);
            typeSet.add(goal);
            this.allGoals.add(goal);
        }else{
            HashSet<Goal> typeSet = new HashSet<>();
            typeSet.add(goal);
            this.allGoals.add(goal);
            this.goalsByType.put(GoalType.BoxToGoal, typeSet);
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

    public HashSet<Box> getBoxesByColor(Color color){
        return boxesByColor.get(color);
    }

    public HashSet<Box> getBoxesByChar(Character chr){
        return boxesByChar.get(chr);
    }

    public HashSet<Goal> getGoalsByChar(Character chr){
        return goalsByChar.get(chr);
    }

    public HashSet<Goal> getAllGoals() {
        return allGoals;
    }
}