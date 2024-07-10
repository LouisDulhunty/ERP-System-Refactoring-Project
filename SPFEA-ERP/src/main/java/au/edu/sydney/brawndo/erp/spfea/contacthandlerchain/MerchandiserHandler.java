package au.edu.sydney.brawndo.erp.spfea.contacthandlerchain;

import au.edu.sydney.brawndo.erp.auth.AuthToken;
import au.edu.sydney.brawndo.erp.contact.Merchandiser;
import au.edu.sydney.brawndo.erp.ordering.Customer;
import au.edu.sydney.brawndo.erp.spfea.ContactMethod;

/**
 * MerchandiserHandler is a concrete implementation of the ContactMethodHandler interface for handling
 * merchandiser contact method in a chain of responsibility.
 */
public class MerchandiserHandler implements ContactMethodHandler {

    private ContactMethodHandler nextHandler;

    @Override
    public boolean handleInvoice(AuthToken token, Customer customer, ContactMethod contactMethod, String data) {
        if (contactMethod == ContactMethod.MERCHANDISER) {
            String merchandiser = customer.getMerchandiser();
            String businessName = customer.getBusinessName();
            if (merchandiser != null && businessName != null) {
                Merchandiser.sendInvoice(token, customer.getfName(), customer.getlName(), data, merchandiser, businessName);
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