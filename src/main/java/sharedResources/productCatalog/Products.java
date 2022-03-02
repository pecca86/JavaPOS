package sharedResources.productCatalog;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import sharedResources.utils.HttpController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

// TODO: Make class implement iterable?
public class Products {

    private final HashMap<Integer, Product> productList;

    public Products () {this.productList = new HashMap<>();}


    /**
     * Searches for all products which keyword matches the given keyword
     *
     * @param keyword User input
     * @return HashMap containing found products
     */
    HashMap<Integer, Product> findByKeyword (String keyword) {
        if (keyword == null || keyword == "" || keyword.equals("*"))
            return this.productList;
        HashMap<Integer, Product> searchResult = new HashMap<>();
        this.productList.forEach((k, v) -> {
            if (v
                    .getKeyword()
                    .toLowerCase()
                    .equals(keyword)) searchResult.put(v.getBarCode(), v);
        });

        return searchResult;
    }

    /**
     * Adds product to the product catalog.
     * FUNCTION NOT IMPLEMENTED AT THE API BACKEND AT THE MOMENT!
     *
     * @param product product to add
     */
    void addProduct (Product product) {this.productList.put(product.getBarCode(), product);}


    /**
     * Looks up all the products that have been added to the productList.
     *
     * @return a hashmap containing all the products inside the productList.
     */
    HashMap<Integer, Product> getAllProducts () {
        return this.productList;
    }


    /**
     * Gets all the products from the Product Catalog API and turns them into Java objects.
     *
     * @param apiURL API URL.
     */
    void fetchProducts (String apiURL) {
        HttpController http = new HttpController("GET", apiURL);
        http.sendRequest();

        String response = http.getResponse();
        JSONObject data = XML.toJSONObject(response);
        JSONArray dataArray = data
                .getJSONObject("products")
                .getJSONArray("product");

        for (Object raw : dataArray) {
            JSONObject p = (JSONObject) raw;
            Product product = Product.fromJson(p);
            this.addProduct(product);
        }

    }

    JSONArray toJson () {
        JSONArray products = new JSONArray();
        for (Product prod : this.productList.values()) products.put(prod.toJson());
        return products;
    }


    /**
     * Returns a product from the product list if barcode matches.
     *
     * @param barCode Integer barCode.
     * @return Returns a product from the product list if barcode matches. Else null.
     */
    Product findProductByBarCode (Integer barCode) {
        return this.productList.get(barCode);
    }

    /**
     * @param kw Keyword to search by
     * @return The matching product otherwise null
     */
    Product[] findProductsKeyword (String kw) {
        Product[] productArray = this.productList
                .values()
                .stream()
                .filter((Product p) -> p
                        .getKeyword()
                        .toLowerCase()
                        .equals(kw.toLowerCase()))
                .toArray(Product[]::new);
        if (productArray.length == 0) return null;
        else return productArray;
    }

    Product getRandomProduct () {
        ArrayList<Integer> barcodes = new ArrayList<>(this.productList.keySet());
        int size = barcodes.size();
        if (size == 0) return null;
        int random = new Random().nextInt(size);
        return this.productList.get(random);
    }

    /**
     * @param kw Keyword to search by
     * @return The matching product otherwise null
     */
    Product findProductName (String kw) {
        Product[] product = this.productList
                .values()
                .stream()
                .filter((Product p) -> p
                        .getName()
                        .toLowerCase()
                        .equals(kw))
                .toArray(Product[]::new);
        return product.length > 0 ? product[0] : null;
    }

    /**
     * Edits the specified product.
     *
     * @param product Product we want to edit.
     */
    void editProduct (Product product) {
        Integer barCode = product.getBarCode();
        //TODO: Check logic
        /*for (Product p : this.productList)
            if (p
                    .getBarCode()
                    .equals(barCode)) {
                p = product;
                this.productList.add(p);
            }*/
    }

    /**
     * Main function implemented so that the class can be tested.
     *
     * @param args
     */
    public static void main (String[] args) {
        Products products = new Products();

        Product p1 = new Product(1, 100, "Milk", 30, "Dairy");
        Product p2 = new Product(2, 200, "Cheese", 40, "Dairy");
        Product p3 = new Product(3, 300, "Beer", 50, "Alcoholic Drink");

        products.addProduct(p1);
        products.addProduct(p2);
        products.addProduct(p3);

        System.out.println(products.findProductByBarCode(200)); // Should fetch Cheese


    }

}
