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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Cell cell = (Cell) o;

        if (col != cell.col) return false;
        return row == cell.row;
    }

    @Override
    public int hashCode() {
        int result = col;
        result = 31 * result + row;
        return result;
    }
}
