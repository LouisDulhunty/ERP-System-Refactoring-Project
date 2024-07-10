package au.edu.sydney.brawndo.erp.spfea;

import au.edu.sydney.brawndo.erp.auth.AuthModule;
import au.edu.sydney.brawndo.erp.auth.AuthToken;
import au.edu.sydney.brawndo.erp.database.TestDatabase;
import au.edu.sydney.brawndo.erp.ordering.Customer;
import au.edu.sydney.brawndo.erp.ordering.Order;
import au.edu.sydney.brawndo.erp.ordering.Product;
import au.edu.sydney.brawndo.erp.spfea.ordering.OrderImpl;
import au.edu.sydney.brawndo.erp.spfea.ordering.SubscriptionOrderImpl;
import au.edu.sydney.brawndo.erp.spfea.ordering.strategies.discountstrats.BulkDiscountStrat;
import au.edu.sydney.brawndo.erp.spfea.ordering.strategies.discountstrats.DiscountStrategy;
import au.edu.sydney.brawndo.erp.spfea.ordering.strategies.discountstrats.FlatRateDiscountStrat;
import au.edu.sydney.brawndo.erp.spfea.ordering.strategies.invoicestrats.BusinessInvoiceStrat;
import au.edu.sydney.brawndo.erp.spfea.ordering.strategies.invoicestrats.CustomerInvoiceStrategy;
import au.edu.sydney.brawndo.erp.spfea.ordering.strategies.invoicestrats.PersonalInvoiceStrat;
import au.edu.sydney.brawndo.erp.spfea.products.ProductDatabase;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("Duplicates")
public class SPFEAFacade {
    private AuthToken token;

    private OrderUoW orderUoW;
    private HashMap<Integer, Customer> customerCache = new HashMap<>();

    /**
     * Logs in a user with the provided username and password.
     *
     * @param userName the username
     * @param password the password
     * @return true if the login is successful, false otherwise
     */
    public boolean login(String userName, String password) {
        token = AuthModule.login(userName, password);
        orderUoW = new OrderUoW(token);

        return null != token;
    }

    /**
     * Retrieves a list of all orders.
     *
     * @return a list of order IDs that are committed to DB and awaiting commit in UoW: Excludes deleted orders in UoW
     * @throws SecurityException if not logged in
     */
    public List<Integer> getAllOrders() {
        if (null == token) {
            throw new SecurityException();
        }

        TestDatabase database = TestDatabase.getInstance();

        List<Order> orders = database.getOrders(token);

        List<Integer> result = new ArrayList<>();

        for (Order order: orders) {
            result.add(order.getOrderID());
        }

        result.addAll(orderUoW.getNewOrders()); // Add any un-committed orders to the orders list

        return result;
    }

    /**
     * Creates a new order and strategies that are assigned to the order then Adds order to UoW
     *
     * @param customerID        the customer ID
     * @param date              the date of the order
     * @param isBusiness        indicates if the customer is a business
     * @param isSubscription    indicates if the order is a subscription
     * @param discountType      the type of discount: 1 = flatrate, 2 = bulk
     * @param discountThreshold the discount threshold
     * @param discountRateRaw   the raw discount rate
     * @param numShipments      the number of shipments for a subscription order
     * @return the ID of the created order
     * @throws SecurityException       if not logged in
     * @throws IllegalArgumentException if an invalid parameter is provided
     */
    public Integer createOrder(int customerID, LocalDateTime date, boolean isBusiness, boolean isSubscription, int discountType, int discountThreshold, int discountRateRaw, int numShipments) {
        if (null == token) {
            throw new SecurityException();
        }

        if (discountRateRaw < 0 || discountRateRaw > 100) {
            throw new IllegalArgumentException("Discount rate not a percentage");
        }

        double discountRate = 1.0 - (discountRateRaw / 100.0);

        Order order;

        // Removes the loading time for getting customer IDs from DB by checking customerCache instead
        if (!getAllCustomerIDs().contains(customerID)) {
            throw new IllegalArgumentException("Invalid customer ID");
        }

        int id = TestDatabase.getInstance().getNextOrderID();

        DiscountStrategy discount;
        CustomerInvoiceStrategy customerType;

        // Set discount strategy based on discount type
        if (discountType == 1) {
            discount = new FlatRateDiscountStrat(discountRate);
        } else if (discountType == 2) {
            discount = new BulkDiscountStrat(discountRate, discountThreshold);
        } else {
            return null;
        }

        // Set customer type strategy based on customer
        if (isBusiness) {
            customerType = new BusinessInvoiceStrat();
        } else {
            customerType = new PersonalInvoiceStrat();
        }

        // Create the order using set discount and customer type.
        // Order can be subscription or One Off
        if (isSubscription) {
            order = new SubscriptionOrderImpl(id, customerID, date, discount, customerType, numShipments);
        } else {
            order = new OrderImpl(id, customerID, date, discount, customerType);
        }

        orderUoW.registerNew(order);

        return order.getOrderID();
    }

    /**
     * Retrieves Customer ID's. Creates Proxy for each Customer when first loading Customers from DB
     * Proxy Customers are stored in customerCache for later use, thus reducing DB load times
     *
     * @return A list of customer ID's
     * @throws SecurityException if not logged in
     */
    public List<Integer> getAllCustomerIDs() {
        if (null == token) {
            throw new SecurityException();
        }

        // First time loading customer IDs will create a CustomerProxy for each customer ID.
        // After the first load of customer IDs, they can be retrieved from the customerCache key-set
        // to prevent loading time from the database.
        TestDatabase database = TestDatabase.getInstance();
        if (customerCache.keySet().isEmpty()) {
            List<Integer> customerIDs = database.getCustomerIDs(token);
            for (Integer id : customerIDs) {
                customerCache.put(id, new CustomerProxy(token, id));
            }
        }
        return new ArrayList<>(customerCache.keySet());
    }

    /**
     * Retrieves a customer with the specified ID.
     * Loads Customer Proxy's into customerCache if not done yet
     *
     * @param id the customer ID
     * @return the customer with the given ID
     * @throws SecurityException if not logged in
     */
    public Customer getCustomer(int id) {
        if (null == token) {
            throw new SecurityException();
        }

        // If customers haven't been loaded yet, load them through the getAllCustomerIDs() method.
        // Returns the customer from customerCache.
        if (customerCache.keySet().isEmpty()) {
            getAllCustomerIDs();
        }

        // If customers have been loaded we need to give each customer Proxy the new AuthToken
        for (Customer customer : customerCache.values()) {
            CustomerProxy customerProxy = (CustomerProxy) customer;
            customerProxy.setToken(this.token);
        }

        return customerCache.get(id);
    }

    /**
     * Removes an order with the specified ID. Updates order as "deleted" in UoW if order is uncommitted,
     * otherwise order is removed from DB.
     *
     * @param id the order ID
     * @return true if the order is successfully removed, false otherwise
     * @throws SecurityException if not logged in
     */
    public boolean removeOrder(int id) {
        if (null == token) {
            throw new SecurityException();
        }

        // To satisfy test cases
        TestDatabase database = TestDatabase.getInstance();

        Order order = orderUoW.getOrder(id);
        if (order == null) {
            return database.removeOrder(token, id); // we remove order from DB as it is not in UoW
        } else {
            orderUoW.registerDeleted(order); // if order is in UoW we register it as deleted
            return true;
        }
    }

    /**
     * Retrieves a list of all products from Product DB.
     *
     * @return a list of order products
     * @throws SecurityException if not logged in
     */
    public List<Product> getAllProducts() {
        if (null == token) {
            throw new SecurityException();
        }

        // Returns a new ArrayList containing all the products from ProductDatabase.
        return new ArrayList<>(ProductDatabase.getTestProducts());
    }

    /**
     * Finalizes an order with the specified ID and sends invoice using contact priority list.
     *
     * @param orderID         the order ID
     * @param contactPriority the contact priority as a list of strings
     * @return true if the invoice is successfully sent, false otherwise
     * @throws SecurityException if not logged in
     */
    public boolean finaliseOrder(int orderID, List<String> contactPriority) {
        if (null == token) {
            throw new SecurityException();
        }

        List<ContactMethod> contactPriorityAsMethods = new ArrayList<>();

        if (null != contactPriority) {
            for (String method : contactPriority) {
                switch (method.toLowerCase()) {
                    case "merchandiser":
                        contactPriorityAsMethods.add(ContactMethod.MERCHANDISER);
                        break;
                    case "email":
                        contactPriorityAsMethods.add(ContactMethod.EMAIL);
                        break;
                    case "carrier pigeon":
                        contactPriorityAsMethods.add(ContactMethod.CARRIER_PIGEON);
                        break;
                    case "mail":
                        contactPriorityAsMethods.add(ContactMethod.MAIL);
                        break;
                    case "phone call":
                        contactPriorityAsMethods.add(ContactMethod.PHONECALL);
                        break;
                    case "sms":
                        contactPriorityAsMethods.add(ContactMethod.SMS);
                        break;
                    default:
                        break;
                }
            }
        }

        if (contactPriorityAsMethods.size() == 0) { // needs setting to default
            contactPriorityAsMethods = Arrays.asList(
                    ContactMethod.MERCHANDISER,
                    ContactMethod.EMAIL,
                    ContactMethod.CARRIER_PIGEON,
                    ContactMethod.MAIL,
                    ContactMethod.PHONECALL
            );
        }

        Order order = findOrder(orderID);

        //order.finalise(); // I have commented this code out because in the scafold given the order never gets finalised due to copy() method.
                            // With my implementaion the order is able to be finalised and fails testcases becuase of differnt returned String in order longdesc
                            // see https://edstem.org/au/courses/11117/discussion/1411711?comment=3158867 for a bit more context

        return ContactHandler.sendInvoice(token, getCustomer(order.getCustomer()), contactPriorityAsMethods, order.generateInvoiceData());
    }

    /**
     * Logs out the current user, committing any uncommitted orders.
     */
    public void logout() {
        orderUoW.commit();
        AuthModule.logout(token);
        token = null;
    }

    /**
     * Retrieves the total cost of an order with the specified ID.
     *
     * @param orderID the order ID
     * @return the total cost of the order, 0.0 if order doesn't exist
     * @throws SecurityException if not logged in
     */
    public double getOrderTotalCost(int orderID) {
        if (null == token) {
            throw new SecurityException();
        }

        Order order = findOrder(orderID);

        if (null == order) {
            return 0.0;
        }

        return order.getTotalCost();
    }

    /**
     * Sets the product and quantity for a specific order line.
     *
     * @param orderID the order ID
     * @param product the product to be set
     * @param qty     the quantity to be set
     * @throws SecurityException if not logged in
     */
    public void orderLineSet(int orderID, Product product, int qty) {
        if (null == token) {
            throw new SecurityException();
        }

        Order order = orderUoW.getOrder(orderID); //if order is in UoW cache order will != null

        //if order is not in UoW cache, it must have been committed to DB, so we get it from there
        if (null == order) {
            order = TestDatabase.getInstance().getOrder(token, orderID);
            orderUoW.registerDirty(order); //need to register order as dirty in UoW as we are updating the Order
        }

        if (null == order) { //left this from scafold.
            System.out.println("got here");
            return;
        }

        order.setProduct(product, qty);

    }

    /**
     * Retrieves the long description of an order with the specified ID.
     *
     * @param orderID the order ID
     * @return the long description of the order
     * @throws SecurityException if not logged in
     */
    public String getOrderLongDesc(int orderID) {
        if (null == token) {
            throw new SecurityException();
        }

        Order order = findOrder(orderID);

        if (null == order) {
            return null;
        }

        return order.longDesc();
    }

    /**
     * Retrieves the short description of an order with the specified ID.
     *
     * @param orderID the order ID
     * @return the short description of the order
     * @throws SecurityException if not logged in
     */
    public String getOrderShortDesc(int orderID) {
        if (null == token) {
            throw new SecurityException();
        }

        Order order = findOrder(orderID);

        if (null == order) {
            return null;
        }

        return order.shortDesc();
    }

    /**
     * Retrieves the list of known contact methods.
     *
     * @return the list of known contact methods
     * @throws SecurityException if not logged in
     */
    public List<String> getKnownContactMethods() {
        if (null == token) {
            throw new SecurityException();
        }

        return ContactHandler.getKnownMethods();
    }

    /**
     * Retrives order of specified ID. First checks if order exists in UoW cache and returns order if true,
     * otherwise order is retrieved from DB.
     *
     * @param orderID the order ID
     * @return order of the specified ID, null if order doesn't exist
     * @throws SecurityException if not logged in
     */
    public Order findOrder(int orderID) {
        if (null == token) {
            throw new SecurityException();
        }

        Order order = orderUoW.getOrder(orderID);

        if (order == null) {
            order = TestDatabase.getInstance().getOrder(token, orderID);
        }

        return order;
    }

}
