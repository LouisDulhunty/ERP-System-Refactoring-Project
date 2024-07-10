package au.edu.sydney.brawndo.erp.contact;

import au.edu.sydney.brawndo.erp.auth.AuthModule;
import au.edu.sydney.brawndo.erp.auth.AuthToken;

public class PhoneCall {
    public static void sendInvoice(AuthToken token, String customerFName, String customerLName, String data, String phone) {
        if (!AuthModule.authenticate(token)) {
            throw new SecurityException("Invalid authorisation");
        }
        System.out.println("Now robodialling " + customerFName + " " + customerLName + " at " + phone + "!");
        System.out.println(data);
    }
}

