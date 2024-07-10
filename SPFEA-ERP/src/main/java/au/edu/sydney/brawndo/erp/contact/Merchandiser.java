package au.edu.sydney.brawndo.erp.contact;

import au.edu.sydney.brawndo.erp.auth.AuthModule;
import au.edu.sydney.brawndo.erp.auth.AuthToken;

public class Merchandiser {
    public static void sendInvoice(AuthToken token, String customerFName, String customerLName, String data, String merchName, String businessName) {
        if (!AuthModule.authenticate(token)) {
            throw new SecurityException("Invalid authorisation");
        }
        System.out.println(merchName + " will pass on this invoice to " + customerFName + " " + customerLName + ", from " + businessName);
        System.out.println(data);
    }
}
