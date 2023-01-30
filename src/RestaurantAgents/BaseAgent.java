package RestaurantAgents;

import Utility.ACLMessageFIPAUtil;
import gui.SplashScreen;
import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseAgent extends Agent {

    // it's the base role of the current/selected agent
    protected String agentRole;
    public  static ArrayList<String> chefNamesList = new ArrayList<>();
    public static ArrayList<String> waitersNamesList = new ArrayList<>();
    public static ArrayList<String> customerNamesList = new ArrayList<>();

    // this is the abstract method to add waiters into the restaurant which implemented by indidually by the child agent class
    // list contains total waiters in the restaurants which communicates with different customers
    public abstract void waitersAddInTheRestaurant(AID[] addNewWaitors);

    public void sendMessageUsingACLFIPA(AID aid, int performative, String protocol, String conversationID, String content) {
        ACLMessage aclMessage = ACLMessageFIPAUtil.getAclMessageObject(aid, performative, protocol, conversationID, content);
        send(aclMessage);
    }

    // just show the messages to  the GUI and CLI
    public void showMessageInGUIAndCLI(String message) {
        String responseMessage = "[" + agentRole + " " + getAID().getLocalName() + "] => " + message;
        System.out.println(responseMessage);
        SplashScreen.getInstance().communicationGUI.writeMessagesOntheGUI(responseMessage);
    }

    // This is the listener for UI which updates the UI whenever some communication happens between agents
    public interface UiMessageListener {
        void writeMessagesOntheGUI(String msg);

        void writeWaiterAgentNameOnTheGUI(List<String> names);

        void writeCustomerAgentNameOnTheGUI(List<String> names);

        void writeChefAgentNameOnTheGUI(List<String> names);
    }
}
