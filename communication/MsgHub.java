package communication;

import communicationclient.Agent;
import level.*;

import java.util.*;

/**
 * MsgHub is where agents post messages and see if they have any responses.
 *
 * Agents can invoke sendMessage(), broadcast(), getResponses()
 */
public class MsgHub {

    private static MsgHub instance;

    private HashMap<Color, List<Agent>> agents;

    // in this map we store each message and its replies
    private HashMap<Message, Queue<Message>> messageMap;

    private MsgHub(HashMap<Color, List<Agent>> agents){
        this.agents = agents;
        messageMap = new HashMap<>();
    }

    public void sendMessage(char receiver, Message message){
        Queue<Message> responses = new ArrayDeque<>();
        messageMap.put(message, responses);
        for (List<Agent> agentList : this.agents.values()) {
            for (Agent agent : agentList) {
                if (agent.getId() == receiver)
                    agent.receiveAnnouncement(message);
            }
        }
    }
    public void broadcast(Message announcement) {
        Queue<Message> responses = new ArrayDeque<>();

        messageMap.put(announcement, responses);

        char sender = announcement.getSender();

        for (List<Agent> agentList : this.agents.values()) {
            for (Agent agent : agentList) {
                if (agent.getId() != sender)
                    agent.receiveAnnouncement(announcement);
            }
        }
    }

    public Queue<Message> getResponses(Message announcement) {
        return messageMap.get(announcement);
    }

    public static MsgHub createInstance(Level level){
        HashMap<Color, List<Agent>> agents = level.getAgentsByColorMap();
        if(instance == null) {
            instance = new MsgHub(agents);
        }
        return instance;
    }

    public static MsgHub getInstance() {
        return instance;
    }

    public void reply(Message message, Message response) {
        Queue<Message> responses = messageMap.get(message);
        responses.add(response);

        messageMap.put(message, responses);
    }
}