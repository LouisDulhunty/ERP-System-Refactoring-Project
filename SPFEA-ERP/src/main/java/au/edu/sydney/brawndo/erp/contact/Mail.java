package au.edu.sydney.brawndo.erp.contact;

import au.edu.sydney.brawndo.erp.auth.AuthModule;
import au.edu.sydney.brawndo.erp.auth.AuthToken;

public class Mail {
    public static void sendInvoice(AuthToken token, String customerFName, String customerLName, String data, String address, String suburb, String state, String postcode) {
        if (!AuthModule.authenticate(token)) {
            throw new SecurityException("Invalid authorisation");
        }
        System.out.println("Now posting to " + customerFName + " " + customerLName + " at " + address + ", " + suburb + " " + state + " " + postcode + "!");
        System.out.println(data);
    }
}
