package model;

import java.io.Serializable;

public class WaiterArg<T, U> implements Serializable {

    private T AIDInfo;
    private U isHonestOrTrustFull;
    
    public WaiterArg(T AIDInfo, U isHonestOrTrustFull) {
        this.AIDInfo = AIDInfo;
        this.isHonestOrTrustFull = isHonestOrTrustFull;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof WaiterArg) {
            WaiterArg p = (WaiterArg) o;

            return p.getKey().equals(this.AIDInfo) && p.getValue().equals(this.isHonestOrTrustFull);
        }
        else
            return false;
    }

    public T getKey() {
        return AIDInfo;
    }

    public U getValue() {
        return isHonestOrTrustFull;
    }

    public void setKey(T newKey) {
        AIDInfo = newKey;
    }

    public void setValue(U newValue) {
        isHonestOrTrustFull = newValue;
    }
}