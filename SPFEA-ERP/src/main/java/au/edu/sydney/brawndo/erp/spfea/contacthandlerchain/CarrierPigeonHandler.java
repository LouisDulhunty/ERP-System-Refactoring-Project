package au.edu.sydney.brawndo.erp.spfea.contacthandlerchain;

import au.edu.sydney.brawndo.erp.auth.AuthToken;
import au.edu.sydney.brawndo.erp.contact.CarrierPigeon;
import au.edu.sydney.brawndo.erp.ordering.Customer;
import au.edu.sydney.brawndo.erp.spfea.ContactMethod;

/**
 * CarrierPigeonHandler is a concrete implementation of the ContactMethodHandler interface for handling carrier pigeon
 * contact method in a chain of responsibility.
 */
public class CarrierPigeonHandler implements ContactMethodHandler {

    private ContactMethodHandler nextHandler;

    @Override
    public boolean handleInvoice(AuthToken token, Customer customer, ContactMethod contactMethod, String data) {
        if (contactMethod == ContactMethod.CARRIER_PIGEON) {
            String pigeonCoopID = customer.getPigeonCoopID();
            if (pigeonCoopID != null) {
                CarrierPigeon.sendInvoice(token, customer.getfName(), customer.getlName(), data, pigeonCoopID);
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