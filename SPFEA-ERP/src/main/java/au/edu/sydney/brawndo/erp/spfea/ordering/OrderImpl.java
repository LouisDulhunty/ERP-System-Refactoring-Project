package au.edu.sydney.brawndo.erp.spfea.ordering;

import au.edu.sydney.brawndo.erp.ordering.Order;
import au.edu.sydney.brawndo.erp.ordering.Product;
import au.edu.sydney.brawndo.erp.spfea.ordering.strategies.discountstrats.DiscountStrategy;
import au.edu.sydney.brawndo.erp.spfea.ordering.strategies.invoicestrats.CustomerInvoiceStrategy;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * ConcreteOrder represents an order in the Brawndo ERP system.
 */
public class OrderImpl implements Order {

    private Map<Product, Integer> products = new HashMap<>(); // Map of products and their quantities
    private final int id; // Order ID
    private LocalDateTime date; // Order date and time
    private int customerID; // ID of the customer associated with the order
    private boolean finalised = false; // Indicates if the order has been finalized
    private DiscountStrategy discountStrat; // Discount strategy for the order
    private CustomerInvoiceStrategy customerType; // Invoice strategy for the order

    /**
     * Constructs a ConcreteOrder object: A Regular Non-Subscription Order
     *
     * @param id             the ID of the order
     * @param customerID     the ID of the customer associated with the order
     * @param date           the date and time of the order
     * @param discountStrat  the discount strategy for the order
     * @param customerType   the invoice strategy for the order
     */
    public OrderImpl(int id, int customerID, LocalDateTime date, DiscountStrategy discountStrat,
                     CustomerInvoiceStrategy customerType) {
        this.id = id;
        this.date = date;
        this.customerID = customerID;
        this.discountStrat = discountStrat;
        this.customerType = customerType;
    }

    @Override
    public int getOrderID() {
        return id;
    }

    @Override
    public double getTotalCost() {
        double cost = discountStrat.calculateCost(this, products); // Calculate the total cost of the order using specified Discount Strategy
        return cost;
    }

    @Override
    public LocalDateTime getOrderDate() {
        return date;
    }

    @Override
    public void setProduct(Product product, int qty) {
        if (finalised) {
            throw new IllegalStateException("Order was already finalised.");
        }

        // We can't rely on like products having the same object identity since they get
        // rebuilt over the network, so we had to check for presence and same values

        for (Product contained : products.keySet()) {
            if (contained.equals(product)) {
                product = contained; // Update the reference to an existing product with the same values
                break;
            }
        }

        products.put(product, qty); // Add the product and its quantity to the order
    }

    @Override
    public Set<Product> getAllProducts() {
        return products.keySet();
    }

    @Override
    public int getProductQty(Product product) {
        for (Product contained : products.keySet()) {
            if (contained.equals(product)) {
                product = contained; // Update the reference to an existing product with the same values
                break;
            }
        }

        Integer result = products.get(product); // Get the quantity of the specified product
        return null == result ? 0 : result;
    }

    @Override
    public String generateInvoiceData() {
        return customerType.generateInvoice(this); // Generate invoice data using the customer's invoice strategy
    }

    @Override
    public int getCustomer() {
        return customerID;
    }

    @Override
    public void finalise() {
        this.finalised = true; // Mark the order as finalized
    }

    @Override
    public Order copy() {
        Order copy = new OrderImpl(id, customerID, date, discountStrat, customerType); // Create a copy of the order
        for (Product product : products.keySet()) {
            copy.setProduct(product, products.get(product)); // Copy the products and their quantities to the new order
        }
        return copy;
    }

    @Override
    public String shortDesc() {
        return String.format("ID:%s $%,.2f", id, getTotalCost());
    }

    @Override
    public String longDesc() {
        double fullCost = 0.0;
        double discountedCost = getTotalCost();
        StringBuilder productSB = new StringBuilder();

        List<Product> keyList = new ArrayList<>(products.keySet());
        keyList.sort(Comparator.comparing(Product::getProductName).thenComparing(Product::getCost));

        for (Product product : keyList) {
            double subtotal = product.getCost() * products.get(product);
            fullCost += subtotal;

            productSB.append(String.format("\tProduct name: %s\tQty: %d\tUnit cost: $%,.2f\tSubtotal: $%,.2f\n",
                    product.getProductName(),
                    products.get(product),
                    product.getCost(),
                    subtotal));
        }

        return String.format(finalised ? "" : "*NOT FINALISED*\n" +
                        "Order details (id #%d)\n" +
                        "Date: %s\n" +
                        "Products:\n" +
                        "%s" +
                        "\tDiscount: -$%,.2f\n" +
                        "Total cost: $%,.2f\n",
                id,
                date.format(DateTimeFormatter.ISO_LOCAL_DATE),
                productSB.toString(),
                fullCost - discountedCost,
                discountedCost
        );

    }
}

