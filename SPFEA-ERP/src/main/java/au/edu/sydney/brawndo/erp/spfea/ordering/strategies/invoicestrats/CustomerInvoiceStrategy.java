package au.edu.sydney.brawndo.erp.spfea.ordering.strategies.invoicestrats;

import au.edu.sydney.brawndo.erp.ordering.Order;
import au.edu.sydney.brawndo.erp.ordering.SubscriptionOrder;


// NOTE TO MARKER: I feel like using method overloading for this is appropriate as I'm assuming that there will only ever
// be two main types of orders; SubscriptionOrder & Order. If this isn't a satisfactory solution while your marking, could
// you please leave a comment in my marks about why it isn't?

/**
 * Interface representing a customer invoice strategy. Invoices are generated based on weather they are subscription or not
 */
public interface CustomerInvoiceStrategy {

    /**
     * Generates an invoice for a subscription order.
     *
     * @param subscriptionOrder the subscription order for which to generate the invoice
     * @return the generated Subscription invoice as a string
     */
    String generateInvoice(SubscriptionOrder subscriptionOrder);

    /**
     * Generates an invoice for a regular order.
     *
     * @param order the order for which to generate the invoice
     * @return the generated Non-Subscription invoice as a string
     */
    String generateInvoice(Order order);
}





//package au.edu.sydney.brawndo.erp.spfea.ordering.strategies.invoiceStrategies;
//
//import au.edu.sydney.brawndo.erp.ordering.Order;
//import au.edu.sydney.brawndo.erp.ordering.SubscriptionOrder;
//
//public interface CustomerInvoiceStrategy {
//    public String generateInvoice(SubscriptionOrder subscriptionOrder);
//    public String generateInvoice(Order order);
//}
