package Service.WaiterService;

import AppConstant.AppConstant;
import AppConstant.DefaultMessages;
import RestaurantAgents.Waiter;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ServingCustomer extends CyclicBehaviour {

    private Waiter waiterAgent;

    public ServingCustomer(Waiter waiterAgent) {
        this.waiterAgent = waiterAgent;
    }

    // perform someAction when taking order from customer
    @Override
    public void action() {
        // use template
        MessageTemplate template = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                MessageTemplate.MatchConversationId(AppConstant.WaiterRequestConversationGroupID));
        ACLMessage aclMessage = waiterAgent.receive(template);

        if (aclMessage != null)
            takingOrderFromCustomer(aclMessage);
        else
            block();
    }

    // if waiter is not busy then he will take the order from the customer
    private void takingOrderFromCustomer(ACLMessage msg) {
        if (waiterAgent.isBusy()) { // Waiter is bust at the moment
            waiterAgent.showMessageInGUIAndCLI(String.format(DefaultMessages.W_msg_Waiter_Busy, msg.getSender().getLocalName()));
            waiterAgent.sendMessageUsingACLFIPA(msg.getSender(), ACLMessage.REFUSE, FIPANames.InteractionProtocol.FIPA_REQUEST,
                    msg.getConversationId(), "busy");
        } else { // waiter is free and serving customer
            waiterAgent.setCustomerID(msg.getSender());
            waiterAgent.showMessageInGUIAndCLI(String.format(DefaultMessages.W_msg_Waiter_Serve, msg.getSender().getLocalName()));
            waiterAgent.sendMessageUsingACLFIPA(msg.getSender(), ACLMessage.AGREE, FIPANames.InteractionProtocol.FIPA_REQUEST,
                    msg.getConversationId(), "ok");
            waiterAgent.addBehaviour(new PlacingOrder(waiterAgent));
            waiterAgent.showMessageInGUIAndCLI(DefaultMessages.W_msg_Taking_Order);
            waiterAgent.sendMessageUsingACLFIPA(msg.getSender(), ACLMessage.INFORM, FIPANames.InteractionProtocol.FIPA_REQUEST,
                    msg.getConversationId(), "proceed");
        }
    }
}
