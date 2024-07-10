package au.edu.sydney.brawndo.erp.contact;

import au.edu.sydney.brawndo.erp.auth.AuthModule;
import au.edu.sydney.brawndo.erp.auth.AuthToken;

public class Email {
    public static void sendInvoice(AuthToken token, String customerFName, String customerLName, String data, String email) {
        if (!AuthModule.authenticate(token)) {
            throw new SecurityException("Invalid authorisation");
        }
        System.out.println("Now emailing " + customerFName + " " + customerLName + " at " + email + "!");
        System.out.println(data);
    }
}
