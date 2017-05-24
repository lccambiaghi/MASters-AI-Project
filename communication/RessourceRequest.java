package communication;

import graph.Vertex;

/**
 * Created by salik on 24-05-2017.
 */
public class RessourceRequest {
    private int timestep;
    private Vertex requestedCell;

    public RessourceRequest(int timestep, Vertex requestedCell) {
        this.timestep = timestep;
        this.requestedCell = requestedCell;
    }

    public int getTimestep() {
        return timestep;
    }

    public void setTimestep(int timestep) {
        this.timestep = timestep;
    }

    public Vertex getRequestedCell() {
        return requestedCell;
    }

    public void setRequestedCell(Vertex requestedCell) {
        this.requestedCell = requestedCell;
    }
}
