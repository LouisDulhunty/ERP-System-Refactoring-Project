package au.edu.sydney.brawndo.erp.spfea;

import au.edu.sydney.brawndo.erp.auth.AuthToken;
import au.edu.sydney.brawndo.erp.ordering.Customer;
import au.edu.sydney.brawndo.erp.spfea.contacthandlerchain.*;

import java.util.Arrays;
import java.util.List;

/**
 * The ContactHandler class handles sending invoices to customers using various contact methods.
 * Chain of Responsibility pattern is used to handle this
 */
public class ContactHandler {

    /**
     * Sends an invoice to a customer using the specified contact methods in priority order.
     * The first available contact in priority list will be where the invoice is sent, determined by the CoR
     *
     * @param token    the authentication token
     * @param customer the customer to whom the invoice should be sent
     * @param priority the priority order of contact methods to use
     * @param data     the invoice data to send
     * @return true if the invoice was successfully sent, false otherwise
     */
    public static boolean sendInvoice(AuthToken token, Customer customer, List<ContactMethod> priority, String data) {
        ContactMethodHandler firstHandler = createHandlerChain();
        for (ContactMethod method : priority) {
            if (firstHandler.handleInvoice(token, customer, method, data)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates the chain of responsibility for handling contact methods.
     *
     * @return the first handler in the chain
     */
    private static ContactMethodHandler createHandlerChain() {
        SMSHandler smsHandler = new SMSHandler();
        MailHandler mailHandler = new MailHandler();
        EmailHandler emailHandler = new EmailHandler();
        PhoneCallHandler phoneCallHandler = new PhoneCallHandler();
        MerchandiserHandler merchandiserHandler = new MerchandiserHandler();
        CarrierPigeonHandler carrierPigeonHandler = new CarrierPigeonHandler();

        smsHandler.setNextHandler(mailHandler);
        mailHandler.setNextHandler(emailHandler);
        emailHandler.setNextHandler(phoneCallHandler);
        phoneCallHandler.setNextHandler(merchandiserHandler);
        merchandiserHandler.setNextHandler(carrierPigeonHandler);
        carrierPigeonHandler.setNextHandler(null);

        return smsHandler;
    }

    /**
     * Retrieves the list of known contact methods.
     *
     * @return the list of known contact methods
     */
    public static List<String> getKnownMethods() {
        return Arrays.asList(
                "Carrier Pigeon",
                "Email",
                "Mail",
                "Merchandiser",
                "Phone call",
                "SMS"
        );
    }
}