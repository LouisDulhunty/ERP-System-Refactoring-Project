package au.edu.sydney.brawndo.erp.contact;

import au.edu.sydney.brawndo.erp.auth.AuthModule;
import au.edu.sydney.brawndo.erp.auth.AuthToken;

public class CarrierPigeon {
    public static void sendInvoice(AuthToken token, String customerFName, String customerLName, String data, String pigeonCoopID) {
        if (!AuthModule.authenticate(token)) {
            throw new SecurityException("Invalid authorisation");
        }
        System.out.println("Now sending carrier pigeon to " + customerFName + " " + customerLName + " from coop " + pigeonCoopID + "!");
        System.out.println(data);
    }
}
