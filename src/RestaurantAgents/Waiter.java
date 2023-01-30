package RestaurantAgents;

import AppConstant.AppConstant;
import AppConstant.DefaultMessages;
import Service.WaiterServiceSearch;
import gui.SplashScreen;
import model.Dish;
import model.WaiterArg;
import Service.WaiterService.ServingCustomer;
import Service.WaiterService.CustomerReplyToWaiter;
import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Waiter extends BaseAgent {

    private AID kitchenInfo;
    private AID customerID;

    private ArrayList<Dish> knownDishes = new ArrayList<>();
    private ArrayList<WaiterArg<AID, Boolean>> waitersList = new ArrayList<>();


    private double tipAmount = 0;
    private boolean isHonestOrTrustFul;
    private int waiterIndex = 0;

    // to create the waiter with arguments
    @Override
    protected void setup() {
        agentRole = AppConstant.WaiterAgentClassName;

        Object[] args = getArguments();

        if (args.length != 1) { // if there is no argument
            System.out.println(DefaultMessages.W_msg_Argument_Error);
            doDelete();
            return;
        }

        isHonestOrTrustFul = Boolean.parseBoolean((String) args[0]);

        DFAgentDescription dfAgentDesc = new DFAgentDescription();
        ServiceDescription serviceDesc = new ServiceDescription();

        dfAgentDesc.setName(this.getAID());
        serviceDesc.setType(AppConstant.CustomerServiceName);
        serviceDesc.setName(AppConstant.RestaurantName);
        dfAgentDesc.addServices(serviceDesc);

        try {// register the Waiter
            DFService.register(this, dfAgentDesc);
        } catch (FIPAException e) {
            e.printStackTrace();
        }

        if (!isKitchenServiceAvailable()) // if kitchen not found then waiter wont be available
            this.doDelete();

        // set the behaviour of the waiter
        this.addBehaviour(new WaiterServiceSearch(this, 500));
        this.addBehaviour(new ServingCustomer(this));
        this.addBehaviour(new CustomerReplyToWaiter(this));

        // Waiter join the restaurant and working
        showMessageInGUIAndCLI(DefaultMessages.W_msg_Check_In);
        // it will add the waiter into the list of GUI
        if (!waitersNamesList.contains(getAID().getLocalName())) {
            waitersNamesList.add(getAID().getLocalName());
            SplashScreen.getInstance().communicationGUI.writeWaiterAgentNameOnTheGUI(waitersNamesList);
        }
    }

    // add new waiter in the restaurant
    @Override
    public void waitersAddInTheRestaurant(AID[] addNewWaitors) {
        boolean isFound;

        List<String> names = new ArrayList<>();
        for (AID newWaiter : addNewWaitors) {
            isFound = false;
            if (newWaiter.equals(this.getAID()))
                continue;
            for (WaiterArg<AID, Boolean> waiter : waitersList)
                if (waiter.getKey().equals(newWaiter)) {
                    isFound = true;
                    names.add(waiter.getKey().getLocalName());
//                    break;
                }

            if (!isFound) {
                waitersList.add(new WaiterArg<>(newWaiter, true));
            }
        }
        // it will add the waiter into the list of GUI
        if (!waitersNamesList.contains(getAID().getLocalName())) {
            waitersNamesList.add(getAID().getLocalName());
            SplashScreen.getInstance().communicationGUI.writeWaiterAgentNameOnTheGUI(waitersNamesList);
        }

    }

    // Waiter leaving the restaurant because of some reason
    @Override
    protected void takeDown() {
        // it will remove the waiter from the list
        int leavingWaiterIndex = waitersNamesList.indexOf(getAID().getLocalName());
        if (leavingWaiterIndex != -1) {
            waitersNamesList.remove(leavingWaiterIndex);
            SplashScreen.getInstance().communicationGUI.writeWaiterAgentNameOnTheGUI(waitersNamesList);
        }
        removingWaiter();
        showMessageInGUIAndCLI(DefaultMessages.W_msg_leaving);
    }


    // ======================================= CUSTOM METHODS ===========================================


    // checking if someone is serving kithcen or not
    private boolean isKitchenServiceAvailable() {
        DFAgentDescription dfAgentDescription = new DFAgentDescription();
        ServiceDescription serviceDesc = new ServiceDescription();

        serviceDesc.setType(AppConstant.KitchenServiceName);
        dfAgentDescription.addServices(serviceDesc);

        try {
            DFAgentDescription[] kitchenAgents = DFService.search(this, dfAgentDescription);

            // atleast one agent
            if (kitchenAgents.length > 0)
                kitchenInfo = kitchenAgents[0].getName();
            else { // otherwise kitchen will be closed
                showMessageInGUIAndCLI(DefaultMessages.W_msg_Kitchen_still_Closed);
                return false;
            }
        } catch (FIPAException e) { /// FIPA Exception
            e.printStackTrace();
        }
        return true;
    }


    // update the known dishes or previouslly available dishes
    //like it's quanity and if dish name is reliable than update the name as well
    public void updateKnownDish(Dish newDish) {
        int dishIndex = knownDishes.indexOf(newDish);
        Dish knownDish = knownDishes.get(dishIndex);

        // if new dish reliable then add new or update the nonreliable existing dish name
        if (isDishInfoReliable(newDish) || !isDishInfoReliable(knownDish))
            knownDishes.set(dishIndex, newDish);
        else {
            if (newDish.getQuantity() < knownDish.getQuantity())
                knownDish.setQuantity(newDish.getQuantity());
        }
    }

    public boolean isDishInfoReliable(Dish dish) {
        return dish.getAidInformationSource().equals(kitchenInfo);
    }

    public Dish suggestOtherDish(Dish originalDish, int customerMood) {

        for (Dish knownDish : knownDishes)
            if (customerMood - knownDish.getNormalTasteCookTiming() - 5 >= 3
                    && customerMood + knownDish.getDeliciousTasteCookTiming() - 5 >= 3
                    && !knownDish.getName().equals(originalDish.getName()))
                return knownDish;

        return null;
    }

    // check if dish is available in the kitchen or not
    public void checkIfDishIsAvailableInTheKitchen(AID otherWaiter, String dishName) {
        Random random = new Random();
        int lying = random.nextInt(99) + 1;
        int dishIndex = getKnownDishIndex(dishName);

        //75% that waiter is lying that dish is not available
        if ((dishIndex == -1 && isHonestOrTrustFul) || (!isHonestOrTrustFul && lying > 75)) {
            showMessageInGUIAndCLI(DefaultMessages.W_msg_5050_try_some_other_dish);
            sendMessageUsingACLFIPA(otherWaiter, ACLMessage.FAILURE, FIPANames.InteractionProtocol.FIPA_REQUEST,
                    AppConstant.dishDetailsConversationGroupID, dishName);
        } else { // dish is genuinely not available
            Dish desireDishByCustomer = null;

            if (dishIndex != -1)
                desireDishByCustomer = knownDishes.get(dishIndex);

            String desiredDishDetails = dishName + " - ";
            String messageToCustomer = "";

            if (!isHonestOrTrustFul) {
                // if dish not exist or quantoty is zero
                if (desireDishByCustomer == null || desireDishByCustomer.getQuantity() == 0)
                    desiredDishDetails += random.nextInt(5) + 1;
                else // if exist then get the current quantity and do minus 1 as well
                    desiredDishDetails += random.nextInt(desireDishByCustomer.getQuantity());

                desiredDishDetails += " - " + (random.nextInt(6) + 5) + " - "
                        + random.nextInt(random.nextInt(5) + 1);
                messageToCustomer = DefaultMessages.W_msg_FALSE_LIE;
            } else
                desiredDishDetails += desireDishByCustomer.getQuantity() + " - " + desireDishByCustomer.getNormalTasteCookTiming() + " - "
                        + desireDishByCustomer.getDeliciousTasteCookTiming();

            messageToCustomer += String.format(DefaultMessages.W_msg_FALSE_dish_ready, desiredDishDetails);

            showMessageInGUIAndCLI(messageToCustomer);
            sendMessageUsingACLFIPA(otherWaiter, ACLMessage.INFORM, FIPANames.InteractionProtocol.FIPA_REQUEST,
                    AppConstant.dishDetailsConversationGroupID, desiredDishDetails);
        }

    }

    // remove the waiter from the restaurant
    private void removingWaiter() {
        try {
            DFService.deregister(this);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    public void resetWaiterIndex() {
        for (int i = 0; i < waitersList.size(); i++)
            if (waitersList.get(i).getValue()) {
                waiterIndex = i;
                return;
            }
    }

    public AID getAnotherReliableWaiterForCustomer() {
        WaiterArg<AID, Boolean> waiter;

        if (waitersList.size() == 0 || waiterIndex >= waitersList.size())
            return null;

        do {
            waiter = waitersList.get(waiterIndex);
            waiterIndex++;
        }
        while (!waiter.getValue() && waiterIndex < waitersList.size());

        if (!waiter.getValue())
            return null;
        else
            return waiter.getKey();
    }


    public WaiterArg<AID, Boolean> getWaiter(AID waiter) {
        for (WaiterArg<AID, Boolean> knownWaiter : waitersList)
            if (knownWaiter.getKey().equals(waiter))
                return knownWaiter;

        return null;
    }

    public Dish getKnownDish(String dishName) {
        int index = getKnownDishIndex(dishName);

        if (index != -1)
            return knownDishes.get(index);
        else
            return null;
    }

    public int getKnownDishIndex(String dishName) {
        for (int i = 0; i < knownDishes.size(); i++)
            if (knownDishes.get(i).getName().equals(dishName))
                return i;

        return -1;
    }

    public ArrayList<Dish> getKnownDishes() {
        return knownDishes;
    }

    public void addTip(double tip) {
        tipAmount += tip;
    }

    public double getTipAmount() {
        return tipAmount;
    }

    public AID getCustomerID() {
        return customerID;
    }

    public void setCustomerID(AID cid) {
        customerID = cid;
    }

    public boolean getHonestOrTrustFul() {
        return isHonestOrTrustFul;
    }


    public boolean isBusy() {
        return customerID != null;
    }

    public AID getKitchenInfo() {
        return kitchenInfo;
    }

    public ArrayList<WaiterArg<AID, Boolean>> getWaitersList() {
        return waitersList;
    }
}