package au.edu.sydney.brawndo.erp.spfea.ordering;

import au.edu.sydney.brawndo.erp.ordering.Order;
import au.edu.sydney.brawndo.erp.ordering.Product;
import au.edu.sydney.brawndo.erp.ordering.SubscriptionOrder;
import au.edu.sydney.brawndo.erp.spfea.ordering.strategies.discountstrats.DiscountStrategy;
import au.edu.sydney.brawndo.erp.spfea.ordering.strategies.invoicestrats.CustomerInvoiceStrategy;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Concrete implementation of the SubscriptionOrder interface.
 */
public class SubscriptionOrderImpl implements SubscriptionOrder {
    private Map<Product, Integer> products = new HashMap<>();
    private final int id;
    private LocalDateTime date;
    private int customerID;
    private boolean finalised = false;
    private int numShipments;

    private CustomerInvoiceStrategy customerType;
    private DiscountStrategy discountStrat;

    /**
     * Constructs a ConcreteSubscriptionOrder object.
     *
     * @param id              the order ID
     * @param customerID      the customer ID
     * @param date            the order date
     * @param discountStrat   the discount strategy to be applied
     * @param customerType    the customer invoice strategy
     * @param numShipments    the number of shipments
     */
    public SubscriptionOrderImpl(int id, int customerID, LocalDateTime date, DiscountStrategy discountStrat,
                                 CustomerInvoiceStrategy customerType, int numShipments) {
        this.id = id;
        this.date = date;
        this.customerID = customerID;
        this.discountStrat = discountStrat;
        this.customerType = customerType;
        this.numShipments = numShipments;
    }

    @Override
    public int getOrderID() {
        return id;
    }

    @Override
    public double getTotalCost() {
        double cost = discountStrat.calculateCost(this, products);
        return cost * numShipments;
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
                product = contained;
                break;
            }
        }

        products.put(product, qty);
    }

    @Override
    public Set<Product> getAllProducts() {
        return products.keySet();
    }

    @Override
    public int getProductQty(Product product) {
        for (Product contained : products.keySet()) {
            if (contained.equals(product)) {
                product = contained;
                break;
            }
        }

        Integer result = products.get(product);
        return null == result ? 0 : result;
    }

    @Override
    public String generateInvoiceData() {
        return customerType.generateInvoice(this);
    }

    @Override
    public int getCustomer() {
        return customerID;
    }

    @Override
    public void finalise() {
        this.finalised = true;
    }

    @Override
    public Order copy() {
        Order copy = new SubscriptionOrderImpl(id, customerID, date, discountStrat, customerType, numShipments);
        for (Product product : products.keySet()) {
            copy.setProduct(product, products.get(product));
        }
        return copy;
    }

    @Override
    public String shortDesc() {
        return String.format("ID:%s $%,.2f per shipment, $%,.2f total", id, getRecurringCost(), getRecurringCost());
    }

    @Override
    public String longDesc() {
        double fullCost = 0.0;
        double discountedCost = getRecurringCost();
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
                        "Number of shipments: %d\n" +
                        "Products:\n" +
                        "%s" +
                        "\tDiscount: -$%,.2f\n" +
                        "Recurring cost: $%,.2f\n" +
                        "Total cost: $%,.2f\n",
                id,
                date.format(DateTimeFormatter.ISO_LOCAL_DATE),
                numShipments,
                productSB.toString(),
                fullCost - discountedCost,
                getRecurringCost(),
                getTotalCost()
        );
    }

    @Override
    public double getRecurringCost() {
        return discountStrat.calculateCost(this, products);
    }

    @Override
    public int numberOfShipmentsOrdered() {
        return numShipments;
    }
}
