package au.edu.sydney.brawndo.erp.spfea.products;

import java.util.*;

/**
 * FlyWeightFactory is responsible for creating and managing flyweight objects. This class is a singleton
 */
public class FlyWeightFactory {

    // Access to singleton instance
    private static FlyWeightFactory instance;
    private List<Flyweight> productDataCache;

    // Constructor creates ArrayList for data cache
    private FlyWeightFactory() {
        productDataCache = new ArrayList<>();
    }

    /**
     * Returns the singleton instance of the FlyWeightFactory.
     *
     * @return the singleton instance
     */
    public static FlyWeightFactory getInstance() {
        if (instance == null) {
            instance = new FlyWeightFactory();
        }
        return instance;
    }

    /**
     * Returns a flyweight object based on the provided data array. If a flyweight object with the same data already
     * exists in the cache, that object is returned. Otherwise, a new flyweight object is created and added to the cache.
     *
     * @param data the data array
     * @return the flyweight object
     */
    public Flyweight getFlyweight(double[] data) {
        // Return null for null data
        if (data == null) {
            return null;
        }

        // If an existing DataFlyweight object containing matching data[] exists, return the existing flyweight object
        for (Flyweight dataFlyweight : productDataCache) {
            if (Arrays.equals(data, dataFlyweight.getData())) {
                return dataFlyweight;
            }
        }

        // Otherwise, create a new DataFlyweight object storing the new unique data[] and add it to productDataCache
        Flyweight flyweight = new DataFlyweight(data);
        productDataCache.add(flyweight);
        return flyweight;
    }
}


