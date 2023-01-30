package Service.KitchenService;

import AppConstant.DefaultMessages;
import RestaurantAgents.Kitchen;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class DishOrderRequest extends CyclicBehaviour {

    private Kitchen kitchenAgent;

    //Constructor
    public DishOrderRequest(Kitchen kitchenAgent) {
        this.kitchenAgent = kitchenAgent;
    }

    // working on the dish which waiter conveys
    @Override
    public void action() {
        ACLMessage request = kitchenAgent.receive();

        if (request != null) {
            ACLMessage requestReply = request.createReply();
            String meal = request.getContent();
            String mealString;

            requestReply.setConversationId(request.getConversationId());
            requestReply.setPerformative(ACLMessage.AGREE);
            requestReply.setContent("ok");
            kitchenAgent.send(requestReply);

            if (kitchenAgent.checkMeal(meal)) { // if meal exist
                int[] mealInfo = kitchenAgent.getMealInfo(meal);

                if (request.getConversationId().equals("start-dish")) {
                    mealInfo[0]--;
                    kitchenAgent.getDishAllInformation().put(meal, mealInfo);
                }

                mealString = meal + " - " + mealInfo[0] + " - " + mealInfo[1] + " - " + mealInfo[2];

                requestReply.setContent(mealString);
                requestReply.setPerformative(ACLMessage.INFORM);
                kitchenAgent.showMessageInGUIAndCLI(String.format(DefaultMessages.K_msg_take_your_dish, mealString));
            } else { // if meal not exist
                requestReply.setContent(meal);
                requestReply.setPerformative(ACLMessage.FAILURE);
                kitchenAgent.showMessageInGUIAndCLI(DefaultMessages.K_msg_unavailable_dish);
            }

            kitchenAgent.send(requestReply);
        } else
            block();
    }
}