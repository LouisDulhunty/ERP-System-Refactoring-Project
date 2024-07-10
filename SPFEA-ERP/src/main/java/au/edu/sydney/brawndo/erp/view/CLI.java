package au.edu.sydney.brawndo.erp.view;

import au.edu.sydney.brawndo.erp.ordering.Customer;
import au.edu.sydney.brawndo.erp.ordering.Product;
import au.edu.sydney.brawndo.erp.spfea.SPFEAFacade;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@SuppressWarnings("Duplicates")
public class CLI {
    private static SPFEAFacade model = new SPFEAFacade();

    public static void main(String[] args) {
        authMenu();
    }

    private static void authMenu() {
        int selection;

        do {
            selection = ViewUtils.displayMenu("Login menu",
                    new String[]{
                            "Login",
                            "Quit",
                    },
                    "Please enter a selection");

            switch (selection) {
                case 1:
                    login();
                    selection = -1;
                    break;
                case 2:
                    return;
                default:
                    // Can't get here
            }
        } while (-1 == selection);
    }

    private static void login() {
        String userName;
        String password;
        boolean auth;

        do {
            userName = ViewUtils.getString("Please enter your username (enter blank to cancel): ", true);
            if ("".equals(userName)) {
                return;
            }
            password = ViewUtils.getString("Please enter your password: (enter blank to cancel)", true);
            if ("".equals(password)) {
                return;
            }

            auth = model.login(userName, password);

            if (!auth) {
                System.out.println("Error, credentials rejected");
            }

        } while (!auth);

        mainMenu();
        model.logout();
    }

    private static void mainMenu() {
        int selection;

        do {
            selection = ViewUtils.displayMenu("Main menu",
                    new String[]{
                            "Customer Actions",
                            "Order Actions",
                            "Product Actions",
                            "Back",
                    },
                    "Please enter a selection");

            switch (selection) {
                case 1:
                    customerMenu();
                    selection = -1;
                    break;
                case 2:
                    orderMenu();
                    selection = -1;
                    break;
                case 3:
                    productMenu();
                    selection = -1;
                    break;
                case 4:
                    return;
                default:
                    // Can't get here
            }
        } while (-1 == selection);
    }

    private static void customerMenu() {
        int selection;

        do {
            selection = ViewUtils.displayMenu("Customer menu",
                    new String[]{
                            "List all",
                            "View",
                            "Back",
                    },
                    "Please enter a selection");

            switch (selection) {
                case 1:
                    listAllCustomers();
                    selection = -1;
                    break;
                case 2:
                    viewCustomerMenu();
                    selection = -1;
                    break;
                case 3:
                    return;
                default:
                    // Can't get here
            }
        } while (-1 == selection);
    }

    private static void listAllCustomers() {
        List<Integer> customers = model.getAllCustomerIDs();

        for (Integer integer: customers) {
            System.out.println("Customer id: " + integer);
        }

    }

    private static void viewCustomerMenu() {
        Integer customerID = ViewUtils.getInt("Please enter customer id or blank to cancel", true);

        if (null == customerID) {
            return;
        }

        Customer customer = model.getCustomer(customerID);

        if (null == customer) {
            System.out.println("No matching customer found");
            return;
        }

        System.out.println("Customer details:");
        System.out.println("ID: " + customerID);
        System.out.println("First Name: " + customer.getfName());
        System.out.println("Last Name: " + customer.getlName());
        System.out.println("Phone: " + customer.getPhoneNumber());
        System.out.println("Email: " + customer.getEmailAddress());
        System.out.println("Merchandiser: " + customer.getMerchandiser());
        System.out.println("Business Name: " + customer.getBusinessName());
        System.out.println("Street Address: " + customer.getAddress());
        System.out.println("Suburb: " + customer.getSuburb());
        System.out.println("State: " + customer.getState());
        System.out.println("Postcode: " + customer.getPostCode());
        System.out.println("Pigeon Coop ID: " + customer.getPigeonCoopID());
    }

    private static void orderMenu() {
        int selection;

        do {
            selection = ViewUtils.displayMenu("Order menu",
                    new String[]{
                            "List all",
                            "View",
                            "Edit",
                            "Remove",
                            "New",
                            "Back"
                    },
                    "Please enter a selection");

            switch (selection) {
                case 1:
                    listAllOrders();
                    selection = -1;
                    break;
                case 2:
                    viewOrderMenu();
                    selection = -1;
                    break;
                case 3:
                    editOrderMenu();
                    selection = -1;
                    break;
                case 4:
                    removeOrder();
                    selection = -1;
                    break;
                case 5:
                    newOrder();
                    selection = -1;
                    break;
                case 6:
                    return;
                default:
                    // Can't get here
            }
        } while (-1 == selection);
    }

    private static void editOrderMenu() {
        int orderID = -1;

        while (-1 == orderID) {
            Integer response = ViewUtils.getInt("Enter an order id to edit, or blank to return: ", true);
            if (null == response) {
                return;
            }
            if (response < 1) {
                System.out.println("Invalid id chosen");
                continue;
            }

            orderID = response;
        }

        editOrder(orderID);
    }

    private static void listAllOrders() {
        List<Integer> orders = model.getAllOrders();
        orders.sort(Comparator.comparing(Integer::intValue));

        System.out.println("Current Orders:\n");
        for (Integer orderID: orders) {
            System.out.println(String.format("%s: $%,.2f", orderID, model.getOrderTotalCost(orderID)));
        }
    }

    private static void viewOrderMenu() {
        int orderID = -1;

        while (-1 == orderID) {
            Integer response = ViewUtils.getInt("Enter an order id to view, or blank to return: ", true);
            if (null == response) {
                return;
            }
            if (response < 1) {
                System.out.println("Invalid id chosen");
                continue;
            }

            orderID = response;
        }

        printOrder(orderID);
    }

    private static void removeOrder() {
        int orderID = -1;

        while (-1 == orderID) {
            Integer response = ViewUtils.getInt("Enter an order id to remove, or blank to return: ", true);
            if (null == response) {
                return;
            }
            if (response < 1) {
                System.out.println("Invalid id chosen");
                continue;
            }

            orderID = response;

            boolean result = model.removeOrder(orderID);

            if (!result) {
                System.out.println("No matching order found.");
                orderID = -1;
            }
        }

        System.out.println("Order removed");

    }

    private static void newOrder() {
        Integer customerID = -1;
        LocalDateTime date = LocalDateTime.now();
        Boolean isBusiness;
        Integer discountType = -1;
        Integer discountThreshold = -1;
        Integer discountRate = -1;
        Boolean isSubscription;
        Integer numShipments = -1;

        while (-1 == customerID) {
            customerID = ViewUtils.getInt("Enter a customer ID or blank to cancel: ", true);
            if (null == customerID) {
                return;
            }

            if (model.getCustomer(customerID) == null) {
                System.out.println("Customer not found.");
                customerID = -1;
            }
        }

        isBusiness = ViewUtils.getBoolean("Is this a business order?: ", false);
        isSubscription = ViewUtils.getBoolean("Is this a subscription order?: ", false);
        if (null == isBusiness || null == isSubscription) return; // just here to keep the linter quiet

        while (-1 == discountType) {
            discountType = ViewUtils.getInt("Is this order using the flat discount (1) or bulk discount (2) strategy?", false);
            if (null == discountType || discountType < 1 || discountType > 2) {
                System.out.println("Invalid discount strategy");
                discountType = -1;
            }
        }

        if (2 == discountType) {
            while (-1 == discountThreshold) {
                discountThreshold = ViewUtils.getInt("Please enter the bulk discount threshold amount", false);
                if (null == discountThreshold || discountThreshold < 1) {
                    System.out.println("Invalid discount threshold");
                    discountThreshold = -1;
                }
            }
        }

        while (-1 == discountRate) {
            discountRate = ViewUtils.getInt("Please enter the discount rate in %", false);
            if (null == discountRate || discountRate < 0 || discountRate > 100) {
                System.out.println("Invalid discount rate");
                discountRate = -1;
            }
        }

        if (isSubscription) {
            while (-1 == numShipments) {
                numShipments = ViewUtils.getInt("Please enter the number of shipments to order", false);
                if (null == numShipments || numShipments < 0) {
                    System.out.println("Invalid number of shipments");
                    numShipments = -1;
                }
            }
        }

        Integer orderID = model.createOrder(customerID, date, isBusiness, isSubscription, discountType, discountThreshold, discountRate, numShipments);

        if (null == orderID) {
            System.out.println("Order creation failed.");
        } else {
            editOrder(orderID);
        }

    }

    private static void editOrder(int orderID) {
        String input = null;

        while (!"finalise".equals(input)) {
            input = ViewUtils.getString("Enter 'list' to list products, 'view' to view current order contents, " +
                    "'set' to set product quantity, 'finalise' to finalise the order, 'cancel' to remove the order, or 'back' to leave the order to be finished later.", false);
            switch (input) {
                case "list":
                    listAllProducts();
                    break;
                case "view":
                    printOrder(orderID);
                    break;
                case "set":
                    Integer productID = ViewUtils.getInt("Please enter a product, or blank to cancel", true);
                    if (null == productID) break;
                    List<Product> products = model.getAllProducts();
                    if (productID < 1 || productID > products.size()) {
                        System.out.println("Invalid product");
                        break;
                    }
                    Integer productQty = ViewUtils.getInt("Please enter a quantity, 0 to remove, or blank to cancel", true);
                    if (null == productQty) break;
                    if (productQty < 0) {
                        System.out.println("Invalid quantity");
                        break;
                    }

                    model.orderLineSet(orderID, products.get(productID - 1), productQty);

                    break;
                case "finalise":
                    Boolean customPriority = ViewUtils.getBoolean("Would you like to use a custom invoicing method list?", false);
                    List<String> contactMethodList = null;
                    if (null != customPriority && customPriority) {
                        contactMethodList = getCustomPriorityList();
                    }
                    if (!model.finaliseOrder(orderID, contactMethodList)) {
                        System.out.println("No matching contact method found - no invoice sent.");
                    }
                    break;
                case "cancel":
                    model.removeOrder(orderID);
                    System.out.println("Order removed.");
                    return;
                case "back":
                    System.out.println("Order " + orderID + " will be left unfinished. Use this order id to return later.");
                    return;
                default:
                    System.out.println("Unknown command");
                    break;
            }
        }
    }

    private static List<String> getCustomPriorityList() {

        String response = null;
        List<String> result = new ArrayList<>();
        List<String> available = model.getKnownContactMethods();

        while (!"end".equals(response)) {
            response = ViewUtils.getString("Enter a contact method, 'end' to finish, blank to list available methods", true);
            if ("".equals(response)) {
                System.out.println("Known methods:");
                for (String method: available) {
                    System.out.println("\t" + method);
                }
            } else if (!"end".equals(response)) {
                boolean match = false;
                for (String method: available) {
                    if (method.toLowerCase().equals(response.toLowerCase())) {
                        match = true;
                    }
                }
                if (match) {
                    result.add(response);
                } else {
                    System.out.println("Unknown method.");
                }
            }
        }

        if (result.size() == 0) {
            return null;
        } else {
            return result;
        }
    }

    private static void productMenu() {
        int selection;

        do {
            selection = ViewUtils.displayMenu("Product menu",
                    new String[]{
                            "List all",
                            "View details",
                            "Back",
                    },
                    "Please enter a selection");

            switch (selection) {
                case 1:
                    listAllProducts();
                    selection = -1;
                    break;
                case 2:
                    showProductDetails();
                    selection = -1;
                    break;
                case 4:
                    return;
                default:
                    // Can't get here
            }
        } while (-1 == selection);
    }

    private static void listAllProducts() {
        List<Product> products = model.getAllProducts();

        System.out.println("All Products:\n");


        for (int i = 1; i <= products.size(); i++) {
            System.out.println(i + ". " + products.get(i - 1));
        }
    }

    private static void showProductDetails() {
        int productID = -1;
        List<Product> products = model.getAllProducts();

        while (-1 == productID) {
            Integer response = ViewUtils.getInt("Enter a product to view, or blank to return: ", true);
            if (null == response) {
                return;
            }
            if (response < 1 || response > products.size()) {
                System.out.println("Invalid product chosen");
                continue;
            }
            productID = response;

        }
        Product product = products.get(productID - 1);

        System.out.println(String.format("%s: $%,.2f", product.getProductName(), product.getCost()));
    }

    private static void printOrder(int orderID) {
        System.out.println(model.getOrderLongDesc(orderID));
    }
}
