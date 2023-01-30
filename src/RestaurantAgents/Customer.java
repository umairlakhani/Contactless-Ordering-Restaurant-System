package RestaurantAgents;

import java.util.*;

import AppConstant.AppConstant;
import AppConstant.DefaultMessages;
import Service.CustomerService.OrderInProcess;
import Service.WaiterServiceSearch;
import Utility.ACLMessageFIPAUtil;
import gui.SplashScreen;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.proto.SimpleAchieveREInitiator;

public class Customer extends BaseAgent {

    private String desiredDish;
    private boolean hasDishArgument;
    private HashSet<AID> unavailableWaiters = new HashSet<>();
    private boolean hasWaiter;
    private AID waiter;
    private ArrayList<AID> waiters = new ArrayList<>();

    // mood of the customer
    private int mood;
    private int initialMood;

    private int attempts;

    private WaiterServiceSearch waiterServiceSearch;

    @Override
    protected void setup() {
        agentRole = AppConstant.CustomerAgentClassName;

        showMessageInGUIAndCLI(String.format(DefaultMessages.C_msg_customer_entry, AppConstant.CustomerAgentClassName, getAID().getLocalName()));
        // it will add the customer into the list of GUI
        if (!customerNamesList.contains(getAID().getLocalName())) {
            customerNamesList.add(getAID().getLocalName());
            SplashScreen.getInstance().communicationGUI.writeCustomerAgentNameOnTheGUI(customerNamesList);
        }
        Random random = new Random();
        mood = random.nextInt(9) + 1; //10 being very relaxed and 1 being very frustrated
        initialMood = mood;

        hasWaiter = false;
        attempts = 0;
        desiredDish = "";
        hasDishArgument = false;

        Object[] args = getArguments();

        if (args.length > 0) {
            desiredDish = (String) args[0];
            hasDishArgument = true;
        }

        waiterServiceSearch = new WaiterServiceSearch(this, 1000);
        addBehaviour(waiterServiceSearch);
    }

    @Override
    public void waitersAddInTheRestaurant(AID[] addNewWaitors) {
        this.setWaiters(new ArrayList<>(Arrays.asList(addNewWaitors)));

        if (!this.hasWaiter()) {
            this.getAvailableWaiter();
        }
    }

    @Override
    protected void takeDown() {
        showMessageInGUIAndCLI(DefaultMessages.C_msg_leaving);
        // it will remove the waiter from the list
        int leavingWaiterIndex = customerNamesList.indexOf(getAID().getLocalName());
        if (leavingWaiterIndex != -1) {
            customerNamesList.remove(leavingWaiterIndex);
            SplashScreen.getInstance().communicationGUI.writeCustomerAgentNameOnTheGUI(customerNamesList);
        }
    }

    // ======================================= CUSTOM METHODS ===========================================

    // Step 0: get the available waiter in the kitchen
    private void getAvailableWaiter() {
        AID currentWaiter = getCurrentWaiter();

        if (currentWaiter == null) {
            showMessageInGUIAndCLI(DefaultMessages.C_msg_happy_waiter_not_available);
            doDelete();
            return;
        }

        // get an available waiter
        ACLMessage msg = ACLMessageFIPAUtil.getACLMessageObjectForAvailableWaiters(currentWaiter);

        showMessageInGUIAndCLI(String.format(DefaultMessages.C_msg_happy_anyone_available, currentWaiter.getLocalName()));

        addBehaviour(new SimpleAchieveREInitiator(this, msg) {
            @Override
            protected void handleInform(ACLMessage inform) {
                hasWaiter = true;
                waiter = currentWaiter;
                waiterServiceSearch.stop();
                orderDish();
            }

            @Override
            protected void handleRefuse(ACLMessage msg) {
                unavailableWaiters.add(currentWaiter);
            }
        });
    }

    // Step 1: Order dish to the waiter
    public void orderDish() {
        if (!hasDishArgument) {
            decideDish();
        } else {
            hasDishArgument = false;
        }

        showMessageInGUIAndCLI(String.format(DefaultMessages.C_msg_eating_desire, desiredDish));

        ACLMessage msg = ACLMessageFIPAUtil.getACLMessageObjectForCustomerService(waiter, desiredDish + " - " + mood);
        addBehaviour(new OrderInProcess(this, msg));
    }

    private AID getCurrentWaiter() {
        Random random = new Random();
        int index = random.nextInt(waiters.size());

        boolean allWaitersUnavailable = true;

        for (AID aid : waiters) {
            if (!unavailableWaiters.contains(aid)) {
                allWaitersUnavailable = false;
            }
        }

        if (allWaitersUnavailable) {
            return null;
        }

        while (unavailableWaiters.contains(waiters.get(index))) {
            index = random.nextInt(waiters.size());
        }
        return waiters.get(index);
    }


    private void decideDish() {
        String oldDish = desiredDish;
        String[] dishes = Kitchen.getMenu();
        Random rand = new Random();

        desiredDish = dishes[rand.nextInt(dishes.length)];

        while (oldDish.equals(desiredDish)) {
            desiredDish = dishes[rand.nextInt(dishes.length)];
        }
    }




    //  ==================== getter setter ==================================
    public void incrementAttempts() {
        attempts++;
    }

    public String getDesiredDish() {
        return desiredDish;
    }

    public AID getWaiter() {
        return waiter;
    }

    private void setWaiters(ArrayList<AID> agents) {
        waiters = agents;
    }

    private boolean hasWaiter() {
        return hasWaiter;
    }

    public int getAttempts() {
        return attempts;
    }

    public int getMood() {
        return mood;
    }

    public int getInitialMood() {
        return initialMood;
    }

    public void decrementMood() {
        if (mood > 0) {
            mood--;
        }
    }

    public void incrementMood() {
        if (mood < 10) {
            mood++;
        }
    }
}
