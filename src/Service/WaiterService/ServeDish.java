package Service.WaiterService;

import AppConstant.AppConstant;
import AppConstant.DefaultMessages;
import RestaurantAgents.Waiter;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.text.DecimalFormat;

public class ServeDish extends WakerBehaviour {

    private AID customerInfo;
    private Waiter waiterAgent;
    private String dishName;

    ServeDish(Agent waiterAgent, long timeout, AID customerInfo, String[] dishName) {
        super(waiterAgent, timeout);

        this.waiterAgent = (Waiter) waiterAgent;
        this.customerInfo = customerInfo;
        this.dishName = dishName[0];
    }

    @Override
    public void onWake() {
        waiterAgent.showMessageInGUIAndCLI(String.format(DefaultMessages.W_msg_your_ordered, dishName, customerInfo.getLocalName()));
        waiterAgent.sendMessageUsingACLFIPA(customerInfo, ACLMessage.REQUEST, FIPANames.InteractionProtocol.FIPA_REQUEST,
                AppConstant.MealDeliveryConversationGroupID, dishName);
        customerFeedbackAndSuggestion();
    }

    // ================================= CUSTOM METHODS =======================================


    // getting tips from the cusomter after serving him / her
    private void gettingTipFromCustomerAfterServing(ACLMessage msg) {
        String[] contentsInArgs = msg.getContent().split("-");
        double tipAmount = Double.parseDouble(contentsInArgs[0]);
        waiterAgent.addTip(tipAmount);
        waiterAgent.showMessageInGUIAndCLI(String.format(DefaultMessages.W_msg_thanks_for_tip, customerInfo.getLocalName(), tipAmount));

        DecimalFormat decFormat = new DecimalFormat();
        decFormat.setMaximumFractionDigits(2);

        String totalTips = decFormat.format(waiterAgent.getTipAmount());
        waiterAgent.showMessageInGUIAndCLI(String.format(DefaultMessages.W_msg_Total_Tips, totalTips));
    }

    private void customerFeedbackAndSuggestion() {
        // First feedback template
        MessageTemplate template = MessageTemplate.and(MessageTemplate.MatchConversationId(AppConstant.MealDeliveryConversationGroupID),
                MessageTemplate.MatchPerformative(ACLMessage.AGREE));
        ACLMessage msg;

        do {
            msg = waiterAgent.receive(template);
            if (msg == null)
                block();
        }
        while (msg == null);

        // second feedback template
        ACLMessage secondMessage;
        MessageTemplate secondTemplate = MessageTemplate.and(MessageTemplate.MatchConversationId(AppConstant.MealDeliveryConversationGroupID),
                MessageTemplate.MatchPerformative(ACLMessage.INFORM));

        do {
            secondMessage = waiterAgent.receive(secondTemplate);
            if (secondMessage == null)
                block();
        }
        while (secondMessage == null);

        gettingTipFromCustomerAfterServing(secondMessage);
    }

}
