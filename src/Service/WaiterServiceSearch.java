package Service;

import AppConstant.AppConstant;
import RestaurantAgents.BaseAgent;
import jade.core.AID;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

public class WaiterServiceSearch extends TickerBehaviour {
    
    private BaseAgent baseAgent;

    public WaiterServiceSearch(BaseAgent baseAgent, long period) {
        super(baseAgent, period);
        this.baseAgent = baseAgent;
    }

    @Override
    protected void onTick() {
        DFAgentDescription dfAgentDescription = new DFAgentDescription();
        ServiceDescription serviceDescription = new ServiceDescription();

        serviceDescription.setType(AppConstant.CustomerServiceName);
        dfAgentDescription.addServices(serviceDescription);
        try {
            // getting waiters which is mentioned in the argument
            DFAgentDescription[] dfAgentDescList = DFService.search(myAgent, dfAgentDescription);
            AID[] waitersList = new AID[dfAgentDescList.length];
            for(int i=0; i<dfAgentDescList.length; i++) {
                waitersList[i] = dfAgentDescList[i].getName();
            }

            // atleast one waiter should be need for the restaurant
            if(dfAgentDescList.length > 0)
                baseAgent.waitersAddInTheRestaurant(waitersList);

        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }
}
