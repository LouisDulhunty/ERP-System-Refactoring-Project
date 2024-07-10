package au.edu.sydney.brawndo.erp.spfea.contacthandlerchain;

import au.edu.sydney.brawndo.erp.auth.AuthToken;
import au.edu.sydney.brawndo.erp.contact.Email;
import au.edu.sydney.brawndo.erp.ordering.Customer;
import au.edu.sydney.brawndo.erp.spfea.ContactMethod;

/**
 * EmailHandler is a concrete implementation of the ContactMethodHandler interface for handling email contact method
 * in a chain of responsibility.
 */
public class EmailHandler implements ContactMethodHandler {

    private ContactMethodHandler nextHandler;

    @Override
    public boolean handleInvoice(AuthToken token, Customer customer, ContactMethod contactMethod, String data) {
        if (contactMethod == ContactMethod.EMAIL) {
            String email = customer.getEmailAddress();
            if (email != null) {
                Email.sendInvoice(token, customer.getfName(), customer.getlName(), data, email);
                return true;
            }
        }
        if (nextHandler != null) {
            return nextHandler.handleInvoice(token, customer, contactMethod, data);
        }
        return false;
    }

    @Override
    public void setNextHandler(ContactMethodHandler nextHandler) {
        this.nextHandler = nextHandler;
    }
}