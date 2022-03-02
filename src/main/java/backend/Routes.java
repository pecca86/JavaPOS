package backend;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sharedResources.exceptions.MissingDataException;
import sharedResources.exceptions.NoSuchCustomerException;
import sharedResources.productCatalog.Product;
import sharedResources.productCatalog.ProductCatalog;

/**
 * Class for defining the various routes for the API
 */
@RestController
public class Routes {


    /**
     * @param rawBody Request body should be an object with at least a "sales" property,
     *                with an array of sold items in a transaction,
     *                if the customer is a bonus customer, a property "customer"
     *                with the customer data should also be included
     *                ex. {"sales": [{"productId":1, "price": 1.50, "barcode":123123}], "customer": 0}
     */
    @RequestMapping(method = RequestMethod.POST, path = "/api/sale", consumes = "application/json")
    public ResponseEntity<String> recordSale (@RequestBody String rawBody) {
        JSONObject body = new JSONObject(rawBody);
        if (!body.has("sales")) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        System.out.println(rawBody);
        System.out.println(body);
        try {
            Analytics
                    .getInstance()
                    .recordSale(body);
        } catch (MissingDataException | JSONException ex) {
            // Return 400
            System.out.println("Missing data: " + ex.getMessage());
            ex.printStackTrace();
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (NoSuchCustomerException ex) {
            // Return 404
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception ex) {
            // Return 500
            StringBuilder trace = new StringBuilder();
            for (StackTraceElement str : ex.getStackTrace())
                trace
                        .append(str)
                        .append("\n");
            System.out.println("Exception during handling of new sale:\n" + ex.getMessage() + "\n" + trace);
            return new ResponseEntity<>("Internal error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Get all sales or sales by specific customer
     *
     * @param customer Optional customer ID
     * @return JSON response
     */
    @RequestMapping(method = RequestMethod.GET, path = { "/api/sales/{customer}", "/api/sales" }, produces = "application/json")
    public ResponseEntity<String> popular (@PathVariable(required = false) String customer) {
        if (customer != null) try {
            int id = Integer.parseInt(customer);
            return new ResponseEntity<>(Analytics
                    .getInstance()
                    .getSales(id)
                    .toString(), HttpStatus.OK);
        } catch (NumberFormatException ex) {
            return new ResponseEntity<>("Customer must be integer", HttpStatus.BAD_REQUEST);
        } catch (NoSuchCustomerException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
        }
        else return new ResponseEntity<>(Analytics
                .getInstance()
                .getSales()
                .toString(), HttpStatus.OK);
    }

    /**
     * Get sold items grouped by date and barcode
     *
     * @return JSON response with the data
     */
    @RequestMapping(method = RequestMethod.GET, path = { "/api/sales/bydate" }, produces = "application/json")
    public ResponseEntity<String> popularByDate () {
        JSONObject data = Analytics
                .getInstance()
                .getSalesByDate();
        return new ResponseEntity<>(data.toString(), HttpStatus.OK);
    }

    /**
     * Get popular items for a time range for bonus customers
     *
     * @param start Range start as a UNIX timestamp
     * @param end   Range end as a UNIX timestamp
     * @return A JSON Object with the data if found
     */
    @RequestMapping(method = RequestMethod.GET, path = { "/api/popular/{start}-{end}/bonus" }, produces = "application/json")
    public ResponseEntity<String> popularBonus (@PathVariable() int start, @PathVariable() int end) {
        try {
            JSONArray popular = Analytics
                    .getInstance()
                    .getPopularBonus(start, end);
            return new ResponseEntity<>(popular.toString(), HttpStatus.OK);
        } catch (NoSuchCustomerException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
        }

    }

    /**
     * Get popular items for a time range
     *
     * @param start Range start as a UNIX timestamp
     * @param end   Range end as a UNIX timestamp
     * @return A JSON Object with the data.
     */
    @RequestMapping(method = RequestMethod.GET, path = { "/api/popular/{start}-{end}" }, produces = "application/json")
    public ResponseEntity<String> popular (@PathVariable() int start, @PathVariable() int end) {
        JSONObject popular = Analytics
                .getInstance()
                .getPopular(start, end);

        return new ResponseEntity<>(popular.toString(), HttpStatus.OK);
    }

    /**
     * Retrieve the product catalog
     *
     * @return JSON representation of the entire product catalog
     */
    @RequestMapping(method = RequestMethod.GET, path = { "/api/productcatalog" }, produces = "application/json")
    public ResponseEntity<String> productCatalog () {
        ProductCatalog catalog = ProductCatalog.getInstance();
        return new ResponseEntity<>(catalog
                .toJson()
                .toString(), HttpStatus.OK);
    }

    /**
     * Get a product by its barcode
     *
     * @param barcode Product barcode
     * @return Product data in JSON
     */
    @RequestMapping(method = RequestMethod.GET, path = { "/api/productcatalog/barcode/{barcode}" }, produces = "application/json")
    public ResponseEntity<String> getByBarcode (@PathVariable int barcode) {
        ProductCatalog catalog = ProductCatalog.getInstance();
        if (!catalog.has(barcode))
            return new ResponseEntity<>(JSONObject.NULL.toString(), HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(catalog
                .findProductByBarCode(barcode)
                .toJson()
                .toString(), HttpStatus.OK);
    }

    /**
     * Retrieve products by keyword
     *
     * @param keyword Keyword to search by
     * @return Array of product data matching keyword
     */
    @RequestMapping(method = RequestMethod.GET, path = { "/api/productcatalog/keyword/{keyword}" }, produces = "application/json")
    public ResponseEntity<String> getByKeyword (@PathVariable String keyword) {
        if (keyword == null)
            return new ResponseEntity<>("Missing keyword", HttpStatus.BAD_REQUEST);
        ProductCatalog catalog = ProductCatalog.getInstance();
        JSONArray products = catalog.findProductsKeyword(keyword);
        if (products == null)
            return new ResponseEntity<>(JSONObject.NULL.toString(), HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(products.toString(), HttpStatus.OK);
    }

    /**
     * Retrieve product by its name
     *
     * @param name Product name
     * @return Product data in JSON
     */
    @RequestMapping(method = RequestMethod.GET, path = { "/api/productcatalog/name/{name}" }, produces = "application/json")
    public ResponseEntity<String> getByName (@PathVariable String name) {
        if (name == null)
            return new ResponseEntity<>("Missing name", HttpStatus.BAD_REQUEST);
        ProductCatalog catalog = ProductCatalog.getInstance();
        Product product = catalog.findProductName(name);
        if (product == null)
            return new ResponseEntity<>(JSONObject.NULL.toString(), HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(product
                .toJson()
                .toString(), HttpStatus.OK);
    }

    /**
     * Set the price of a product
     *
     * @param barcode The product barcode
     * @param body    JSON body with a price property
     * @return 204 if successful
     */
    @RequestMapping(method = RequestMethod.POST, path = { "/api/product/{barcode}/price" }, consumes = "application/json", produces = "application/json")
    public ResponseEntity<String> setPrice (@PathVariable int barcode, @RequestBody String body) {
        ProductCatalog catalog = ProductCatalog.getInstance();
        if (!catalog.has(barcode)) return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        float price;
        try {
            JSONObject json = new JSONObject(body);
            price = json.getFloat("price");
        } catch (JSONException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }

        catalog
                .getProduct(barcode)
                .setPrice(price);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);

    }

    /**
     * Define discounts for products or product groups
     *
     * @param body A JSON body containing a products property with product barcodes to apply the discount to,
     *             alternatively a keyword property with a keyword for which product category to apply discount to.
     *             Also should contain properties from, until, bonusOnly and discount with their respective values
     * @return 204 if successful
     */
    @RequestMapping(method = RequestMethod.POST, path = { "/api/product/discount" }, consumes = "application/json", produces = "application/json")
    public ResponseEntity<String> setDiscount (@RequestBody String body) {
        ProductCatalog catalog = ProductCatalog.getInstance();

        float discount;
        long from, until;
        JSONArray products;
        String keyword;
        boolean bonusOnly;
        try {
            JSONObject json = new JSONObject(body);
            discount = json.getFloat("discount");
            from = json.getLong("from");
            until = json.getLong("until");
            products = json.optJSONArray("products");
            keyword = json.optString("keyword");
            bonusOnly = json.optBoolean("bonusOnly", false);
        } catch (JSONException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }

        if (keyword == null && (products == null || products == JSONObject.NULL))
            return new ResponseEntity<>("Must provide either keyword or array of product ids.", HttpStatus.BAD_REQUEST);

        if (discount > 1 || discount < 0)
            return new ResponseEntity<>("Discount value must be between 0 and 1", HttpStatus.BAD_REQUEST);
        if (products != null)
            catalog.setDiscounts(products, from, until, discount, bonusOnly);
        else catalog.setDiscounts(keyword, from, until, discount, bonusOnly);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);

    }

}