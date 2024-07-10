package au.edu.sydney.brawndo.erp.ordering;

public interface Product {

    /**
     * Note from Frito: You would think you could use productName as an id, but there's more than
     * 1700 cases of duplicate name in the database where the marketing guys decided to see what people's reaction
     * to different recipes or costs would be.
     *
     * @return
     */
    String getProductName();
    double getCost();
    double[] getManufacturingData();
    double[] getRecipeData();
    double[] getMarketingData();
    double[] getSafetyData();
    double[] getLicensingData();
}
