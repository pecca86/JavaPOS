/**
 * Controller that is responsible for the logic inside the cashier UI.
 * Also responsible for loading in product's from the backend and sending sales data back to the backend.
 */
package frontend.cashflowFX;

import frontend.interfaces.Window;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.print.PageLayout;
import javafx.print.PrinterJob;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.converter.FloatStringConverter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import sharedResources.exceptions.NoSuchCustomerException;
import sharedResources.productCatalog.Product;
import sharedResources.productCatalog.ProductCatalog;
import sharedResources.structures.Customer;
import sharedResources.structures.CustomerRegister;
import sharedResources.utils.HttpController;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

public class CashierTransactionViewController implements Window, Initializable {
    // Formats floats with . as separator
    private final NumberFormat nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
    private DecimalFormat decimalFormat = new DecimalFormat("##.00");

    // CONTAINERS
    @FXML
    private BorderPane mainPane;
    @FXML
    private GridPane gridPaneLowLeft;

    // CARD READER RELATED
    @FXML
    private TextField cardReaderSum;
    private float sum;
    @FXML
    private Label cartSum;

    // REQUEST RELATED
    private HttpController httpController;

    // CONTAINERS
    private ProductCatalog productCatalog;

    // BARCODE RELATED
    @FXML
    private TextField barCodeField;

    @FXML
    private TextArea cashierMessageCenter;

    // CASHBOX RELATED
    @FXML
    private TextField cashAmount;

    // SEARCH RELATED
    @FXML
    private TextField searchField;


    // PRODUCT RELATED
    @FXML
    private TableView productTable;
    @FXML
    private TableView cartTable;
    @FXML
    private Button setDiscountBtn;
    @FXML
    private TextField discountAmountField;

    // SALE RELATED
    @FXML
    private Button finishSaleBtn;
    @FXML
    private Button printReceiptBtn;
    @FXML
    private Button payBtn;

    // CUSTOMER RELATED
    private CustomerRegister customerRegister;
    private Customer customer;
    private Customer bonusCustomer;
    // Checks if customer has already received a bonus customer discount or not
    private boolean hasNotReceivedBonus = true;
    @FXML
    private Label greetLabel;

    // SHELF RELATED
    private boolean isShelved = false;
    @FXML
    private Button shelfBtn;
    @FXML
    private BorderPane borderPaneMiddle;
    @FXML
    private ScrollPane scrollPaneMiddle;
    @FXML
    private HBox hBoxLow;


    //==== CUSTOMER VIEW ===
    private CustomerViewController customerViewController;


    /**
     * How we initialize the window
     *
     * @param url
     * @param resourceBundle
     */
    @Override
    public void initialize (URL url, ResourceBundle resourceBundle) {
        this.decimalFormat = (DecimalFormat) this.nf;

        // Initialize the customer view
        try {
            this.showCustomerDialog();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Init Customer Register
        this.customerRegister = CustomerRegister.getInstance();

        // Initialize
        this.createProducts();
        this.getProducts();
        this.createProductTable();
        this.initializeProductTable();
        this.createCartTable();

        // Adds all event listeners
        this.addListeners();
    }


    /**
     * Create the Customer View Stage and initialize the controller
     *
     * @return the stage
     */
    private Stage showCustomerDialog () throws IOException {
        Stage stage;
        FXMLLoader loader = new FXMLLoader(this
                .getClass()
                .getResource("/frontend/cashflowFX/customer-view.fxml"));
        stage = new Stage(StageStyle.DECORATED);
        stage.setScene(new Scene(loader.load()));
        this.customerViewController = loader.getController();
        //controller.createCartTable();
        stage.show();
        return stage;
    }


    /**
     * Creates the cart TableView element
     */
    private void createCartTable () {
        this.cartTable.setPlaceholder(new Label("Cart is Empty"));
        TableColumn<Product, String> column1 = new TableColumn<>("Name");
        // This needs to match the object's property name!
        column1.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Product, Float> column2 = new TableColumn<>("Price");
        column2.setCellValueFactory(new PropertyValueFactory<>("price"));

        this.cartTable
                .getColumns()
                .add(column1);
        this.cartTable
                .getColumns()
                .add(column2);
    }


    /**
     * Creates the product TableView element
     */
    private void createProductTable () {
        this.productTable.setPlaceholder(new Label("Product Table is empty"));
        TableColumn<Product, String> nameColumn = new TableColumn<>("Name");
        // This needs to match the object's property name!
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Product, String> priceColumn = new TableColumn<>("Price");
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));


        this.productTable
                .getColumns()
                .add(nameColumn);

        this.productTable
                .getColumns()
                .add(priceColumn);
    }


    /**
     * Initializes the product TableView with products from out backend
     */
    private void initializeProductTable () {
        this.productCatalog
                .getAllProducts()
                .forEach((k, v) -> this.productTable
                        .getItems()
                        .add(v));
    }


    /**
     * Retrieves items from the product catalog
     */
    private void getProducts () {
        HashMap<Integer, Product> products = this.productCatalog.getAllProducts();
    }


    /**
     * Creates the product catalog
     */
    private void createProducts () {
        try {
            this.productCatalog = ProductCatalog.getInstance();
            HttpController http = new HttpController("GET", "http://localhost:8080/api/productcatalog");
            http.sendRequest();
            String response = http.getResponse();

            JSONArray array = new JSONArray(response);
            for (Object entry : array) {
                JSONObject productData = (JSONObject) entry;
                Product product = Product.fromJson(productData);
                if (product.getDiscount() > 0 && !product.getBonusOnlyDiscount())
                    product.setPrice(product.getDiscountedPrice());
                this.productCatalog.addProduct(product);
            }

        } catch (JSONException e) {
            System.out.println("Failed parsing the JSON @CashierTransactionViewController");
            e.printStackTrace();
        }
    }


    /**
     * centralized place where we add all listeners
     */
    private void addListeners () {
        this.addProductEventListener();
        this.removeCartItemEventListener();
        this.onBarCodeScannedEventListener();
        this.cashPaymentListener();
        this.onFinishSaleListener();
        this.onPrintReceiptListener();
        this.onChangeProductPriceListener();
        this.onAddDiscount();
        this.searchByKeyWordListener();
        this.shelfTransactionListener();
        this.receivePaymentListener();
    }


    /**
     * Listens on if shelfBtn is clicked
     */
    private void shelfTransactionListener () {
        this.shelfBtn.setOnMouseClicked(e -> this.toggleIsShelved());
    }


    /**
     * Action when payBtn is clicked.
     */
    private void receivePaymentListener () {
        this.payBtn.setOnMouseClicked(e -> this.onReceivePayment());
    }


    /**
     * Reveals the payment control board to the cashier
     */
    private void onReceivePayment () {
        this.gridPaneLowLeft.setVisible(true);
        this.payBtn.setVisible(false);
    }


    /**
     * Disables all buttons while transaction is on shelf
     */
    private void toggleIsShelved () {
        this.isShelved = !this.isShelved;
        this.borderPaneMiddle.setDisable(this.isShelved);
        this.scrollPaneMiddle.setDisable(this.isShelved);
        this.hBoxLow.setDisable(this.isShelved);
        this.cartTable.setDisable(this.isShelved);
        if (this.isShelved) this.shelfBtn.setText("Unshelf");
        else this.shelfBtn.setText("Shelf");
    }


    /**
     * Filters products to the view according to product keyword
     */
    private void searchByKeyWordListener () {
        this.searchField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) this.onSearchByKeyword();
        });
    }

    @FXML
    private void onSearchByKeyword () {
        this.productTable
                .getItems()
                .clear();
        String keyword = this.searchField
                .getText()
                .toLowerCase();
        this.productCatalog
                .findByKeyword(keyword)
                .forEach((k, v) -> this.productTable
                        .getItems()
                        .add(v));
    }

    private void onAddDiscount () {
        this.setDiscountBtn.setOnMouseClicked(e -> this.setDiscount());
    }


    /**
     * Listens on if INSERT key is pressed inside the Cart table.
     */
    private void onChangeProductPriceListener () {
        this.cartTable.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.I) {

                // Get the columns from the cart table
                ObservableList<TableColumn> columns = this.cartTable.getColumns();
                // Make the cart table editable
                this.cartTable.setEditable(true);

                // Get the float values from the column and convert it to a string
                columns
                        .get(1)
                        .setCellFactory(TextFieldTableCell.forTableColumn(new FloatStringConverter()));

                // Action when edit is committed ( = ENTER key pressed )
                ((TableColumn<Product, Float>) columns.get(1)).setOnEditCommit(evt -> {
                    this.setCartTotalSum(evt.getNewValue(), evt.getOldValue());
                    evt
                            .getRowValue()
                            .setPrice(evt.getNewValue());
                    Product product = evt.getRowValue();

                    // Also update the customer side
                    int indexOfSelectedProduct = this.cartTable
                            .getSelectionModel()
                            .getSelectedIndex();
                    this.customerViewController.findProductAndUpdate(product, indexOfSelectedProduct);
                    this.customerViewController.setCartTotalSum(this.cartSum.getText());
                });
            }
        });
    }


    /**
     * Sets the cart total sum after a change to the product has been made.
     *
     * @param newSum User entered sum
     * @param oldSum The product's old sum
     */
    private void setCartTotalSum (float newSum, float oldSum) {
        float updatedSum = newSum - oldSum;
        //this.cartSum
        float cs = Float.parseFloat(this.cartSum.getText());
        float replace = cs + updatedSum;
        replace = Float.parseFloat(this.decimalFormat.format(replace));

        //this.sum = cs + updatedSum;
        this.sum = replace;
        this.cartSum.setText(Float.toString(this.sum));
    }


    /**
     * Listens on if the printReceiptBtn has been pressed
     */
    private void onPrintReceiptListener () {
        this.printReceiptBtn.setOnMouseClicked(click -> this.onPrintReceipt());

    }


    /**
     * Listens on if the finishSaleBtn has been pressed
     */
    private void onFinishSaleListener () {
        this.finishSaleBtn.setOnMouseClicked(click -> this.onFinishSale());
    }


    /**
     * Send sale data to our backend
     */
    private void sendSaleData () {
        JSONObject transaction = new JSONObject();
        //ArrayList<Product> productList = new ArrayList<>();
        JSONArray productList = new JSONArray();

        for (Object product : this.cartTable.getItems())
            productList.put((((Product) product).toJson()));

        transaction.put("sales", productList);

        if (this.bonusCustomer != null)
            transaction.put("customer", this.bonusCustomer.getId());

        HttpController httpController = new HttpController("POST", "http://localhost:8080/api/sale", "application/json", transaction.toString());
        httpController.sendRequest();
    }


    /**
     * Listens on if cashier has entered amount of received cash inside the cashAmount TextField
     */
    private void cashPaymentListener () {
        this.cashAmount.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) this.onPayedWithCash();
        });
    }


    /**
     * Listens on if a product is clicked twice inside the product list
     */
    private void addProductEventListener () {
        this.productTable.setOnMouseClicked(click -> {
            if (click.getClickCount() == 2) this.onAddProductToCart();
        });
    }


    /**
     * Listens on double-clicks in items in the cart
     */
    private void removeCartItemEventListener () {
        this.cartTable.setOnMouseClicked(click -> {
            if (click.getClickCount() == 2) this.onRemoveProductFromCart();
        });
    }


    /**
     * Listens on if ENTER is pressed inside the bar code text field
     */
    private void onBarCodeScannedEventListener () {
        this.barCodeField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) this.onScanBarCode();
        });
    }


    /**
     * If the window is closed without the transaction being finished
     */
    boolean exitWithoutSaving () {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit without saving?");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to abort the sale?");

        ButtonType buttonTypeYes = new ButtonType("Yes", ButtonBar.ButtonData.OK_DONE);
        ButtonType buttonTypeCancel = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert
                .getButtonTypes()
                .setAll(buttonTypeYes, buttonTypeCancel);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == buttonTypeYes) {
            this.customerViewController.close();
            return true;
        }
        return false;
    }


    /**
     * Prompts if cashier wants to finish the sale
     */
    private void onFinishSale () {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Finish Sale");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to finish the sale?");

        ButtonType buttonTypeYes = new ButtonType("Yes", ButtonBar.ButtonData.OK_DONE);
        ButtonType buttonTypeCancel = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert
                .getButtonTypes()
                .setAll(buttonTypeYes, buttonTypeCancel);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == buttonTypeYes) {
            this.sendSaleData();
            // Get the stage we are in
            Stage stage = (Stage) this.mainPane
                    .getScene()
                    .getWindow();
            // Close the windows
            stage.close();
            // Close the customer window
            this.customerViewController.close();
        }
    }


    /**
     * Prints receipt to a png image file
     */
    private void onPrintToImage () {
        try {
            // Set date and time to file name to avoid name crashes
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy_MM_dd-HH-mm-ss");
            LocalDateTime now = LocalDateTime.now();
            String sb = "kvitto-" + dtf.format(now) + ".png";

            // Create new image file buffer
            BufferedImage img = new BufferedImage(500, 600, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = img.createGraphics();

            // Paint to the canvas
            g2d.setColor(Color.white);
            g2d.fillRect(0, 0, 500, 600);
            g2d.setColor(Color.black);

            // Set on which heigh to begin painting
            int height = 20;
            String[] sList = this.cashierMessageCenter
                    .getText()
                    .split("\n");
            for (String s : sList) {
                g2d.drawString(s, 20, height);
                height += 20;
            }

            // Clear out the object from memory
            g2d.dispose();

            // Create String rep of file name
            File imgFile = new File(sb);
            ImageIO.write(img, "png", imgFile);


        } catch (IOException e) {
            System.out.println("Failed creating receipt image file!");
        }
    }

    /**
     * Prints the receipt to the cashierMessageCenter textarea and opens a printer dialog
     */
    private void onPrintReceipt () {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        StringBuilder sb = new StringBuilder(dtf.format(now) + "\n\n");
        // Check if sale involved a bonus customer
        if (this.bonusCustomer != null)
            sb.append("Thank you for shopping with us " + this.bonusCustomer.getName() + "!\nYou earned 5 bonus points!\n\n");

        // Set variable for cart total sum
        float receiptTotalSum = 0F;

        // ObservableList gets the values from inside the TableView
        ObservableList<TableColumn> columns = this.cartTable.getColumns();
        for (Object row : this.cartTable.getItems()) {
            for (TableColumn column : columns)
                if (column
                        .getCellObservableValue(row)
                        .getValue() instanceof String) sb.append(column
                        .getCellObservableValue(row)
                        .getValue() + "____");
                else {
                    sb
                            .append(column
                                    .getCellObservableValue(row)
                                    .getValue())
                            .append("€");
                    receiptTotalSum = receiptTotalSum + (Float) column
                            .getCellObservableValue(row)
                            .getValue();
                }
            sb.append("\n");
        }

        sb.append("\nTotal: " + receiptTotalSum + "€");
        this.cashierMessageCenter.setText(sb.toString());

        // Print to image
        this.onPrintToImage();

        // Create the printing area and
        TextFlow printArea = new TextFlow(new Text(this.cashierMessageCenter.getText()));

        PrinterJob printerJob = PrinterJob.createPrinterJob();

        if (printerJob != null && printerJob.showPrintDialog(this.cashierMessageCenter
                .getScene()
                .getWindow())) {
            PageLayout pageLayout = printerJob
                    .getJobSettings()
                    .getPageLayout();
            printArea.setMaxWidth(pageLayout.getPrintableWidth());

            // done printing
            if (printerJob.printPage(printArea)) printerJob.endJob();
            else System.out.println("Failed to print");
        } else System.out.println("Cancelled");
    }


    /**
     * Gets the product we want to add to the cart
     */
    private void onAddProductToCart () {
        try {
            Product p = (Product) this.productTable
                    .getSelectionModel()
                    .getSelectedItem();
            Product clone = p.clone();
            this.addProductToCart(clone);
        } catch (CloneNotSupportedException e) {
            System.out.println("Object not clonable!");
            e.printStackTrace();
        }
    }


    /**
     * Adds a product to the cart
     *
     * @param p the product we wish to add
     */
    private void addProductToCart (Product p) {
        this.customerViewController.addProduct(p);
        this.cartTable
                .getItems()
                .add(p);

        // Calculates the cart sum after the item is added
        this.sum += p.getPrice();
        this.cartSum.setText(Float.toString(this.sum));
        // Also update the customer view
        this.customerViewController.setCartTotalSum(this.cartSum.getText());
    }


    /**
     * Removes a product from the cart
     *
     * @param p the product we wish to remove
     */
    private void removeProductFromCart (Product p) {
        this.cartTable
                .getItems()
                .remove(p);

        // Calculated the cart sum after the item is removed
        float productSum = p.getPrice();
        productSum = Float.parseFloat(this.decimalFormat.format(productSum));
        this.sum -= productSum;
        this.sum = Float.parseFloat(this.decimalFormat.format(this.sum));
        this.cartSum.setText(Float.toString(this.sum));
        // Also update the customer view
        this.customerViewController.setCartTotalSum(this.cartSum.getText());
    }


    /**
     * Removes an item that is double-clicked from the cart
     */
    private void onRemoveProductFromCart () {
        Product p = (Product) this.cartTable
                .getSelectionModel()
                .getSelectedItem();
        this.customerViewController.removeProduct(p);
        this.removeProductFromCart(p);
    }


    /**
     * Adds a product to the cart by requesting it from the Product Catalog API
     */
    @FXML
    private void onScanBarCode () {
        String scannedBarCode = this.barCodeField.getText();

        if (scannedBarCode.equals("")) {
            this.cashierMessageCenter.setText("No product barcode entered!");
            return;
        }

        int barCode = Integer.parseInt(scannedBarCode);

        if (!this.productCatalog.has(barCode)) {
            this.cashierMessageCenter.setText("No products found with given barcode!");
            return;
        }

        Product p = this.productCatalog.findProductByBarCode(barCode);

        this.addProductToCart(p);
        this.barCodeField.setText("");
    }


    /**
     * Reduce the sum that was payed from the cart, if more, display how much change the customer gets back
     */
    private void onPayedWithCash () {
        float payedSum = Float.parseFloat(this.cashAmount.getText());
        this.paymentSuccess(payedSum);
        this.cashAmount.setText("");
    }


    /**
     * Takes the percentage value entered inside the discount amount text field and applies it to the product.
     * Also updates the price at the customer side and the cart total.
     */
    private void setDiscount () {
        try {
            Product product = (Product) this.cartTable
                    .getSelectionModel()
                    .getSelectedItem();
            float oldPrice = product.getPrice();
            float discount;
            if (!((this.discountAmountField.getText() == null || this.discountAmountField
                    .getText()
                    .length() <= 0))) {
                discount = Float.parseFloat(this.discountAmountField.getText());
                if (discount > 100)
                    this.cashierMessageCenter.setText("Discount cannot be greater than 100%!");
                else {
                    discount = 1 - (discount / 100);
                    // Calculated the new price and round to just 2 decimals
                    float discountedPrice = product.getPrice() * discount;
                    discountedPrice = Float.parseFloat(this.decimalFormat.format(discountedPrice));
                    product.setPrice(discountedPrice);
                }
            } else
                this.cashierMessageCenter.setText("Please enter a percentage value to the discount!");
            // Update cart total
            this.setCartTotalSum(product.getPrice(), oldPrice);
            // Refresh the view
            this.cartTable.refresh();
            // Also update the customer side
            int indexOfSelectedProduct = this.cartTable
                    .getSelectionModel()
                    .getSelectedIndex();
            this.customerViewController.findProductAndUpdate(product, indexOfSelectedProduct);
            this.customerViewController.setCartTotalSum(this.cartSum.getText());
        } catch (NullPointerException e) {
            System.out.println("No item selected!");
        }
    }


    /**
     * Button that calls a HttpController class which communicates with the Cashbox API.
     * Expects the Cashbox's status as a String (OPEN / CLOSED). Else The user is presented with an error message.
     * The message is then appended to the corresponding label.
     */
    @FXML
    private void onCheckCashBoxStatus () {
        this.httpController = new HttpController("GET", "http://localhost:9001/cashbox/status");
        this.httpController.sendRequest();
        this.cashierMessageCenter.setText(this.httpController.getResponse());
    }


    /**
     * Button that sends a POST request through our HttpController.
     * Expects the Cashbox to open upon success. Else the user is presented with an error message.
     * The message is then appended to the corresponding label.
     */
    @FXML
    private void onOpenCashbox () {
        this.httpController = new HttpController("POST", "http://localhost:9001/cashbox/open");
        this.httpController.sendRequest();
        this.cashAmount.setVisible(true);
    }


    /**
     * Gets the status of the card reader API
     */
    @FXML
    private void onCardReaderStatus () {
        this.httpController = new HttpController("GET", "http://localhost:9002/cardreader/status");
        this.httpController.sendRequest();
        this.cashierMessageCenter.setText("Card Reader Status is: " + this.httpController.getResponse());
    }


    /**
     * Sends the entered sum to the card reader API
     */
    @FXML
    private void onWaitForPayment () {
        try {
            float enteredSum = Float.parseFloat(this.cardReaderSum.getText());
            // Check if the sum entered to the card reader is greater than the cart sum
            if (enteredSum > this.sum) {
                this.cashierMessageCenter.setText("Charged sum is greater than cart total!");
                return;
            }
            String headerSum = "amount=" + enteredSum;
            this.httpController = new HttpController("POST", "http://localhost:9002/cardreader/waitForPayment", "application/x-www-form-urlencoded", headerSum);
            this.httpController.sendRequest();
            this.cashierMessageCenter.setText("Waiting for payment..." + this.httpController.getResponse());
        } catch (NumberFormatException e) {
            System.out.println("Only numbers are accepted!");
        }
    }


    /**
     * Send an abort message to the card reader API
     */
    @FXML
    private void onAbortPayment () {
        this.httpController = new HttpController("POST", "http://localhost:9002/cardreader/abort");
        this.httpController.sendRequest();
        this.cashierMessageCenter.setText("Payment aborted!");
    }


    /**
     * Requests the card reader status from the card reader API
     */
    @FXML
    private void onGetSwipeResult () {
        this.httpController = new HttpController("GET", "http://localhost:9002/cardreader/result");
        this.httpController.sendRequest();
        String response = this.httpController.getResponse();
        if (response != null && response.length() > 0) {
            JSONObject jsonData = swipeResultToJSON(response);
            this.parseSwipeResult(jsonData);
        } else this.cashierMessageCenter.setText("No card swiped.");
    }


    /**
     * Turns the swipe result string into a JSON object.
     *
     * @param response String
     * @return a JSON object
     */
    private static JSONObject swipeResultToJSON (String response) {
        try {
            JSONObject data = XML.toJSONObject(response);
            return data.getJSONObject("result");
        } catch (JSONException e) {
            System.out.println("Error in swipe result!");
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Parses the JSON data into more readable format for the end user
     *
     * @param jsonData Data to be parsed
     */
    private void parseSwipeResult (JSONObject jsonData) {

        if (jsonData.has("bonusCardNumber") && jsonData.has("paymentCardNumber")) {
            // Check if valid bonus customer
            if (this.bonusCardIsValid(jsonData)) {
                this.cashierMessageCenter.appendText("\nBonus card number: " + jsonData.get("bonusCardNumber") + "\n" + "Bonus card state : " + jsonData.get("bonusState") + "\n" + "Good trough month: " + jsonData.get("goodThruMonth") + "\n" + "Good trough year: " + jsonData.get("goodThruYear") + "\n" + "Payment card number: " + jsonData.get("paymentCardNumber") + "\n" + "Payment state: " + jsonData.get("paymentState") + "\n" + "Payment card type: " + jsonData.get("paymentCardType") + "\n" + "===============\n");
                if (this.hasNotReceivedBonus) this.updateBonusCustomerPrice();
                this.handlePayment(jsonData, "combined");
            } else {
                this.cashierMessageCenter.appendText("\nBonus Card Invalid" + "\n" + "Payment card number: " + jsonData.get("paymentCardNumber") + "\n" + "Payment state: " + jsonData.get("paymentState") + "\n" + "Payment card type: " + jsonData.get("paymentCardType") + "\n" + "===============\n");
                this.handlePayment(jsonData, "payment");
            }
            return;
        }

        if (jsonData.has("bonusCardNumber")) {
            // Check if valid bonus customer
            if (this.bonusCardIsValid(jsonData)) {
                this.cashierMessageCenter.setText("\nBonus card number: " + jsonData.get("bonusCardNumber") + "\n" + "Bonus card state : " + jsonData.get("bonusState") + "\n" + "Good trough month: " + jsonData.get("goodThruMonth") + "\n" + "Good trough year: " + jsonData.get("goodThruYear") + "\n" + "===============\n");
                if (this.hasNotReceivedBonus) this.updateBonusCustomerPrice();
                this.handlePayment(jsonData, "bonus");
            } else this.cashierMessageCenter.setText("Invalid Bonus Card!");

            return;
        }

        if (jsonData.has("paymentCardNumber")) {
            this.cashierMessageCenter.setText("\nPayment card number: " + jsonData.get("paymentCardNumber") + "\n" + "Payment state: " + jsonData.get("paymentState") + "\n" + "Payment card type: " + jsonData.get("paymentCardType") + "\n" + "===============\n");
            this.handlePayment(jsonData, "payment");
        }
    }

    /**
     * Checks if any of the products have a special bonus customer price.
     * Set hasRecieved bonus to true, so the same discount can't be applied many times.
     */
    private void updateBonusCustomerPrice () {

        // UPDATE CART
        for (Object obj : this.cartTable.getItems()) {
            Product p = (Product) obj;
            // Check if product price is available for bonus customers only
            if (p.getBonusOnlyDiscount()) {
                float oldPrice = p.getPrice();
                float bonusPrice = p.getDiscountedPrice(this.bonusCustomer);
                float difference = oldPrice - bonusPrice;
                p.setPrice(bonusPrice);
                this.sum -= difference;
                this.sum = Float.parseFloat(this.decimalFormat.format(this.sum));
                this.cartSum.setText(Float.toString(this.sum));
            }
        }
        // UPDATE PRODUCT CATALOG
        for (Object obj : this.productTable.getItems()) {
            Product p = (Product) obj;
            // Check if product price is available for bonus customers only
            if (p.getBonusOnlyDiscount()) {
                float bonusPrice = p.getDiscountedPrice(this.bonusCustomer);
                p.setPrice(bonusPrice);
            }
        }

        this.hasNotReceivedBonus = false;
        this.cartTable.refresh();
        this.productTable.refresh();
    }


    /**
     * Validates the bonus card
     *
     * @param jsonData json object containing bonus card data
     * @return true or false
     */
    private boolean bonusCardIsValid (JSONObject jsonData) {
        try {
            Long cardNumber = jsonData.getLong("bonusCardNumber");
            int goodThruYear = jsonData.getInt("goodThruYear");
            int goodThruMonth = jsonData.getInt("goodThruMonth");
            goodThruYear -= 2000; // Backend needs year in just two digits

            this.bonusCustomer = this.customerRegister.getCustomerByCard(cardNumber, goodThruYear, goodThruMonth);

            this.greetLabel.setText("Bonus Customer: " + this.bonusCustomer.getName());
            return true;
        } catch (NoSuchCustomerException e) {
            return false;
        }
    }


    /**
     * Does the payment logic and checks if there was a bonuscard involved in the payment
     *
     * @param jsondata    JSON object
     * @param paymentType payment card / bonus / combined
     */
    private void handlePayment (JSONObject jsondata, String paymentType) {
        switch (paymentType) {
            case "combined":
            case "payment":
                if (jsondata
                        .getString("paymentState")
                        .equals("ACCEPTED")) {
                    this.cashierMessageCenter.appendText("\nPAYMENT ACCEPTED!");
                    float payedSum = Float.parseFloat(this.cardReaderSum.getText());
                    this.paymentSuccess(payedSum);
                }
                break;
            case "bonus":
                this.cashierMessageCenter.appendText("\nVALID BONUS CARD!");
                this.onResetCardReader();
                break;
            default:
                System.out.println("Unsupported type");
        }
    }


    /**
     * Logic if we have a successful payment.
     *
     * @param payedSum amount the customer payed
     */
    private void paymentSuccess (float payedSum) {
        // Check if sum is negative and show how much money the customer gets back
        if ((this.sum - payedSum) < 0) {
            float returnSum = payedSum - this.sum;
            this.sum = 0;
            this.cashierMessageCenter.appendText("\nCUSTOMER RETURN CHANGE: " + returnSum);
            this.setShowFinishSaleBtns();
        } else if (this.sum - payedSum == 0) {
            this.setShowFinishSaleBtns();
            this.sum = 0;
        } else this.sum -= payedSum;
        this.cartSum.setText(Float.toString(this.sum));
        // Update Customer View as well
        this.customerViewController.setCartTotalSum(this.cartSum.getText());
        this.onResetCardReader();
    }


    /**
     * If the cart sum is 0, we see it as a completed sale and show the buttons for printing a receipt
     * and finishing the sale
     */
    private void setShowFinishSaleBtns () {
        this.finishSaleBtn.setVisible(true);
        this.printReceiptBtn.setVisible(true);
    }


    /**
     * Posts a reset call to the card reader
     */
    @FXML
    private void onResetCardReader () {
        this.httpController = new HttpController("POST", "http://localhost:9002/cardreader/reset");
        this.httpController.sendRequest();
        this.cashierMessageCenter.appendText("\nCard Reader reset.");
    }
}