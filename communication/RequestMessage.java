package communication;

import communicationclient.Node;
import level.Box;

import java.util.LinkedList;

/**
 * Created by arhjo on 09/05/2017.
 */
public class RequestMessage extends Message {
    Box box;
    public RequestMessage(MsgType type, Box box, LinkedList<Node> content, char agentID) {
        super(type, content, agentID);
        this.box = box;
    }
}
