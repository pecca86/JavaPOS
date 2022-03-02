package backend.structures;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sharedResources.exceptions.NoSuchCustomerException;
import sharedResources.productCatalog.Product;
import sharedResources.productCatalog.ProductCatalog;
import sharedResources.structures.Customer;
import sharedResources.structures.CustomerRegister;

class TransactionTest {

    private Product[] products;
    private Customer customer;
    private Customer bonusCustomer;
    private Transaction transaction;

    @BeforeEach
    void setUp () throws NoSuchCustomerException {
        ProductCatalog catalog = ProductCatalog.getInstance();
        catalog.fetchProducts("http://localhost:9003/rest/findByName/*");
        this.products = new Product[3];
        this.products[0] = catalog.getRandomProduct();
        this.products[1] = catalog.getRandomProduct();
        this.products[2] = catalog.getRandomProduct();
        for (Product product : this.products) product.setPrice(2.50F);

        float discount = .20F;
        long now = System.currentTimeMillis() / 1000;
        this.products[0].setDiscount(discount, now - 3600, now + 3600, true);
        this.products[1].setDiscount(discount, now - 3600, now + 3600, false);

        CustomerRegister register = CustomerRegister.getInstance();
        this.bonusCustomer = register.getCustomer(1); // Should be changed to the ID of an active bonus customer for proper testing
        this.customer = register.getCustomer(53); // Should be the ID of a non-bonus customer

        this.transaction = new Transaction(this.products);
    }

    @Test
    void fromJsonAndToJson () throws JSONException {
        JSONArray input = new JSONArray();
        float total = 0;
        for (Product product : this.products) {
            input.put(product.toJson());
            total += product.getDiscountedPrice();
        }

        Transaction transaction = Transaction.fromJson(input);
        assert transaction.getTotal() == total;

        JSONObject out = transaction.toJson();
        assert out.getDouble("total") == total;
        assert out
                .getJSONArray("items")
                .length() == 3;
    }

    @Test
    void setCustomerAndGetTotal () {
        this.transaction.setCustomer(this.customer);
        float total = 0;
        for (Product product : this.products)
            total += product.getDiscountedPrice(this.customer);
        assert this.transaction.getTotal() == total;

        this.transaction.setCustomer(this.bonusCustomer);
        total = 0;
        for (Product product : this.products)
            total += product.getDiscountedPrice(this.bonusCustomer);
        assert this.transaction.getTotal() == total;
    }

}