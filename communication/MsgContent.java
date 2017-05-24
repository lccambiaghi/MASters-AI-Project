package communication;

import communicationclient.Node;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by salik on 24-05-2017.
 */
public class MsgContent {
    private LinkedList<Node> content;
    private ArrayList<ResourceRequest> resourceRequests;

    public MsgContent(LinkedList<Node> content) {
        this.content = content;
    }

    public LinkedList<Node> getContent() {
        return content;
    }

    public void setContent(LinkedList<Node> content) {
        this.content = content;
    }

    public ArrayList<ResourceRequest> getResourceRequests() {
        return resourceRequests;
    }

    public void setResourceRequests(ArrayList<ResourceRequest> resourceRequests) {
        this.resourceRequests = resourceRequests;
    }
}
