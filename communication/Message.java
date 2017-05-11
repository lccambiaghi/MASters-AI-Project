package communication;

import communicationclient.Node;

import java.util.LinkedList;

/**
 * Created by lucacambiaghi on 17/04/2017.
 */
public class Message {

    private MsgType type;

    private int contentStart;
    private LinkedList<Node> content;

    private char sender;

    public Message (MsgType type, LinkedList<Node> content, char agentID){
        this.type = type;
        this.content = content;
        this.sender = agentID;
    }

    public LinkedList<Node> getContent() {
        return content;
    }

    public char getSender() {
        return sender;
    }

    public MsgType getType() {
        return type;
    }

    public int getContentStart() {
        return contentStart;
    }

    public void setContentStart(int contentStart) {
        this.contentStart = contentStart;
    }
}
