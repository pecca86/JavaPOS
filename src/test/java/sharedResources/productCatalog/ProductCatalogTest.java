/**
 * Tests for the product catalog
 */
package sharedResources.productCatalog;

import frontend.cashflowFX.CustomerViewController;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

public class ProductCatalogTest {

    private ProductCatalog productCatalog;
    private CustomerViewController customerViewController;

    /**
     * Sets up the test
     */
    @BeforeEach
    public void setUp() {
        this.productCatalog = ProductCatalog.getInstance();
        this.productCatalog.fetchProducts("http://localhost:9003/rest/findByName/*");
    }


    /**
     * Resets the product catalog
     */
    @AfterEach
    public void tearDown() {
        this.productCatalog = null;
    }


    /**
     * Test adding a product
     */
    @Test
    public void testAddProduct() {
        Product p = new Product(1, 123, "Milk", 20F, "Milch");
        this.productCatalog.addProduct(p);
        assert productCatalog.getProduct(123).getBarCode() == p.getBarCode();
    }


    /**
     * Test finding a product by it's bar code
     */
    @Test
    public void findProductByBarCode() {
        Product p = new Product(1, 234, "Milk", 20F, "Milch");
        this.productCatalog.addProduct(p);
        assert this.productCatalog.findProductByBarCode(234) == p;
        assert this.productCatalog.findProductByBarCode(3352) == null;
    }


    /**
     * Finds all products with the given keyword
     */
    @Test
    public void findProductsByKeyword() {
        Product p = new Product(1, 234, "Milk", 20F, "Milch");
        Product p1 = new Product(2, 345, "Cheese", 20F, "Milch");
        Product p2 = new Product(3, 456, "Cream", 20F, "Milch");

        this.productCatalog.addProduct(p);
        this.productCatalog.addProduct(p1);
        this.productCatalog.addProduct(p2);

        assert this.productCatalog.findByKeyword("milch").containsKey(p.getBarCode()) == true;
        assert this.productCatalog.findByKeyword("milch").containsKey(p1.getBarCode()) == true;
        assert this.productCatalog.findByKeyword("milch").containsKey(p2.getBarCode()) == true;

        assert this.productCatalog.findByKeyword("*").containsKey(p2.getBarCode()) == true;
    }


    /**
     * Check if product catalog can be polled if it contains a product by it's barcod
     */
    @Test
    public void checkIfCatalogContainsProduct() {
        Product p = new Product(1, 234, "Milk", 20F, "Milch");
        this.productCatalog.addProduct(p);

        assert this.productCatalog.has(p.getBarCode()) == true;
        assert this.productCatalog.has(74574) == false;
    }


    @Test
    public void testGetAllProducts() {
        Product p = new Product(66, 555, "Milk", 20F, "Milch");
        Product p1 = new Product(77, 789, "Cheese", 20F, "Milch");
        Product p2 = new Product(88, 909, "Cream", 20F, "Milch");

        int originalSize = this.productCatalog.getAllProducts().size();
        this.productCatalog.addProduct(p);
        this.productCatalog.addProduct(p1);
        this.productCatalog.addProduct(p2);

        assert this.productCatalog.getAllProducts().size() == originalSize + 3;
    }

}
