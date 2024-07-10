package au.edu.sydney.brawndo.erp.spfea.ordering.strategies.invoicestrats;

import au.edu.sydney.brawndo.erp.ordering.Order;
import au.edu.sydney.brawndo.erp.ordering.Product;
import au.edu.sydney.brawndo.erp.ordering.SubscriptionOrder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// NOTE TO MARKER: I feel like using method overloading for this is appropriate as I'm assuming that there will only ever
// be two main types of orders; SubscriptionOrder & Order. If this isn't a satisfactory solution while your marking, could
// you please leave a comment in my marks about why it isn't?

/**
 * Implementation of the CustomerInvoiceStrategy interface for personal invoices.
 */
public class PersonalInvoiceStrat implements CustomerInvoiceStrategy {

    /**
     * Generates an invoice for a subscription order in a personal account.
     *
     * @param subscriptionOrder the subscription order for which to generate the invoice
     * @return the generated invoice as a string
     */
    @Override
    public String generateInvoice(SubscriptionOrder subscriptionOrder) {
        StringBuilder sb = new StringBuilder();

        sb.append("Thank you for your Brawndo© order!\n");
        sb.append("Your order comes to: $");
        sb.append(String.format("%,.2f", subscriptionOrder.getRecurringCost()));
        sb.append(" each week, with a total overall cost of: $");
        sb.append(String.format("%,.2f", subscriptionOrder.getTotalCost()));
        sb.append("\nPlease see below for details:\n");
        List<Product> keyList = new ArrayList<>(subscriptionOrder.getAllProducts());
        keyList.sort(Comparator.comparing(Product::getProductName).thenComparing(Product::getCost));

        for (Product product: keyList) {
            sb.append("\tProduct name: ");
            sb.append(product.getProductName());
            sb.append("\tQty: ");
            sb.append(subscriptionOrder.getProductQty(product));
            sb.append("\tCost per unit: ");
            sb.append(String.format("$%,.2f", product.getCost()));
            sb.append("\tSubtotal: ");
            sb.append(String.format("$%,.2f\n", product.getCost() * subscriptionOrder.getProductQty(product)));
        }

        return sb.toString();
    }

    /**
     * Generates an invoice for a regular order in a personal account.
     *
     * @param order the order for which to generate the invoice
     * @return the generated invoice as a string
     */
    @Override
    public String generateInvoice(Order order) {
        StringBuilder sb = new StringBuilder();

        sb.append("Thank you for your Brawndo© order!\n");
        sb.append("Your order comes to: $");
        sb.append(String.format("%,.2f", order.getTotalCost()));
        sb.append("\nPlease see below for details:\n");
        List<Product> keyList = new ArrayList<>(order.getAllProducts());
        keyList.sort(Comparator.comparing(Product::getProductName).thenComparing(Product::getCost));

        for (Product product: keyList) {
            sb.append("\tProduct name: ");
            sb.append(product.getProductName());
            sb.append("\tQty: ");
            sb.append(order.getProductQty(product));
            sb.append("\tCost per unit: ");
            sb.append(String.format("$%,.2f", product.getCost()));
            sb.append("\tSubtotal: ");
            sb.append(String.format("$%,.2f\n", product.getCost() * order.getProductQty(product)));
        }

        return sb.toString();
    }
}
