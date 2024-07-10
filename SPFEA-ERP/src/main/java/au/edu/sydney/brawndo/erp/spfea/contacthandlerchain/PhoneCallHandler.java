package au.edu.sydney.brawndo.erp.spfea.contacthandlerchain;

import au.edu.sydney.brawndo.erp.auth.AuthToken;
import au.edu.sydney.brawndo.erp.contact.PhoneCall;
import au.edu.sydney.brawndo.erp.ordering.Customer;
import au.edu.sydney.brawndo.erp.spfea.ContactMethod;

/**
 * PhoneCallHandler is a concrete implementation of the ContactMethodHandler interface for handling
 * phone call contact method in a chain of responsibility.
 */
public class PhoneCallHandler implements ContactMethodHandler {

    private ContactMethodHandler nextHandler;

    @Override
    public boolean handleInvoice(AuthToken token, Customer customer, ContactMethod contactMethod, String data) {
        if (contactMethod == ContactMethod.PHONECALL) {
            String phone = customer.getPhoneNumber();
            if (phone != null) {
                PhoneCall.sendInvoice(token, customer.getfName(), customer.getlName(), data, phone);
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