package au.edu.sydney.brawndo.erp.spfea.products;

import au.edu.sydney.brawndo.erp.ordering.Product;
import java.util.Arrays;
import java.util.Objects;

/*
 * NOTE FOR MARKER:
 * "ProductDatabase is basically just giving dummy values, for the actual codebase you can assume non-unique values are more common.
 * As such, if your solution at least technically would reduce RAM usage, then it's fine" - Liza via Ed A2 FAQ: https://edstem.org/au/courses/11117/discussion/1355799
 *
 * Based off this, I have assumed that the same data arrays will be commonly shared across different product instances. With the data arrays containing massive amounts of data,
 * sharing data array flyweights across different product instances will reduce RAM usage as their location in memory can be shared
 *  a case could be argued that flyweights for the name and cost could also be shared however their load on RAM is much less in comparison to the arrays.
 * */


/**
 * ProductImpl represents an implementation of the Product interface.
 */
public class ProductImpl implements Product {

    private final String name;
    private final double cost;
    private final Flyweight manufacturingData;
    private final Flyweight recipeData;
    private final Flyweight marketingData;
    private final Flyweight safetyData;
    private final Flyweight licensingData;

    /**
     * Constructs a ProductImpl object.
     *
     * @param name             the name of the product
     * @param cost             the cost of the product
     * @param manufacturingData the manufacturing data for the product
     * @param recipeData       the recipe data for the product
     * @param marketingData    the marketing data for the product
     * @param safetyData       the safety data for the product
     * @param licensingData    the licensing data for the product
     */
    public ProductImpl(String name,
                       double cost,
                       double[] manufacturingData,
                       double[] recipeData,
                       double[] marketingData,
                       double[] safetyData,
                       double[] licensingData) {
        // Return singleton instance of Flyweight Factory
        FlyWeightFactory factorySingleton = FlyWeightFactory.getInstance();

        this.name = name;
        this.cost = cost;
        this.manufacturingData = factorySingleton.getFlyweight(manufacturingData);
        this.recipeData = factorySingleton.getFlyweight(recipeData);
        this.marketingData = factorySingleton.getFlyweight(marketingData);
        this.safetyData = factorySingleton.getFlyweight(safetyData);
        this.licensingData = factorySingleton.getFlyweight(licensingData);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(name, cost);
        result = 31 * result + Arrays.hashCode(getManufacturingData());
        result = 31 * result + Arrays.hashCode(getRecipeData());
        result = 31 * result + Arrays.hashCode(getMarketingData());
        result = 31 * result + Arrays.hashCode(getSafetyData());
        result = 31 * result + Arrays.hashCode(getLicensingData());
        return result;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (!(o instanceof Product)) {
            return false;
        }

        Product product = (Product) o;

        return Objects.equals(product.getCost(), this.getCost()) &&
                Objects.equals(product.getProductName(), this.getProductName()) &&
                Arrays.equals(product.getManufacturingData(), this.getManufacturingData()) &&
                Arrays.equals(product.getRecipeData(), this.getRecipeData()) &&
                Arrays.equals(product.getMarketingData(), this.getMarketingData()) &&
                Arrays.equals(product.getSafetyData(), this.getSafetyData()) &&
                Arrays.equals(product.getLicensingData(), this.getLicensingData());
    }

    @Override
    public String getProductName() {
        return name;
    }

    @Override
    public double getCost() {
        return cost;
    }

    @Override
    public double[] getManufacturingData() {
        if (manufacturingData == null) {
            return null;
        } else {
            return manufacturingData.getData();
        }
    }

    @Override
    public double[] getRecipeData() {
        if (recipeData == null) {
            return null;
        } else {
            return recipeData.getData();
        }
    }

    @Override
    public double[] getMarketingData() {
        if (marketingData == null) {
            return null;
        } else {
            return marketingData.getData();
        }
    }

    @Override
    public double[] getSafetyData() {
        if (safetyData == null) {
            return null;
        } else {
            return safetyData.getData();
        }
    }

    @Override
    public double[] getLicensingData() {
        if (licensingData == null) {
            return null;
        } else {
            return licensingData.getData();
        }
    }

    @Override
    public String toString() {
        return String.format("%s", name);
    }
}