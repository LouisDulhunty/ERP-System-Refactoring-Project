package au.edu.sydney.brawndo.erp.spfea.products;

/**
 * DataFlyweight represents a concrete implementation of the Flyweight interface.
 */
public class DataFlyweight implements Flyweight {

    private double[] productData;

    /**
     * Constructs a DataFlyweight object with the provided product data.
     *
     * @param productData the product data
     */
    public DataFlyweight(double[] productData) {
        this.productData = productData;
    }

    @Override
    public double[] getData() {
        return productData;
    }
}
