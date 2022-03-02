/**
 * Tests for the product class
 */
package sharedResources.productCatalog;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sharedResources.exceptions.NoSuchCustomerException;
import sharedResources.structures.Customer;
import sharedResources.structures.CustomerRegister;

class ProductTest {

    private Product product;
    private Customer bonusCustomer, customer;

    @BeforeEach
    void setUp () throws NoSuchCustomerException {
        ProductCatalog catalog = ProductCatalog.getInstance();
        catalog.fetchProducts("http://localhost:9003/rest/findByName/*");
        CustomerRegister register = CustomerRegister.getInstance();
        this.bonusCustomer = register.getCustomer(1); // Should be changed to the ID of an active bonus customer for proper testing
        this.customer = register.getCustomer(53); // Should be the ID of a non-bonus customer
        this.product = catalog.getRandomProduct();
    }

    /**
     * Test functionality of setDiscount and getPrice
     * as their functionality is inherently intertwined
     */
    @Test
    void testPriceAndDiscount () {
        long now = System.currentTimeMillis() / 1000;
        assert this.product.getPrice() == 0;

        this.product.setPrice(2.50F);
        assert this.product.getPrice() == 2.50;
        assert this.product.getDiscountedPrice(this.customer) == 2.50;
        assert this.product.getDiscountedPrice(this.bonusCustomer) == 2.50;

        this.product.setDiscount(.25F, now - 3600, now + 3600, true);
        assert this.product.getPrice() == 2.50;
        assert this.product.getDiscountedPrice() == 2.50;
        assert !this.bonusCustomer.activeBonus() || this.product.getDiscountedPrice(this.bonusCustomer) == 1.875;
        assert this.product.getDiscountedPrice(this.customer) == 2.50;

        this.product.setDiscount(.25F, now - 3600, now + 3600, false);
        assert this.product.getDiscountedPrice() == 1.875;
        assert this.product.getDiscountedPrice(this.bonusCustomer) == 1.875;
        assert this.product.getDiscountedPrice(this.customer) == 1.875;

        this.product.setDiscount(.25F, now + 3600, now + 3600, false);
        assert this.product.getPrice() == 2.50;
        assert this.product.getDiscountedPrice(this.bonusCustomer) == 2.50;
        assert this.product.getDiscountedPrice(this.customer) == 2.50;
    }

    @Test
    void toAndFromJson () {
        JSONObject data = this.product.toJson();
        Product product = Product.fromJson(data);
        assert this.product
                .toString()
                .equals(product.toString());
        assert data
                .toString()
                .equals(product
                        .toJson()
                        .toString());
    }

    @Test
    void setPrice () {
        this.product.setPrice(1);
        assert this.product.getPrice() == 1;
        this.product.setPrice(2.5F);
        assert this.product.getPrice() == 2.5;
    }

}