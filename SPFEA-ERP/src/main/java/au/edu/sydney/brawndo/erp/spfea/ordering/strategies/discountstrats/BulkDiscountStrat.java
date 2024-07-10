package au.edu.sydney.brawndo.erp.spfea.ordering.strategies.discountstrats;

import au.edu.sydney.brawndo.erp.ordering.Product;
import au.edu.sydney.brawndo.erp.ordering.Order;

import java.util.Map;

/**
 * Implementation of the DiscountStrat interface that applies bulk discount to an order.
 */
public class BulkDiscountStrat implements DiscountStrategy {

    private int discountThreshold;
    private double discountRate;

    /**
     * Constructs a BulkDiscountStrat object.
     *
     * @param discountRate      the discount rate to be applied
     * @param discountThreshold the threshold quantity for the discount to be applied
     */
    public BulkDiscountStrat(double discountRate, int discountThreshold) {
        this.discountRate = discountRate;
        this.discountThreshold = discountThreshold;
    }

    /**
     * Calculates the cost of the order with bulk discount applied.
     *
     * @param order    the order to calculate the cost for
     * @param products the map of products and their quantities in the order
     * @return the cost of the order with bulk discount applied
     */
    public double calculateCost(Order order, Map<Product, Integer> products) {
        double cost = 0.0;

        for (Product product : products.keySet()) {
            int count = products.get(product);
            if (count >= discountThreshold) {
                cost += count * product.getCost() * discountRate;
            } else {
                cost += count * product.getCost();
            }
        }
        return cost;
    }
}