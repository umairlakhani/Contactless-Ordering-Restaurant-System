package Service.CustomerService;

import AppConstant.AppConstant;
import AppConstant.DefaultMessages;
import RestaurantAgents.Customer;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.SimpleAchieveREResponder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

public class CustomerRecieveDish extends SimpleAchieveREResponder {

    private Customer customerAgent;

    // constructor
    CustomerRecieveDish(Customer c, MessageTemplate mt) {
        super(c, mt);
        customerAgent = c;
    }

    // Customer recieve meal and giving some tip
    @Override
    protected ACLMessage prepareResponse(ACLMessage request) {
        ACLMessage requestReplyACL = request.createReply();
        requestReplyACL.setPerformative(ACLMessage.AGREE);
        requestReplyACL.setConversationId(AppConstant.MealDeliveryConversationGroupID);
        requestReplyACL.setContent("ok");

        customerAgent.showMessageInGUIAndCLI(DefaultMessages.C_msg_Thank_You);
        customerAgent.showMessageInGUIAndCLI(DefaultMessages.C_msg_Payment);
        return requestReplyACL;
    }

    // Tip waiter
    // Customer always tips, although it can tip low
    @Override
    protected synchronized ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) {
        double tipAmount = getTipFromTheFormula();

        ACLMessage notificationAclMessage = request.createReply();
        notificationAclMessage.setPerformative(ACLMessage.INFORM);
        notificationAclMessage.setConversationId(AppConstant.MealDeliveryConversationGroupID);
        notificationAclMessage.setContent(tipAmount + "-" + customerAgent.getInitialMood() + "-" + customerAgent.getMood());

        customerAgent.showMessageInGUIAndCLI(String.format(DefaultMessages.C_msg_tip, tipAmount));

        customerAgent.addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                customerAgent.doDelete();
            }
        });

        return notificationAclMessage;
    }

    // ================================= CUSTOM METHODS =======================================

    // calcluate the tip everytime different
    private double getTipFromTheFormula() {
        Random random = new Random();
        // Maximum tipAmount: 5.99
        double tipAmount = customerAgent.getMood() * 0.5 + 0.01 * random.nextInt(99);

        BigDecimal bd = BigDecimal.valueOf(tipAmount);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        tipAmount = bd.doubleValue();
        return tipAmount;
    }
}
