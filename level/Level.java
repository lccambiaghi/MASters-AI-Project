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
//    private char[][] goals;
    public int MAX_ROW;
    public int MAX_COL;
    private int NUM_GOALS;

    private Level (int MAX_ROW, int MAX_COL){
        walls = new boolean[MAX_ROW][MAX_COL];
//        goals = new char[MAX_ROW][MAX_COL];
        this.MAX_ROW = MAX_ROW;
        this.MAX_COL = MAX_COL;
        NUM_GOALS = 0;
        goalsByChar = new HashMap<>();
        goalsByType = new HashMap<>();
        boxesByChar = new HashMap<>();
        boxesByColor = new HashMap<>();
        allBoxes = new HashSet<>();
        allGoals = new HashSet<>();
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
        walls[row][col] = wall;
    }

    public boolean[][] getWalls() {
        return walls;
    }
    public HashSet<Box> getAllBoxes(){
        return allBoxes;
    }
    public void addCharGoal(Goal goal){
        if (goalsByChar.containsKey(goal.getGoalChar())){
            HashSet<Goal> charSet = goalsByChar.get(goal.getGoalChar());
            charSet.add(goal);
            allGoals.add(goal);
        }else{
            HashSet<Goal> charSet = new HashSet<>();
            charSet.add(goal);
            allGoals.add(goal);
            goalsByChar.put(goal.getGoalChar(), charSet);
        }
        if (goalsByType.containsKey(GoalType.BoxToGoal)){
            HashSet<Goal> typeSet = goalsByType.get(GoalType.BoxToGoal);
            typeSet.add(goal);
            allGoals.add(goal);
        }else{
            HashSet<Goal> typeSet = new HashSet<>();
            typeSet.add(goal);
            allGoals.add(goal);
            goalsByType.put(GoalType.BoxToGoal, typeSet);
        }
    }
    public void addBox(Box box){
        char boxChar = box.getBoxChar();
        Color boxColor = box.getBoxColor();
        allBoxes.add(box);
        if(boxesByChar.containsKey(boxChar)){
            HashSet<Box> boxesChar = boxesByChar.get(boxChar);
            HashSet<Box> boxesColor = boxesByColor.get(boxColor);
            boxesChar.add(box);
            boxesColor.add(box);
        }else{
            HashSet<Box> boxesChar = new HashSet<>();
            HashSet<Box> boxesColor = new HashSet<>();
            boxesChar.add(box);
            boxesColor.add(box);
            boxesByChar.put(boxChar,boxesChar);
            boxesByColor.put(boxColor,boxesColor);
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
