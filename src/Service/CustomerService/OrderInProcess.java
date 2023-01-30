package Service.CustomerService;

import AppConstant.AppConstant;
import AppConstant.DefaultMessages;
import RestaurantAgents.Customer;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetInitiator;

import java.util.Random;

public class OrderInProcess extends ContractNetInitiator {

    private Customer customerAgent;

    // Constructor
    public OrderInProcess(Customer customerAgent, ACLMessage aclMessage) {
        super(customerAgent, aclMessage);
        this.customerAgent = customerAgent;
    }

    // handle the order requests weather it is acceept/reject or inform/Failure
    @Override
    protected void handleOutOfSequence(ACLMessage msg) {
        switch (msg.getPerformative()) {
            case ACLMessage.REFUSE:
                handleRefuse(msg);
                break;
            case ACLMessage.PROPOSE:
                handlePropose(msg);
                break;
            case ACLMessage.FAILURE:
                handleFailure(msg);
                break;
            case ACLMessage.INFORM:
                handleInform(msg);
                break;
            default:
                break;
        }
    }


    // if order is not available
    @Override
    protected void handleRefuse(ACLMessage msg) {
        orderAgain();
    }

    // if some failure occurs in aclmessage
    @Override
    protected void handleFailure(ACLMessage failure) {
        handleRefuse(failure);
    }

    // inform regarding the meal delivery
    @Override
    protected void handleInform(ACLMessage inform) {
        String[] mealInfo = inform.getContent().split(" - "); // < preparationTime - wellCooked >
        int preparationTime = Integer.parseInt(mealInfo[0]); // 1 to 10, the higher the value, the lower the mood
        int wellCooked = Integer.parseInt(mealInfo[1]); // 1 to 10, the lower the value, the lower the mood

        if (preparationTime < 5) {
            customerAgent.incrementMood();
        } else if (preparationTime > 5) {
            customerAgent.decrementMood();
        }

        if (wellCooked < 5) {
            customerAgent.decrementMood();
        } else if (wellCooked > 5) {
            customerAgent.incrementMood();
        }

        CustomerRecieveDish customerRecieveDish = new CustomerRecieveDish(customerAgent, MessageTemplate.and(MessageTemplate.MatchSender(customerAgent.getWaiter()), MessageTemplate.and(MessageTemplate.MatchConversationId("meal-delivering"), MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST))));
        customerAgent.addBehaviour(customerRecieveDish);
        this.reset();
    }

    // ======================================= CUSTOM METHODS ===========================================

    // Order Again logic
    private void orderAgain() {
        if (customerAgent.getAttempts() >= 3) {
            customerAgent.showMessageInGUIAndCLI(DefaultMessages.C_msg_Enough_Tried);
            customerAgent.doDelete();
        } else {
            customerAgent.showMessageInGUIAndCLI(DefaultMessages.C_msg_Tried_Another_Order);
            customerAgent.incrementAttempts();
            customerAgent.orderDish();
        }
    }

    // The waiter give customer a new dish suggestion when desired dish not available
    private void handlePropose(ACLMessage propose) {
        String proposedDish = propose.getContent().split(" - ")[0];
        String infoSource = propose.getContent().split(" - ")[1];

        if (infoSource.equals("kitchen")) {
            customerAgent.decrementMood();
            customerAgent.showMessageInGUIAndCLI(DefaultMessages.C_msg_waiting_for_so_long);
        }

        if (!proposedDish.equals(customerAgent.getDesiredDish())) {
            Random random = new Random();
            int accept = random.nextInt(1);

            if (accept == 0) {// when you didn't get your desired dish and try another dish
                customerAgent.sendMessageUsingACLFIPA(propose.getSender(), ACLMessage.REJECT_PROPOSAL, FIPANames.InteractionProtocol.FIPA_CONTRACT_NET, AppConstant.dishFeedbackConversationGroupID, "no");
                this.reset();
                customerAgent.showMessageInGUIAndCLI(DefaultMessages.C_msg_irritate_order_not_available);
                orderAgain();
            } else {// when waiter accept another dish instead of your desired dish
                customerAgent.decrementMood();
                customerAgent.sendMessageUsingACLFIPA(propose.getSender(), ACLMessage.ACCEPT_PROPOSAL, FIPANames.InteractionProtocol.FIPA_CONTRACT_NET, AppConstant.dishFeedbackConversationGroupID, proposedDish);
                customerAgent.showMessageInGUIAndCLI(DefaultMessages.C_msg_happy_order_available);
            }

        } else { // When you get your desired dish
            customerAgent.showMessageInGUIAndCLI(DefaultMessages.C_msg_happy_when_desire_order_accept);
            customerAgent.sendMessageUsingACLFIPA(propose.getSender(), ACLMessage.ACCEPT_PROPOSAL, FIPANames.InteractionProtocol.FIPA_CONTRACT_NET, AppConstant.dishFeedbackConversationGroupID, customerAgent.getDesiredDish() + " - original");
        }
    }
}
