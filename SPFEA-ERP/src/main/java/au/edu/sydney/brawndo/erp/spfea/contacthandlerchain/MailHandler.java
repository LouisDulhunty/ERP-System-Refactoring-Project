package au.edu.sydney.brawndo.erp.spfea.contacthandlerchain;

import au.edu.sydney.brawndo.erp.auth.AuthToken;
import au.edu.sydney.brawndo.erp.contact.Mail;
import au.edu.sydney.brawndo.erp.ordering.Customer;
import au.edu.sydney.brawndo.erp.spfea.ContactMethod;

/**
 * MailHandler is a concrete implementation of the ContactMethodHandler interface for handling mail contact method
 * in a chain of responsibility.
 */
public class MailHandler implements ContactMethodHandler {

    private ContactMethodHandler nextHandler;

    @Override
    public boolean handleInvoice(AuthToken token, Customer customer, ContactMethod contactMethod, String data) {
        if (contactMethod == ContactMethod.MAIL) {
            String address = customer.getAddress();
            String suburb = customer.getSuburb();
            String state = customer.getState();
            String postcode = customer.getPostCode();
            if (address != null && suburb != null &&
                    state != null && postcode != null) {
                Mail.sendInvoice(token, customer.getfName(), customer.getlName(), data, address, suburb, state, postcode);
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