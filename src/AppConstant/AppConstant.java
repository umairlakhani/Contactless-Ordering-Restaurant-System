package AppConstant;

public class AppConstant {
    // Restaurant Name
    public static String RestaurantName = "ContactLess Ordering System";
    public static int  SplashScreenStayTime = 2500;

    // Services Names
    public static String KitchenServiceName = "kitchen-service";
    public static String CustomerServiceName = "customer-service";

    // Comunication Group Ids between agent
    public static String OrderRequestConversationGroupID = "order-request";
    public static String MealDeliveryConversationGroupID = "meal-delivering";
    public static String WaiterRequestConversationGroupID = "waiter-request";
    public static String dishFeedbackConversationGroupID = "dish-feedback";
    public static String dishDetailsConversationGroupID = "dish-details";
    public static String startDishConversationGroupID = "start-dish";

    public static String Langauage = "English"; // language for FIPA Communication

    // Agents Base Names for our contactless ordering system
    public static String KitchenAgentClassName = "Kitchen";
    public static String CustomerAgentClassName = "Customer";
    public static String WaiterAgentClassName = "Waiter";

    // Dishes names in our Resaturant
    public static String[] dishesList = new String[]{
            "Pakistani Chicken Karahi",
            "Pakistani Daal",
            "Biryani",
            "Kabab",
            "Haleem",
            "Pizza",
            "Meat Balls",
            "Chicken Roasted",
            "Fried Fish",
            "Burger",
            "French Fries",
            "Mutton",
            "Pasta",
            "Fries"
    };
}
