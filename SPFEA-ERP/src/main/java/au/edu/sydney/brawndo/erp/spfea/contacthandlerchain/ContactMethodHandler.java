package au.edu.sydney.brawndo.erp.spfea.contacthandlerchain;

import au.edu.sydney.brawndo.erp.auth.AuthToken;
import au.edu.sydney.brawndo.erp.ordering.Customer;
import au.edu.sydney.brawndo.erp.spfea.ContactMethod;

/**
 * ContactMethodHandler is an interface for handling contact methods in a chain of responsibility.
 */
public interface ContactMethodHandler {

    /**
     * Handles the invoice using the provided authentication token, customer, contact method, and data.
     *
     * @param token         the authentication token
     * @param customer      the customer
     * @param contactMethod the contact method
     * @param data          the data
     * @return true if the invoice is handled successfully, false otherwise
     */
    boolean handleInvoice(AuthToken token, Customer customer, ContactMethod contactMethod, String data);

    /**
     * Sets the next handler in the chain of responsibility.
     *
     * @param nextHandler the next handler
     */
    void setNextHandler(ContactMethodHandler nextHandler);
}