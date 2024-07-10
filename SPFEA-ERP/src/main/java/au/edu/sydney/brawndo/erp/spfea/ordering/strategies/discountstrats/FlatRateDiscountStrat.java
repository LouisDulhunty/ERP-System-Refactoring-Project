package au.edu.sydney.brawndo.erp.spfea.ordering.strategies.discountstrats;

import au.edu.sydney.brawndo.erp.ordering.Order;
import au.edu.sydney.brawndo.erp.ordering.Product;

import java.util.Map;

/**
 * Implementation of the DiscountStrat interface that applies a flat rate discount to an order.
 */
public class FlatRateDiscountStrat implements DiscountStrategy {

    private double discountRate;

    /**
     * Constructs a FlatRateDiscountStrat object.
     *
     * @param discountRate the discount rate to be applied
     */
    public FlatRateDiscountStrat(double discountRate) {
        this.discountRate = discountRate;
    }

    /**
     * Calculates the cost of the order with a flat rate discount applied.
     *
     * @param order    the order to calculate the cost for
     * @param products the map of products and their quantities in the order
     * @return the cost of the order with a flat rate discount applied
     */
    @Override
    public double calculateCost(Order order, Map<Product, Integer> products) {
        double cost = 0.0;

        for (Product product : products.keySet()) {
            cost += products.get(product) * product.getCost() * discountRate;
        }
        return cost;
    }
}