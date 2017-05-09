package level;

/**
 * Created by arhjo on 09/05/2017.
 */
public class Cell {
     int col;
     int row;

     public Cell(int row, int col){
         this.row = row;
         this.col = col;
     }

    public int getCol() {
        return col;
    }

    public int getRow() {
        return row;
    }
}
