package au.edu.sydney.brawndo.erp.spfea.contacthandlerchain;

import au.edu.sydney.brawndo.erp.auth.AuthToken;
import au.edu.sydney.brawndo.erp.contact.SMS;
import au.edu.sydney.brawndo.erp.ordering.Customer;
import au.edu.sydney.brawndo.erp.spfea.ContactMethod;

/**
 * SMSHandler is a concrete implementation of the ContactMethodHandler interface for handling
 * SMS contact method in a chain of responsibility.
 */
public class SMSHandler implements ContactMethodHandler {

    private ContactMethodHandler nextHandler;

    @Override
    public boolean handleInvoice(AuthToken token, Customer customer, ContactMethod contactMethod, String data) {
        if (contactMethod == ContactMethod.SMS) {
            String smsPhone = customer.getPhoneNumber();
            if (smsPhone != null) {
                SMS.sendInvoice(token, customer.getfName(), customer.getlName(), data, smsPhone);
                return true;
            }
        }

        return nextHandler.handleInvoice(token, customer, contactMethod, data);
    }

    @Override
    public void setNextHandler(ContactMethodHandler nextHandler) {
        this.nextHandler = nextHandler;
    }
}