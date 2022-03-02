package sharedResources.productCatalog;

import org.json.JSONArray;

import java.util.HashMap;

public class ProductCatalog {

    private static ProductCatalog instance;

    private final Products products;

    private ProductCatalog () {this.products = new Products();}

    public static ProductCatalog getInstance () {
        if (instance == null) instance = new ProductCatalog();
        return instance;
    }


    /**
     * Searches for all products which keyword matches the given keyword
     * @param keyword User input
     * @return HashMap containing found products
     */
    public HashMap<Integer, Product> findByKeyword(String keyword) {
        return this.products.findByKeyword(keyword);
    }

    /**
     * Check if a product by barcode is present in the catalog
     *
     * @param barcode the barcode to check for
     * @return true if present, otherwise false
     */
    public boolean has (int barcode) {
        return this.products
                .getAllProducts()
                .containsKey(barcode);
    }

    /**
     * @param kw Keyword to search by
     * @return The matching product otherwise null
     */
    public JSONArray findProductsKeyword (String kw) {
        Product[] productArray = this.products.findProductsKeyword(kw);
        JSONArray products = new JSONArray();
        for (Product product : productArray) products.put(product.toJson());
        return products;
    }

    /**
     * @param kw Keyword to search by
     * @return The matching product otherwise null
     */
    public Product findProductName (String kw) {
        return this.products.findProductName(kw);
    }

    /**
     * Fetches all available products from the product catalog API.
     *
     * @param apiURL API URL.
     */
    public void fetchProducts (String apiURL) {
        this.products.fetchProducts(apiURL);
    }

    /**
     * Calls products to retrieves its products
     *
     * @return A HashMap containing product with k: Id and v: Product
     */
    public HashMap<Integer, Product> getAllProducts () {
        return this.products.getAllProducts();
    }

    /**
     * Adds a product to the product catalog.
     * FUNCTION NOT IMPLEMENTED AT THE API BACKEND AT THE MOMENT!
     *
     * @param product product we want to add.
     */
    public void addProduct (Product product) {
        this.products.addProduct(product);
    }

    /**
     * Alias for findProductByBarCode
     *
     * @param barcode
     * @return
     */
    public Product getProduct (int barcode) {
        return this.findProductByBarCode(barcode);
    }

    /**
     * Get a random product from the catalog, for testing purposes
     *
     * @return A random product
     */
    public Product getRandomProduct () {
        return this.products.getRandomProduct();
    }

    /**
     * Finds a product based on its bar code
     *
     * @param barCode product's bar code
     */
    public Product findProductByBarCode (Integer barCode) {
        return this.products.findProductByBarCode(barCode);
    }

    public void setDiscounts (JSONArray products, long from, long until, float discount, boolean bonusOnly) {

        Product[] productsArray = new Product[products.length()];
        for (int index = 0; index < products.length(); index++) {
            Product product = this.getProduct((int) products.get(index));
            productsArray[index] = product;
        }
        this.setDiscounts(productsArray, from, until, discount, bonusOnly);

    }

    public void setDiscounts (String kw, long from, long until, float discount, boolean bonusOnly) {

        Product[] products = this.products.findProductsKeyword(kw);
        this.setDiscounts(products, from, until, discount, bonusOnly);

    }

    private void setDiscounts (Product[] products, long from, long until, float discount, boolean bonusOnly) {
        for (Product product : products)
            product.setDiscount(discount, from, until, bonusOnly);
    }

    /**
     * Edits the chosen product.
     *
     * @param product product we want to edit.
     */
    public void editProduct (Product product) {
        this.products.editProduct(product);
    }

    public JSONArray toJson () {
        return this.products.toJson();
    }

}
