package RestaurantAgents;

import AppConstant.AppConstant;
import AppConstant.DefaultMessages;
import Service.KitchenService.DishOrderRequest;
import gui.SplashScreen;
import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

import java.util.HashMap;
import java.util.Random;

public class Kitchen extends BaseAgent {

    // divided into 3 intgers value
    // first value contains Item Availability,
    // second value contains that how much time
    // <Dish, <Dish-Quantity, Normal-Cook-Timing, Delicious-Cook-Timing>>
    private HashMap<String, int[]> dishAllInformation;

    // Name of the dishes in the restaurant
    private static String[] dishesName;

    // it's the override method for the Builtin Agent Class
    @Override
    protected void setup() {
        agentRole = AppConstant.KitchenAgentClassName;

        DFAgentDescription dfAgentDesc = new DFAgentDescription();
        ServiceDescription serviceDesc = new ServiceDescription();

        dfAgentDesc.setName(this.getAID());
        serviceDesc.setType(AppConstant.KitchenServiceName);
        serviceDesc.setName(AppConstant.RestaurantName);
        dfAgentDesc.addServices(serviceDesc);

        try {// register the agent into the restaurant
            DFService.register(this, dfAgentDesc);
        } catch (FIPAException e) {
            e.printStackTrace();
        }

        dishAllInformation = new HashMap<>();
        dishesName = AppConstant.dishesList;         // Get dishes list from the appConstant

        Object[] args = getArguments(); // get arguments

        if (args.length > 0)
            setDishesInformationComesFromArgs(args);
        else
            this.generateDish();

        // it will add the customer into the list of GUI
        if (!chefNamesList.contains(getAID().getLocalName())) {
            chefNamesList.add(getAID().getLocalName());
            SplashScreen.getInstance().communicationGUI.writeChefAgentNameOnTheGUI(chefNamesList);
        }
        System.out.println(String.format(DefaultMessages.K_msg_opening_kitchen, this.getAID().getLocalName()));
        SplashScreen.getInstance().communicationGUI.writeMessagesOntheGUI(String.format(DefaultMessages.K_msg_opening_kitchen, this.getAID().getLocalName()));

        //Wait for Waiter requests
        this.addBehaviour(new DishOrderRequest(this));
    }

// use to remove the agent
    @Override
    protected void takeDown() {
        // it will remove the chef from the list
        int leavingWaiterIndex = chefNamesList.indexOf(getAID().getLocalName());
        if (leavingWaiterIndex != -1) {
            chefNamesList.remove(leavingWaiterIndex);
            SplashScreen.getInstance().communicationGUI.writeChefAgentNameOnTheGUI(chefNamesList);
        }
        // de register from the
        try {
            DFService.deregister(this);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        System.out.println(String.format(DefaultMessages.K_msg_shutting_down, this.getAID().getLocalName()));
        SplashScreen.getInstance().communicationGUI.writeMessagesOntheGUI(String.format(DefaultMessages.K_msg_shutting_down, this.getAID().getLocalName()));
    }

    // ======================================= CUSTOM METHODS ===========================================


    // set dishes names
    private void setDishesInformationComesFromArgs(Object[] newDishes) {
        for (Object newDish : newDishes) {
            String[] dishDetails = ((String) newDish).split("-");
            dishAllInformation.put(dishDetails[0], new int[]{Integer.parseInt(dishDetails[1]), Integer.parseInt(dishDetails[2]), Integer.parseInt(dishDetails[3])});
        }
    }

    private void generateDish() {
        Random rand = new Random();
        int normalCookTiming, deliciouslyCookTiming, quantity;

        for (int i = 0; i < dishesName.length; i++) {
            normalCookTiming = rand.nextInt(9) + 1;
            deliciouslyCookTiming = rand.nextInt(9) + 1;
            quantity = rand.nextInt(4) + 1;
            dishAllInformation.put(dishesName[i], new int[]{quantity, normalCookTiming, deliciouslyCookTiming});
        }
    }

    // getter setters
    public static String[] getMenu() {
        return dishesName;
    }

    public HashMap<String, int[]> getDishAllInformation() {
        return dishAllInformation;
    }

    public Boolean checkMeal(String dish) {
        return dishAllInformation.containsKey(dish);
    }

    public int[] getMealInfo(String dish) {
        return dishAllInformation.get(dish);
    }


    // Because waiter doesn't exist here, we need to implement because of abstract method in the baseagent class
    @Override
    public void waitersAddInTheRestaurant(AID[] addNewWaitors) {
        //Empty function
    }
}
