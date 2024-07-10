package au.edu.sydney.brawndo.erp.ordering;

import java.time.LocalDateTime;
import java.util.Set;

public interface Order {
    int getOrderID();
    double getTotalCost();
    LocalDateTime getOrderDate();
    void setProduct(Product product, int qty);
    Set<Product> getAllProducts();
    int getProductQty(Product product);
    String generateInvoiceData();
    int getCustomer();
    void finalise();
    Order copy();
    String shortDesc();
    String longDesc();
}
