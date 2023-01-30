package Service.WaiterService;

import AppConstant.AppConstant;
import AppConstant.DefaultMessages;
import RestaurantAgents.Waiter;
import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import model.Dish;
import model.WaiterArg;

public class PlacingOrder extends SimpleBehaviour {

    private Waiter waiterAgent;
    private int step = 1; // start from the fist step
    private int moodOfCustomer;
    private int rejectDishCounter = 0;

    public PlacingOrder(Waiter waiterAgent) {
        this.waiterAgent = waiterAgent;
    }

    @Override
    public void action() {
        ACLMessage aclMsg;
        MessageTemplate msgTemplate;

        switch (step) {
            case 1:
                //Take order from the customer
                msgTemplate = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.CFP),
                        MessageTemplate.MatchConversationId(AppConstant.OrderRequestConversationGroupID));
                aclMsg = waiterAgent.receive(msgTemplate);

                if (aclMsg != null)
                    takeOrder(aclMsg);
                else
                    block();
                break;

            case 2:
                //Get the dish details from the other Waitor
                msgTemplate = MessageTemplate.and(MessageTemplate.MatchConversationId(AppConstant.dishDetailsConversationGroupID),
                        MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.FAILURE),
                                MessageTemplate.MatchPerformative(ACLMessage.INFORM)));
                aclMsg = waiterAgent.receive(msgTemplate);

                if (aclMsg != null)
                    receiveDishDetails(aclMsg);
                else
                    block();

                break;

            case 3:
                //Take feedback from the customer
                msgTemplate = MessageTemplate.MatchConversationId(AppConstant.dishFeedbackConversationGroupID);
                aclMsg = waiterAgent.receive(msgTemplate);

                if (aclMsg != null) //Message: <dish>
                    takeCustomerFeedback(aclMsg);
                else
                    block();

                break;

            case 4:
                //Get feedback from the kitchen that dish is available or not
                msgTemplate = MessageTemplate.MatchConversationId(AppConstant.startDishConversationGroupID);
                aclMsg = waiterAgent.receive(msgTemplate);

                if (aclMsg != null) //Message: <dish quantity time prep>
                    getKitchenFinalCheck(aclMsg);
                else
                    block();
                break;

            case 5:
                msgTemplate = MessageTemplate.and(
                        MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.AGREE), MessageTemplate.MatchPerformative(ACLMessage.REFUSE)),
                        MessageTemplate.or(MessageTemplate.MatchConversationId(AppConstant.dishDetailsConversationGroupID), MessageTemplate.MatchConversationId("start-dish")));

                aclMsg = waiterAgent.receive(msgTemplate);

                if (aclMsg != null)
                    recieveInformationAckowledgment(aclMsg);
                else
                    block();
                break;
        }
    }

    // steps done or rejectoion goes to 3 then return
    @Override
    public boolean done() {
        return step == 6 || rejectDishCounter >= 3;
    }

    private void getKitchenFinalCheck(ACLMessage msg) {
        String[] desDishInfo = msg.getContent().split(" - ");
        Dish desiredDish = waiterAgent.getKnownDish(desDishInfo[0]);

        if (desiredDish != null && !desiredDish.compareStaticDetails(desDishInfo[0], Integer.parseInt(desDishInfo[2]),
                Integer.parseInt(desDishInfo[3]))) {
            WaiterArg<AID, Boolean> otherWaiter = waiterAgent.getWaiter(desiredDish.getAidInformationSource());

            if (otherWaiter != null) {
                otherWaiter.setValue(false);
                waiterAgent.showMessageInGUIAndCLI("*Thinking* " + otherWaiter.getKey().getLocalName() + " was lying, I'll take note of that...");
            }
        }

        if (desiredDish != null) {
            desiredDish.setQuantity(Integer.parseInt(desDishInfo[1]));
            desiredDish.setNormalTasteCookTiming(Integer.parseInt(desDishInfo[2]));
            desiredDish.setDeliciousTasteCookTiming(Integer.parseInt(desDishInfo[3]));
            desiredDish.setAidInformationSource(waiterAgent.getKitchenInfo());
        }

        /// refuse by the agent
        if (msg.getPerformative() == ACLMessage.REFUSE) {
            step = 1;
            waiterAgent.showMessageInGUIAndCLI(String.format(DefaultMessages.W_msg_dish_not_available, desDishInfo[0]));
            waiterAgent.sendMessageUsingACLFIPA(waiterAgent.getCustomerID(), ACLMessage.FAILURE, FIPANames.InteractionProtocol.FIPA_CONTRACT_NET,
                    AppConstant.OrderRequestConversationGroupID, "unavailable");
        } else { // meal is in process
            step = 6;
            waiterAgent.showMessageInGUIAndCLI(DefaultMessages.W_msg_meal_in_process);
            waiterAgent.sendMessageUsingACLFIPA(waiterAgent.getCustomerID(), ACLMessage.INFORM, FIPANames.InteractionProtocol.FIPA_CONTRACT_NET,
                    AppConstant.OrderRequestConversationGroupID, desDishInfo[2] + " - " + desDishInfo[3]);
            waiterAgent.addBehaviour(new ServeDish(myAgent, Long.parseLong(desDishInfo[2]) * 1000,
                    waiterAgent.getCustomerID(), desDishInfo));
            waiterAgent.setCustomerID(null);
        }
    }

    private void takeCustomerFeedback(ACLMessage aclMessage) {
        String[] msgDet = aclMessage.getContent().split(" - ");

        if (aclMessage.getPerformative() == ACLMessage.ACCEPT_PROPOSAL
                || (msgDet.length > 1 && msgDet[1].equals("original"))) {
            Dish dish = waiterAgent.getKnownDish(msgDet[0]);
            step = 5;
            dish.decrementAvailability();
            waiterAgent.showMessageInGUIAndCLI(DefaultMessages.W_msg_right_away);
            waiterAgent.sendMessageUsingACLFIPA(waiterAgent.getKitchenInfo(), ACLMessage.REQUEST, FIPANames.InteractionProtocol.FIPA_REQUEST,
                    AppConstant.startDishConversationGroupID, dish.getName());
        } else {
            step = 1;
            rejectDishCounter++;
        }

    }

    private void dishEvaluationProcess(Dish dish, String infoSource) {
        //Customer mood += cookingTime - 5 | Customer mood += DishPreparation - 5
        Dish dishSuggestion;

        if (dish.getQuantity() == 0) { // dish is not available and it's ended
            step = 1;
            waiterAgent.showMessageInGUIAndCLI(String.format(DefaultMessages.W_msg_all_dish_ended + dish.getName()));
            waiterAgent.sendMessageUsingACLFIPA(waiterAgent.getCustomerID(), ACLMessage.REFUSE,
                    FIPANames.InteractionProtocol.FIPA_CONTRACT_NET, AppConstant.OrderRequestConversationGroupID, "unavailable");
        }
        // suggesting the dish to the customer
        else if ((moodOfCustomer - dish.getNormalTasteCookTiming() - 5 <= 3 || moodOfCustomer + dish.getDeliciousTasteCookTiming() - 5 <= 3)
                && (dishSuggestion = waiterAgent.suggestOtherDish(dish, moodOfCustomer)) != null) {

            //String suggestionInfoSrc = dishSuggestion.isReliable() ? "kitchen" : "waiter";

            step = 3;
            waiterAgent.showMessageInGUIAndCLI(String.format(DefaultMessages.W_msg_dish_suggesting, dishSuggestion.getName()));
            waiterAgent.sendMessageUsingACLFIPA(waiterAgent.getCustomerID(), ACLMessage.PROPOSE,
                    FIPANames.InteractionProtocol.FIPA_CONTRACT_NET, AppConstant.OrderRequestConversationGroupID,
                    dishSuggestion.getName() + " - " + infoSource);
        } else { // dish is available and waiter took the order
            step = 3;
            waiterAgent.showMessageInGUIAndCLI(DefaultMessages.W_msg_choice_is_good);
            waiterAgent.sendMessageUsingACLFIPA(waiterAgent.getCustomerID(), ACLMessage.PROPOSE,
                    FIPANames.InteractionProtocol.FIPA_CONTRACT_NET, AppConstant.OrderRequestConversationGroupID,
                    dish.getName() + " - " + infoSource);
        }
    }

    private void recieveInformationAckowledgment(ACLMessage msg) {
        if (msg.getPerformative() == ACLMessage.AGREE) {
            if (msg.getConversationId().equals(AppConstant.dishDetailsConversationGroupID))
                step = 2;
            else
                step = 4;
        } else {
            if (!msg.getSender().equals(waiterAgent.getKitchenInfo())) {
                AID nextAgent = waiterAgent.getAnotherReliableWaiterForCustomer();

                if (nextAgent == null) {
                    nextAgent = waiterAgent.getKitchenInfo();
                    waiterAgent.showMessageInGUIAndCLI(DefaultMessages.W_msg_asking_to_kitchen);
                } else
                    waiterAgent.showMessageInGUIAndCLI(String.format(DefaultMessages.W_msg_hows_you_withOK, nextAgent.getLocalName()));

                step = 5;
                waiterAgent.sendMessageUsingACLFIPA(nextAgent, ACLMessage.REQUEST,
                        FIPANames.InteractionProtocol.FIPA_REQUEST, AppConstant.dishDetailsConversationGroupID, msg.getContent());
            } else
                System.out.println(DefaultMessages.W_msg_RefusedRequest);
        }
    }

    private void receiveDishDetails(ACLMessage msg) {
        String content = msg.getContent();

        if (msg.getPerformative() == ACLMessage.FAILURE) {
            if (msg.getSender().equals(waiterAgent.getKitchenInfo())) {
                step = 1;
                waiterAgent.showMessageInGUIAndCLI(DefaultMessages.W_msg_Dish_Is_Not_Serve_Here);
                waiterAgent.sendMessageUsingACLFIPA(waiterAgent.getCustomerID(), ACLMessage.REFUSE,
                        FIPANames.InteractionProtocol.FIPA_CONTRACT_NET, AppConstant.OrderRequestConversationGroupID, "not-found");
                return;
            } else {
                AID nextAgent = waiterAgent.getAnotherReliableWaiterForCustomer();
                step = 5;

                if (nextAgent == null) {
                    waiterAgent.showMessageInGUIAndCLI(DefaultMessages.W_msg_Ask_To_Kitchen);
                    waiterAgent.sendMessageUsingACLFIPA(waiterAgent.getKitchenInfo(), ACLMessage.REQUEST,
                            FIPANames.InteractionProtocol.FIPA_REQUEST, AppConstant.dishDetailsConversationGroupID, msg.getContent());
                } else {
                    waiterAgent.showMessageInGUIAndCLI(String.format(DefaultMessages.W_msg_Hows_you, nextAgent.getLocalName()));
                    waiterAgent.sendMessageUsingACLFIPA(nextAgent, ACLMessage.REQUEST,
                            FIPANames.InteractionProtocol.FIPA_REQUEST, AppConstant.dishDetailsConversationGroupID, msg.getContent());
                }

                return;
            }
        }

        String[] dishDetails = content.split(" - "); //Message format: "dish - availability - cookingTime - preparationRate"

        if (dishDetails.length < 4)
            System.out.println(msg.getSender().getLocalName() + " | " + msg.getPerformative() + " | " + content);

        Dish dish = new Dish(dishDetails[0], Integer.parseInt(dishDetails[1]), Integer.parseInt(dishDetails[2]),
                Integer.parseInt(dishDetails[3]), msg.getSender());
        String infoSrc;

        if (waiterAgent.getKnownDishes().contains(dish))
            waiterAgent.updateKnownDish(dish);
        else
            waiterAgent.getKnownDishes().add(dish);

        if (waiterAgent.isDishInfoReliable(dish))
            infoSrc = "kitchen";
        else
            infoSrc = "waiter";

        dishEvaluationProcess(dish, infoSrc);
    }

    private void takeOrder(ACLMessage msg) {
        String[] customerDetails = msg.getContent().split(" - "); //Message: <Dish - Mood>
        String dish = customerDetails[0];

        int index;

        waiterAgent.resetWaiterIndex();
        moodOfCustomer = Integer.parseInt(customerDetails[1]);

        if ((index = waiterAgent.getKnownDishIndex(customerDetails[0])) == -1) {
            step = 5;

            // examining mood
            if (moodOfCustomer <= 5 && waiterAgent.getWaitersList().size() > 0) {
                waiterAgent.showMessageInGUIAndCLI(DefaultMessages.W_msg_ask_to_Colleague); // ask to colleague regaring the dish
                waiterAgent.sendMessageUsingACLFIPA(waiterAgent.getAnotherReliableWaiterForCustomer(), ACLMessage.REQUEST,
                        FIPANames.InteractionProtocol.FIPA_REQUEST, AppConstant.dishDetailsConversationGroupID, dish);
            } else {
                moodOfCustomer--;
                waiterAgent.showMessageInGUIAndCLI(DefaultMessages.W_msg_ask_to_kitchen_staff); // ASK to kitchen
                waiterAgent.sendMessageUsingACLFIPA(waiterAgent.getKitchenInfo(), ACLMessage.REQUEST, FIPANames.InteractionProtocol.FIPA_REQUEST,
                        AppConstant.dishDetailsConversationGroupID, dish);
            }
        } else
            dishEvaluationProcess(waiterAgent.getKnownDishes().get(index), "kitchen");
    }
}