package au.edu.sydney.brawndo.erp.spfea;

import au.edu.sydney.brawndo.erp.auth.AuthToken;
import au.edu.sydney.brawndo.erp.ordering.Customer;

/**
 * The CustomerProxy class serves as a virtual proxy for a customer, lazily loading the customer data when needed.
 */
public class CustomerProxy implements Customer {
    private AuthToken token;
    private final int id;
    private CustomerImpl customer;

    /**
     * Constructs a CustomerProxy instance.
     *
     * @param token the authentication token
     * @param id    the customer ID
     */
    public CustomerProxy(AuthToken token, int id) {
        this.token = token;
        this.id = id;
    }

    /**
     * Lazily loads the customer data.
     *
     * @return the loaded customer instance
     */
    private CustomerImpl loadCustomer() {
        if (customer == null) {
            customer = new CustomerImpl(token, id);
        }
        return customer;
    }

    public void setToken(AuthToken token) {
        this.token = token;
    }

    public int getId() {
        return loadCustomer().getId();
    }

    @Override
    public String getfName() {
        return loadCustomer().getfName();
    }

    @Override
    public String getlName() {
        return loadCustomer().getlName();
    }

    @Override
    public String getPhoneNumber() {
        return loadCustomer().getPhoneNumber();
    }

    @Override
    public String getEmailAddress() {
        return loadCustomer().getEmailAddress();
    }

    @Override
    public String getAddress() {
        return loadCustomer().getAddress();
    }

    @Override
    public String getSuburb() {
        return loadCustomer().getSuburb();
    }

    @Override
    public String getState() {
        return loadCustomer().getState();
    }

    @Override
    public String getPostCode() {
        return loadCustomer().getPostCode();
    }

    @Override
    public String getMerchandiser() {
        return loadCustomer().getMerchandiser();
    }

    @Override
    public String getBusinessName() {
        return loadCustomer().getBusinessName();
    }

    @Override
    public String getPigeonCoopID() {
        return loadCustomer().getPigeonCoopID();
    }
}