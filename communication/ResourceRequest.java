package communication;

import graph.Vertex;

/**
 * Created by salik on 24-05-2017.
 */
public class ResourceRequest {
    private int timestep;
    private Vertex requestedCell;

    public ResourceRequest(int timestep, Vertex requestedCell) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ResourceRequest that = (ResourceRequest) o;

        if (timestep != that.timestep) return false;
        return requestedCell != null ? requestedCell.equals(that.requestedCell) : that.requestedCell == null;
    }

    @Override
    public int hashCode() {
        int result = timestep;
        result = 31 * result + (requestedCell != null ? requestedCell.hashCode() : 0);
        return result;
    }
}
