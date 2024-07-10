package au.edu.sydney.brawndo.erp.spfea;

import au.edu.sydney.brawndo.erp.auth.AuthToken;
import au.edu.sydney.brawndo.erp.contact.Merchandiser;
import au.edu.sydney.brawndo.erp.database.TestDatabase;
import au.edu.sydney.brawndo.erp.ordering.Customer;

public class CustomerImpl implements Customer {

    private final int id;
    private String fName;
    private String lName;
    private String phoneNumber;
    private String emailAddress;
    private String address;
    private String suburb;
    private String state;
    private String postCode;
    private String merchandiser;
    private String businessName;
    private String pigeonCoopID;


    public CustomerImpl(AuthToken token, int id) {

        this.id = id;
        this.fName = TestDatabase.getInstance().getCustomerField(token, id, "fName");
        this.lName = TestDatabase.getInstance().getCustomerField(token, id, "lName");
        this.phoneNumber = TestDatabase.getInstance().getCustomerField(token, id, "phoneNumber");
        this.emailAddress = TestDatabase.getInstance().getCustomerField(token, id, "emailAddress");
        this.address = TestDatabase.getInstance().getCustomerField(token, id, "address");
        this.suburb = TestDatabase.getInstance().getCustomerField(token, id, "suburb");
        this.state = TestDatabase.getInstance().getCustomerField(token, id, "state");
        this.postCode = TestDatabase.getInstance().getCustomerField(token, id, "postCode");
        this.merchandiser = TestDatabase.getInstance().getCustomerField(token, id, "merchandiser");
        this.businessName = TestDatabase.getInstance().getCustomerField(token, id, "businessName");
        this.pigeonCoopID = TestDatabase.getInstance().getCustomerField(token, id, "pigeonCoopID");

    }

    public int getId() {
        return id;
    }

    @Override
    public String getfName() {
        return fName;
    }

    @Override
    public String getlName() {
        return lName;
    }

    @Override
    public String getPhoneNumber() {
        return phoneNumber;
    }

    @Override
    public String getEmailAddress() {
        return emailAddress;
    }

    @Override
    public String getAddress() {
        return address;
    }

    @Override
    public String getSuburb() {
        return suburb;
    }

    @Override
    public String getState() {
        return state;
    }

    @Override
    public String getPostCode() {
        return postCode;
    }

    @Override
    public String getMerchandiser() {
        return merchandiser;
    }

    @Override
    public String getBusinessName() {
        return businessName;
    }

    @Override
    public String getPigeonCoopID() {
        return pigeonCoopID;
    }
}

