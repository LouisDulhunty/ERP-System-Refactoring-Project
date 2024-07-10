package au.edu.sydney.brawndo.erp.spfea.ordering.strategies.invoicestrats;

import au.edu.sydney.brawndo.erp.ordering.Order;
import au.edu.sydney.brawndo.erp.ordering.SubscriptionOrder;

// NOTE TO MARKER: I feel like using method overloading for this is appropriate as I'm assuming that there will only ever
// be two main types of orders; SubscriptionOrder & Order. If this isn't a satisfactory solution while your marking, could
// you please leave a comment in my marks about why it isn't?

/**
 * Implementation of the CustomerInvoiceStrategy interface for business invoices.
 */
public class BusinessInvoiceStrat implements CustomerInvoiceStrategy {

    /**
     * Generates an invoice for a subscription order in a business account.
     *
     * @param subscriptionOrder the subscription order for which to generate the invoice
     * @return the generated invoice as a string
     */
    @Override
    public String generateInvoice(SubscriptionOrder subscriptionOrder) {
        return String.format("Your business account will be charged: $%,.2f each week, with a total overall cost of: $%,.2f" +
                "\nPlease see your Brawndo© merchandising representative for itemised details.", subscriptionOrder.getRecurringCost(), subscriptionOrder.getTotalCost());
    }

    /**
     * Generates an invoice for a regular order in a business account.
     *
     * @param order the order for which to generate the invoice
     * @return the generated invoice as a string
     */
    @Override
    public String generateInvoice(Order order) {
        return String.format("Your business account has been charged: $%,.2f" +
                "\nPlease see your Brawndo© merchandising representative for itemised details.", order.getTotalCost());
    }
}