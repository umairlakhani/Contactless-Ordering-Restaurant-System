package model;

import jade.core.AID;

public class Dish {
    private String name;
    private int quantity;
    private int normalTasteCookTiming;
    private int deliciousTasteCookTiming;
    private AID aidInformationSource;

    public Dish(String name, int quantity, int normalTasteCookTiming, int deliciousTasteCookTiming, AID aidInformationSource) {
        this.name = name;
        this.quantity = quantity;
        this.normalTasteCookTiming = normalTasteCookTiming;
        this.deliciousTasteCookTiming = deliciousTasteCookTiming;
        this.aidInformationSource = aidInformationSource;
    }

    @Override
    public boolean equals(Object dish) {
        if(dish instanceof Dish) 
            return ((Dish) dish).getName().equals(name);
        else
            return false;
    }

    public boolean compareStaticDetails(String dishName, int ct, int prep) {
        return dishName.equals(name) && ct == normalTasteCookTiming && prep == deliciousTasteCookTiming;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setNormalTasteCookTiming(int normalTasteCookTiming) {
        this.normalTasteCookTiming = normalTasteCookTiming;
    }

    public void setDeliciousTasteCookTiming(int deliciousTasteCookTiming) {
        this.deliciousTasteCookTiming = deliciousTasteCookTiming;
    }

    public void setAidInformationSource(AID aidInformationSource) {
        this.aidInformationSource = aidInformationSource;
    }

    public void decrementAvailability() {
        quantity--;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getNormalTasteCookTiming() {
        return normalTasteCookTiming;
    }

    public int getDeliciousTasteCookTiming() {
        return deliciousTasteCookTiming;
    }

    public AID getAidInformationSource() {
        return aidInformationSource;
    }
}