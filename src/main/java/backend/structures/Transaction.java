package backend.structures;

import org.json.JSONArray;
import org.json.JSONObject;
import sharedResources.productCatalog.Product;
import sharedResources.structures.Customer;

/**
 * Represents a sale.
 * Contains the customer, the items and the total and various other data about the sale
 */
public class Transaction {

    private Customer customer;
    private final long timestamp;
    private float discount;
    private float total;
    private final Product[] items;

    public Transaction (Product[] items) {
        this.items = items;
        this.total = 0;
        for (Product product : items) this.total += product.getDiscountedPrice();
        this.timestamp = System.currentTimeMillis() / 1000;
    }

    /**
     * @param itemsData Raw json data from which to construct the transaction
     * @return The newly created transaction object
     */
    public static Transaction fromJson (JSONArray itemsData) {
        Product[] items = new Product[itemsData.length()];

        for (int i = 0; i < itemsData.length(); i++) {
            JSONObject itemData = itemsData.getJSONObject(i);
            Product product = Product.fromJson(itemData);
            items[i] = product;
        }
        return new Transaction(items);
    }

    private Transaction addDiscount (float discount) {
        this.discount = discount;
        return this;
    }

    public Transaction setCustomer (Customer c) {
        this.customer = c;
        this.total = 0;
        for (Product product : this.items)
            this.total += product.getDiscountedPrice(c);
        return this;
    }

    float getTotal () {return this.total;}

    public float getDiscount () {return this.discount;}

    public long getTimestamp () {return this.timestamp;}

    public Customer getCustomer () {return this.customer;}

    public Product[] getItems () {return this.items;}

    public JSONObject toJson () {
        JSONObject transaction = new JSONObject();
        transaction.put("timestamp", this.timestamp);
        transaction.put("total", this.total);
        transaction.put("discount", this.discount);

        JSONArray items = new JSONArray();
        for (Product item : this.items) items.put(item.toJson());
        transaction.put("items", items);

        if (this.customer != null)
            transaction.put("customerNo", this.customer.getId());
        return transaction;
    }

    @Override
    public String toString () {
        return String.format("""
                TRANSACTION @ %d
                Total: %f
                Item count: %d
                Discount: %f
                Customer: %s""", this.timestamp, this.total, this.items.length, this.discount, this.customer);
    }

}
