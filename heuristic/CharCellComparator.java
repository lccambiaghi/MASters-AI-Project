package heuristic;

import level.CharCell;

import java.util.Comparator;

/**
 * Created by salik on 19-04-2017.
 */
public class CharCellComparator implements Comparator<CharCell>{

    @Override
    public int compare(CharCell o1, CharCell o2) {
        return o2.getPriority() - o1.getPriority();//Higher priority are put on top/front
    }
}


