package au.edu.sydney.brawndo.erp.ordering;

public interface SubscriptionOrder extends Order {
    double getRecurringCost();
    int numberOfShipmentsOrdered();
}
