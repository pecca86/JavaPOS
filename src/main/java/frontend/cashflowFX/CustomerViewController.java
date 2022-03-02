/**
 * Controller that is responsible for the logic inside the Customer View.
 */
package frontend.cashflowFX;

import frontend.interfaces.Window;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import sharedResources.productCatalog.Product;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller
 */
public class CustomerViewController implements Initializable, Window {

    @FXML
    private TableView customerProductTable = new TableView();

    @FXML
    private Label cartTotalSum;


    /**
     * Initialize the stage
     *
     * @param url
     * @param resourceBundle
     */
    @Override
    public void initialize (URL url, ResourceBundle resourceBundle) {
        this.createCartTable();
        this.cartTotalSum.setText("0");
    }


    /**
     * Close the stage
     */
    void close () {
        Stage stage = (Stage) this.customerProductTable
                .getScene()
                .getWindow();
        stage.close();
    }


    /**
     * Creates the cart TableView element
     */
    private void createCartTable () {
        this.customerProductTable.setPlaceholder(new Label("Cart is Empty"));
        TableColumn<Product, String> column1 = new TableColumn<>("Name");
        // This needs to match the object's property name!
        column1.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Product, Float> column2 = new TableColumn<>("Price");
        column2.setCellValueFactory(new PropertyValueFactory<>("price"));

        this.customerProductTable
                .getColumns()
                .add(column1);
        this.customerProductTable
                .getColumns()
                .add(column2);
    }


    /**
     * Allows other controllers to add products to this view
     *
     * @param p
     */
    void addProduct (Product p) {
        this.customerProductTable
                .getItems()
                .add(p);
    }


    /**
     * Allows other controllers to remove products from this view
     *
     * @param p
     */
    void removeProduct (Product p) {
        this.customerProductTable
                .getItems()
                .remove(p);
    }


    /**
     * Sets the cart total to given string
     *
     * @param sum Sum as a string
     */
    void setCartTotalSum (String sum) {
        this.cartTotalSum.setText(sum);
    }


    /**
     * Finds a product based on it's id and replaces it with the given product
     *
     * @param product product we want to replace with
     * @param index   index of replacable product
     */
    void findProductAndUpdate (Product product, int index) {
        for (Object data : this.customerProductTable.getItems()) {
            Product p = (Product) data;
            if (p.getProductId() == product.getProductId()) this.customerProductTable
                    .getItems()
                    .set(index, product);
        }
    }
}
