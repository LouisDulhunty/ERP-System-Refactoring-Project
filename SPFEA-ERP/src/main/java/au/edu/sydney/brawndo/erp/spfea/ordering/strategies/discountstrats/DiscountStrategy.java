package au.edu.sydney.brawndo.erp.spfea.ordering.strategies.discountstrats;

import au.edu.sydney.brawndo.erp.ordering.Order;
import au.edu.sydney.brawndo.erp.ordering.Product;

import java.util.Map;

/**
 * Interface representing a discount strategy for orders.
 */
public interface DiscountStrategy {

    /**
     * Calculates the cost of the order with the implemented discount strategy.
     *
     * @param order    the order to calculate the cost for
     * @param products the map of products and their quantities in the order
     * @return the cost of the order with the implemented discount strategy applied
     */
     double calculateCost(Order order, Map<Product, Integer> products);
}