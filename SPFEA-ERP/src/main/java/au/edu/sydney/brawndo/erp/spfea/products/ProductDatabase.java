package au.edu.sydney.brawndo.erp.spfea.products;

import au.edu.sydney.brawndo.erp.ordering.Product;

import java.util.*;

public class ProductDatabase {

    private static Map<String, Product> products;
    private static String[] productNameVers = {
            "Brawndo Original1",
            "Brawndo New1",
            "Brawndo Double Strength1",
            "Brawndo Quadruple Strength1",
            "Brawndo Only The Strongest Potions1",
            "Brawndo Mild1",
            "Brawndo Odin Warb1",
            "Brawndo Classic1",
            "Brawndo H2-NO!1",
            "Brawndo It's Got Electrolytes!1",
            "Brawndo Original2",
            "Brawndo New2",
            "Brawndo Double Strength2",
            "Brawndo Quadruple Strength2",
            "Brawndo Only The Strongest Potions2",
            "Brawndo Mild2",
            "Brawndo Odin Warb2",
            "Brawndo Classic2",
            "Brawndo H2-NO!2",
            "Brawndo It's Got Electrolytes!2",};

    static {
        products = new HashMap<>();

        for (String name: productNameVers) {
            products.put(name, new ProductImpl(name.substring(0, name.length()-1), getProductCost(), getProductData(), getProductData(), getProductData(), getProductData(), getProductData()));
        }
    }

    /**
     * Note from Frito:
     * The version number is entirely arbitrary and not used anywhere else, so we can't use it as a composite key...
     * There are also more than 2 versions per name in the full system.
     *
     * The recreation/copying here is simulating a networked database connection
     */
    public static Collection<Product> getTestProducts() {

        Collection<Product> originals = products.values();
        List<Product> result = new ArrayList<>();

        for (Product original: originals) {
            result.add(new ProductImpl(original.getProductName(),
                    original.getCost(),
                    original.getManufacturingData().clone(),
                    original.getRecipeData().clone(),
                    original.getMarketingData().clone(),
                    original.getSafetyData().clone(),
                    original.getLicensingData().clone()));

        }

        return result;
    }

    private static double[] getProductData() {

        /*
        Note from Frito:
        If your machine's memory can't handle the whole product data, you could set this lower while you work out
        a way to stop the RAM explosion? We do need to be able to get at the whole product though.
        The database doesn't though...
         */

        double[] result = new double[500000];
        Random random = new Random();

        for (int i = 0; i < result.length; i++) {
            result[i] = random.nextDouble();
        }

        return result;
    }

    private static double getProductCost() {
        Random random = new Random();

        return 1.0 + 99.0 * random.nextDouble();
    }
}
