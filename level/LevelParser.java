package level;

import communicationclient.Agent;
import communicationclient.Strategy;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by salik on 07-04-2017.
 * Class for parsing the level as it is given from the environment-server or reading from a file
 */
public class LevelParser {

    private BufferedReader in;
    private List<Agent> agents = new ArrayList<>();
    private Strategy strategy;
    private boolean debug;

    public LevelParser(Strategy strategy, boolean debug) throws FileNotFoundException {
        this.strategy = strategy;
        this.debug = debug;
        if(this.debug){
            //For Debugging
            FileInputStream fis = null;
            fis = new FileInputStream("levels/MAsimple2.lvl");
            in = new BufferedReader(new InputStreamReader(fis));
        }else{
            in = new BufferedReader(new InputStreamReader(System.in));
        }
    }

    /**
     * Creates level, sets walls and boxes with colours
     * Creates agents with colours
     */
    public void readMap() throws IOException {
        HashMap<Character, Color> colors = new HashMap<>();
        Color color;
        int MAX_COL = 0;
        int MAX_ROW = 0;
        int row = 0;
        ArrayList<String> map = new ArrayList<>();
        String line = in.readLine();

        if(this.debug){
            while(line != null){
                map.add(line);
                if(line.length() > MAX_COL) MAX_COL = line.length();
                line = in.readLine();
                row++;
                MAX_ROW = row;
            }
        }else{
            while(!line.equals("")) {
                map.add(line);
                if(line.length() > MAX_COL) MAX_COL = line.length();
                line = in.readLine();
                row++;
                MAX_ROW = row;
            }
        }


        Level level = Level.createInstance(MAX_ROW, MAX_COL);

        System.err.println(" ");
        System.err.println("Printing scanned map");

        for (String lineInMap: map) {
            System.err.println(lineInMap);
        }

        System.err.println(" ");

        row = 0;
        boolean colorLevel = false;

        for (String lineInMap: map) {
            // if line is a color declaration, MA level -> colors get mapped
            if (lineInMap.matches("^[a-z]+:\\s*[0-9A-Z](,\\s*[0-9A-Z])*\\s*$")) {
                colorLevel = true;
                lineInMap = lineInMap.replaceAll("\\s", "");
                color = Color.valueOf(lineInMap.split( ":" )[0]);
                for (String id : lineInMap.split(":")[1].split(","))
                    colors.put(id.charAt(0), color);
            } else {
                // if SA, map of colors is empty -> all colors set to blue.
                for (int col = 0; col < lineInMap.length(); col++) {
                    char chr = lineInMap.charAt(col);
                    if (chr == '+') { // Wall.
                        level.setWall(true, row, col);
                    } else if ('A' <= chr && chr <= 'Z') { // Box.
                        Box box = new Box(col, row, chr, Color.blue);
                        if(colorLevel) {
                            Color boxColor = colors.get(chr);
                            box.setColor(boxColor);
                        }
                        level.addBox(box);
                    } else if ('a' <= chr && chr <= 'z') { // CharCell.
                        CharCell charCell = new CharCell(col, row, chr);
                        level.addCharCell(charCell);
                    } else if (chr == ' ') {
                        // Free space.
                    }else if ('0' <= chr && chr <= '9') {
                        Agent newAgent = new Agent(chr, Color.blue, this.strategy);
                        if(colorLevel) {
                            Color agentColor = colors.get(chr);
                            newAgent.setColor(agentColor);
                        }
                        newAgent.setAgentRow(row);
                        newAgent.setAgentCol(col);
                        agents.add(newAgent);
                        System.err.println("Agent " + newAgent.getId() + " created, Color is " + newAgent.getColor().toString());
                    }
                }
                row++;
            }
        }

        System.err.println("*--------------------------------------*");

    }
    public List<Agent> getAgents(){
        return agents;
    }
}
