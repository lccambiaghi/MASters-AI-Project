package communication;

import communicationclient.Node;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by salik on 24-05-2017.
 */
public class MsgContent {
    private LinkedList<Node> content;
    private ArrayList<RessourceRequest> ressourceRequests;

    public MsgContent(LinkedList<Node> content) {
        this.content = content;
    }

    public LinkedList<Node> getContent() {
        return content;
    }

    public void setContent(LinkedList<Node> content) {
        this.content = content;
    }

    public ArrayList<RessourceRequest> getRessourceRequests() {
        return ressourceRequests;
    }

    public void setRessourceRequests(ArrayList<RessourceRequest> ressourceRequests) {
        this.ressourceRequests = ressourceRequests;
    }
}
