package Utility;


import AppConstant.AppConstant;
import jade.core.AID;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;

public class ACLMessageFIPAUtil {

    public static ACLMessage getAclMessageObject(AID aid, int performative, String protocol, String conversationID, String content) {
        ACLMessage aclMessage = new ACLMessage(performative);
        aclMessage.addReceiver(aid);

        aclMessage.setLanguage(AppConstant.Langauage);
        aclMessage.setProtocol(protocol);

        aclMessage.setConversationId(conversationID);
        aclMessage.setContent(content);
        return aclMessage;
    }

    public static ACLMessage getACLMessageObjectForCustomerService(AID waiter, String content) {
        ACLMessage aclMessage = new ACLMessage(ACLMessage.CFP);
        aclMessage.addReceiver(waiter);

        aclMessage.setLanguage(AppConstant.Langauage);
        aclMessage.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);

        aclMessage.setConversationId(AppConstant.OrderRequestConversationGroupID);
        aclMessage.setContent(content); // Message: <Dish - Mood>
        return aclMessage;
    }

    public static ACLMessage getACLMessageObjectForAvailableWaiters(AID currentWaiter) {
        ACLMessage aclMessage = new ACLMessage(ACLMessage.REQUEST);
        aclMessage.addReceiver(currentWaiter);

        aclMessage.setLanguage(AppConstant.Langauage);// added extra
        aclMessage.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);

        aclMessage.setConversationId(AppConstant.WaiterRequestConversationGroupID);
        aclMessage.setContent("Be my waiter " + currentWaiter.getLocalName());
        return aclMessage;
    }
}
