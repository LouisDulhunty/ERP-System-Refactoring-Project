package au.edu.sydney.brawndo.erp.contact;

import au.edu.sydney.brawndo.erp.auth.AuthModule;
import au.edu.sydney.brawndo.erp.auth.AuthToken;

public class SMS {
    public static void sendInvoice(AuthToken token, String customerFName, String customerLName, String data, String phone) {
        if (!AuthModule.authenticate(token)) {
            throw new SecurityException("Invalid authorisation");
        }
        System.out.println("Now SMSing " + customerFName + " " + customerLName + " on " + phone + "!");
        System.out.println(data);
    }
}

