package communication;

import communicationclient.Agent;
import level.*;

import java.util.*;

/**
 * MsgHub is where agentsColorMap post messages and see if they have any responses.
 *
 * Agents can invoke sendMessage(), broadcast(), getResponses()
 */
public class MsgHub {

    private static MsgHub instance;

    private List<Agent> allAgents;

    // in this map we store each message and its replies
    private HashMap<Message, Queue<Message>> messageMap;

    private MsgHub(List<Agent> agents){
        this.allAgents = agents;
        this.messageMap = new HashMap<>();
    }

    public void sendMessage(char to, Message message){
        Queue<Message> responses = new ArrayDeque<>();
        messageMap.put(message, responses);

        for(Agent agent : allAgents)
            if(agent.getId() == to)
                agent.receiveMessage(message); // the receiver will put the reply in the messageMap

    }

    public void broadcast(Message announcement) {
        Queue<Message> responses = new ArrayDeque<>();

        messageMap.put(announcement, responses);

        for(Agent agent: allAgents)
            agent.receiveMessage(announcement);

    }

    public static void createInstance(){
        List<Agent> agents = Level.getInstance().getAllAgents();
        if(instance == null) {
            instance = new MsgHub(agents);
        }
    }

    public void reply(Message message, Message response) {
        Queue<Message> responses = new ArrayDeque<>();
        responses.add(response);

        messageMap.put(message, responses);
    }

    public Queue<Message> getResponses(Message announcement) {
        return messageMap.get(announcement);
    }

    public static MsgHub getInstance() {
        return instance;
    }

    public List<Agent> getAllAgents (){
        return this.allAgents;
    }
}