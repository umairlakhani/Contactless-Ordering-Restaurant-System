package Service.WaiterService;

import AppConstant.AppConstant;
import AppConstant.DefaultMessages;
import RestaurantAgents.Waiter;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import model.WaiterArg;

public class CustomerReplyToWaiter extends CyclicBehaviour {

    private Waiter waiterAgent;

    //Constructor
    public CustomerReplyToWaiter(Waiter waiterAgent) {
        this.waiterAgent = waiterAgent;
    }

    // The reason because of Waiter Lying
    // less dish quantity available
    @Override
    public void action() {
        ACLMessage msg;
        MessageTemplate template = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                MessageTemplate.MatchConversationId(AppConstant.dishDetailsConversationGroupID));

        msg = myAgent.receive(template);

        if (msg != null) {
            WaiterArg<AID, Boolean> requestingWaiter = waiterAgent.getWaiter(msg.getSender());

            // try another waiter
            if (requestingWaiter != null && !requestingWaiter.getValue()) {
                waiterAgent.showMessageInGUIAndCLI(DefaultMessages.C_msg_try_another_waiter);
                waiterAgent.sendMessageUsingACLFIPA(msg.getSender(), ACLMessage.REFUSE, FIPANames.InteractionProtocol.FIPA_REQUEST,
                        msg.getConversationId(), msg.getContent());
                return;
            }
            //  reply to waiter
            waiterAgent.showMessageInGUIAndCLI(DefaultMessages.C_msg_replying_to_waiter);
            waiterAgent.sendMessageUsingACLFIPA(msg.getSender(), ACLMessage.AGREE, FIPANames.InteractionProtocol.FIPA_REQUEST,
                    msg.getConversationId(), msg.getContent());
            waiterAgent.checkIfDishIsAvailableInTheKitchen(msg.getSender(), msg.getContent());
        } else
            block();
    }
}