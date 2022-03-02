/**
 * Responsible for the logic inside the sales view.
 * Retrieves product data from the backend and sets new product data to the backend.
 */
package frontend.admin;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sharedResources.productCatalog.Product;
import sharedResources.productCatalog.ProductCatalog;
import sharedResources.utils.HttpController;

import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

/**
 * Controller
 */
public class SalesController implements Initializable {

    private final ZoneId tz = ZoneId.systemDefault();

    @FXML
    private ChoiceBox keywordSelector = new ChoiceBox();
    private final List<String> keywords = new ArrayList<>();
    private final Set<String> keywordSet = new HashSet<>();

    @FXML
    private Button addDiscountButton, changePriceButton, salesDataBtn, removeDiscountBtn;
    @FXML
    private DatePicker datePickerStart, datePickerEnd, salesDataDatePicker;
    @FXML
    private CheckBox bonusCustomersCheck, selectAllCheck, discountByKeywordCheck;
    @FXML
    private TextField discountField, priceField;

    @FXML
    private TableView productTable, discountProductsTable, soldProductsTable;

    private final ProductCatalog productCatalog = ProductCatalog.getInstance();
    private final ArrayList<Product> productList = new ArrayList();
    private final Set<Integer> discountedList = new HashSet<>();
    private final HashMap<Integer, Product> discounteds = new HashMap<>();

    /**
     * Initializes the view
     *
     * @param url
     * @param resourceBundle
     */
    @Override
    public void initialize (URL url, ResourceBundle resourceBundle) {

        // INIT THE TABLES
        this.createProducts();
        this.getProducts();
        this.createProductTable();
        this.updateProductTable();
        this.createDiscountProductsTable();
        this.updateDiscountTable();
        this.createSoldProductsTable();
        this.setKeywords();

        // ADD LISTENERS
        this.addListeners();
    }


    /**
     * Sets keyword to our dropdown selector
     */
    private void setKeywords () {
        this.productList.forEach(prod -> {
            this.keywordSet.add(prod.getKeyword());
        });

        for (Object kw : this.keywordSet.toArray())
            this.keywordSelector
                    .getItems()
                    .add(kw);
    }

    /**
     * Fetches available products from the backend and puts them into our product list
     */
    private void getProducts () {
        this.productList.clear();
        HttpController http = new HttpController("GET", "http://localhost:8080/api/productcatalog");
        http.sendRequest();
        JSONArray jsonArr = new JSONArray(http.getResponse());
        jsonArr.forEach(k -> {
            Product p = Product.fromJson((JSONObject) k);
            this.productList.add(p);
        });
        //this.products = this.productCatalog.getAllProducts();
    }

    /**
     * Fetches available products from the backend and puts them into our productCatalog instance
     */
    private void createProducts () {
        try {
            HttpController http = new HttpController("GET", "http://localhost:8080/api/productcatalog");
            http.sendRequest();
            String response = http.getResponse();

            JSONArray array = new JSONArray(response);
            for (Object entry : array) {
                JSONObject productData = (JSONObject) entry;
                Product product = Product.fromJson(productData);
                this.productCatalog.addProduct(product);
            }

        } catch (JSONException e) {
            System.out.println("Failed parsing the JSON @SalesController");
            e.printStackTrace();
        }
    }

    /**
     * Creates the product TableView element
     */
    private void createProductTable () {
        this.productTable.setPlaceholder(new Label("Product Table is empty"));
        TableColumn<Product, String> nameColumn = new TableColumn<>("Name");
        // This needs to match the object's property name!
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Product, Float> priceColumn = new TableColumn<>("Price");
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));

        TableColumn<Product, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("productId"));

        TableColumn<Product, Integer> barCodeColumn = new TableColumn<>("Bar code");
        barCodeColumn.setCellValueFactory(new PropertyValueFactory<>("barCode"));

        TableColumn<Product, String> keywordColumn = new TableColumn<>("Keyword");
        keywordColumn.setCellValueFactory(new PropertyValueFactory<>("keyword"));

        this.productTable
                .getColumns()
                .add(nameColumn);

        this.productTable
                .getColumns()
                .add(priceColumn);

        this.productTable
                .getColumns()
                .add(idColumn);

        this.productTable
                .getColumns()
                .add(barCodeColumn);

        this.productTable
                .getColumns()
                .add(keywordColumn);

    }

    /**
     * Creates the table where discounted products are shown
     */
    private void createDiscountProductsTable () {
        this.discountProductsTable.setPlaceholder(new Label("No discounted Products yet."));

        // Create table columns
        TableColumn<Product, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Product, Float> priceColumn = new TableColumn<>("Price");
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));

        TableColumn<Product, Integer> idColumn = new TableColumn<>("Discount");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("discount"));

        TableColumn<Product, Integer> barCodeColumn = new TableColumn<>("Bar code");
        barCodeColumn.setCellValueFactory(new PropertyValueFactory<>("barCode"));

        TableColumn<Product, String> keywordColumn = new TableColumn<>("Keyword");
        keywordColumn.setCellValueFactory(new PropertyValueFactory<>("keyword"));

        TableColumn<Product, String> startDateColumn = new TableColumn<>("Start Date");
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("discountFrom"));

        TableColumn<Product, String> endDateColumn = new TableColumn<>("End Date");
        endDateColumn.setCellValueFactory(new PropertyValueFactory<>("discountUntil"));

        TableColumn<Product, String> bonusOnlyColumn = new TableColumn<>("Bonus only");
        bonusOnlyColumn.setCellValueFactory(new PropertyValueFactory<>("bonusOnlyDiscount"));

        this.discountProductsTable
                .getColumns()
                .add(nameColumn);

        this.discountProductsTable
                .getColumns()
                .add(priceColumn);

        this.discountProductsTable
                .getColumns()
                .add(idColumn);

        this.discountProductsTable
                .getColumns()
                .add(barCodeColumn);

        this.discountProductsTable
                .getColumns()
                .add(keywordColumn);

        this.discountProductsTable
                .getColumns()
                .add(startDateColumn);

        this.discountProductsTable
                .getColumns()
                .add(endDateColumn);

        this.discountProductsTable
                .getColumns()
                .add(bonusOnlyColumn);

    }

    /**
     * Creates the table where sold products are shown
     */
    private void createSoldProductsTable () {
        this.soldProductsTable.setPlaceholder(new Label("Set Date for fetching Sales Data..."));

        // Create table columns
        TableColumn<Product, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Product, Integer> barCodeColumn = new TableColumn<>("Bar code");
        barCodeColumn.setCellValueFactory(new PropertyValueFactory<>("barCode"));

        TableColumn<SoldProduct, Integer> amountColumn = new TableColumn<SoldProduct, Integer>("Amount");
        amountColumn.setCellValueFactory(new PropertyValueFactory<SoldProduct, Integer>("amount"));

        this.soldProductsTable
                .getColumns()
                .add(nameColumn);

        this.soldProductsTable
                .getColumns()
                .add(barCodeColumn);

        this.soldProductsTable
                .getColumns()
                .add(amountColumn);
    }


    /**
     * Updates the product table
     */
    private void updateProductTable () {
        this.productTable
                .getItems()
                .clear();

        this.productList.forEach(product -> {
            this.productTable
                    .getItems()
                    .add(product);
        });
    }


    /**
     * Updates the discount table. Gets the products and put each unique product inside a hashmap.
     */
    private void updateDiscountTable () {
        // Clears out existing discounted products
        this.discounteds.clear();

        //
        this.discountProductsTable.getItems().clear();

        // Fetches the products from the backend
        this.getProducts();

        // Checks if newly fetched products have a discount gt 0
        this.productList.forEach(product -> {
            if (product.getDiscount() > 0)
                this.discounteds.put(product.getBarCode(), product);
        });

        // Adds the discounted products to the table
        this.discounteds.forEach((k, v) -> {
            this.discountProductsTable
                    .getItems()
                    .add(v);
        });
    }

    /**
     * Updates the sold products table
     */
    private void updateSoldProductsTable () {
        this.soldProductsTable
                .getItems()
                .clear();
        this.getSalesData();
    }


    /**
     * Gets sales data from our backend and pushes it to our sold products table.
     */
    private void getSalesData () {
        try {
            LocalDate startDate = this.salesDataDatePicker.getValue();
            LocalDate endDate = startDate.plusDays(1);
            long start = startDate
                    .atStartOfDay(this.tz)
                    .toEpochSecond();
            long end = endDate
                    .atStartOfDay(this.tz)
                    .toEpochSecond();

            HttpController http = new HttpController("GET", String.format("http://localhost:8080/api/popular/%d-%d", start, end));
            http.sendRequest();
            JSONObject res = new JSONObject(http.getResponse());

            for (String barcode : JSONObject.getNames(res)) {
                http.setUrl(String.format("http://localhost:8080/api/productcatalog/barcode/%s", barcode));
                http.sendRequest();
                JSONObject barCodeRes = new JSONObject(http.getResponse());
                //            String name = (new JSONObject(http.getResponse())).getString("name");
                String name = barCodeRes.getString("name");
                Integer barCode = barCodeRes.getInt("barCode");
                SoldProduct soldProduct = new SoldProduct();
                soldProduct.setName(name);
                soldProduct.setBarCode(barCode);
                soldProduct.addToAmount(res.getInt(barcode));
                this.soldProductsTable
                        .getItems()
                        .add(soldProduct);
            }
        } catch (NullPointerException e) {
            this.soldProductsTable.setPlaceholder(new Label("No Sales for given date"));
        }
    }


    /**
     * Centralized place for adding listeners.
     */
    private void addListeners () {
        this.priceChangeListener();
        this.specialOfferListener();
        this.getSalesDataListener();
        this.removeDiscountListener();
    }

    /**
     * Listens on if remove discount btn has been clicked.
     * If clicked, selected item's discount is set to 0 and discount table is updated.
     */
    private void removeDiscountListener() {
        this.removeDiscountBtn.setOnMouseClicked(e -> {
            this.removeProductDiscount();
        });
    }


    /**
     * Listens on if salesDataBtn has been clicked.
     */
    private void getSalesDataListener () {
        this.salesDataBtn.setOnMouseClicked(e -> {
            this.updateSoldProductsTable();
        });
    }

    /**
     * Listens on if changePriceButton has been clicked.
     */
    private void priceChangeListener () {
        this.changePriceButton.setOnMouseClicked(e -> {
            this.onChangeProductPrice();
        });
    }

    /**
     * Listens on if addDiscountButton has been clicked.
     */
    private void specialOfferListener () {
        this.addDiscountButton.setOnMouseClicked(e -> {
            this.onSetSpecialOffer();
        });
    }


    /**
     * Send a request to the backend to set the discount for the selected product to 0
     */
    private void removeProductDiscount() {
        Product p = (Product) this.discountProductsTable.getSelectionModel().getSelectedItem();
        if (p == null ) return;
        JSONArray jsonArr = new JSONArray();
        jsonArr.put(p.getBarCode());
        JSONObject data = new JSONObject();
        data.put("discount", 0);
        data.put("from", 0);
        data.put("until", 0);
        data.put("products", jsonArr);
        data.put("bonusOnly", false);

        HttpController http = new HttpController("POST", "http://localhost:8080/api/product/discount", "application/json", data.toString());
        http.sendRequest();
        this.discountProductsTable.getItems().remove(this.discountProductsTable.getSelectionModel().getSelectedItem());
        //this.discountProductsTable.refresh();
        this.updateDiscountTable();
    }

    /**
     * Handles logic on how discounted products are being handled and displayed.
     */
    private void onSetSpecialOffer () {
        try {
            // Gets the date from the date fields and converts them to epoch time for our backend
            long startDate = dateToEpochSecond(this.datePickerStart.getValue());
            System.out.println(startDate);
            long endDate = dateToEpochSecond(this.datePickerEnd.getValue());
            // Gets the value from our discount field and parses it to just two decimals
            float discount = Float.parseFloat(this.discountField.getText()) / 100;

            // If user has checked to discount products by keyword
            if (this.discountByKeywordCheck.isSelected()) {
                if (this.checkIfUserWantsToUpdate()) {
                    JSONObject data = new JSONObject();
                    data.put("discount", discount);
                    data.put("from", startDate);
                    data.put("until", endDate);
                    data.put("keyword", this.keywordSelector.getValue());
                    data.put("bonusOnly", this.bonusCustomersCheck.isSelected());
                    HttpController http = new HttpController("POST", "http://localhost:8080/api/product/discount", "application/json", data.toString());
                    http.sendRequest();
                    this.getProducts();
                    this.updateDiscountTable();
                }
            } else {
                Product p = (Product) this.productTable
                        .getSelectionModel()
                        .getSelectedItem();
                if (p == null) return;

                if (this.CheckIfUserWantsToUpdateProduct(p)) {
                    JSONArray jsonArr = new JSONArray();
                    jsonArr.put(p.getBarCode());

                    JSONObject data = new JSONObject();
                    data.put("discount", discount);
                    data.put("from", startDate);
                    data.put("until", endDate);
                    data.put("products", jsonArr);
                    data.put("bonusOnly", this.bonusCustomersCheck.isSelected());

                    HttpController http = new HttpController("POST", "http://localhost:8080/api/product/discount", "application/json", data.toString());
                    http.sendRequest();
                    this.getProducts();
                    this.updateDiscountTable();
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Not a valid number!");
        } catch (NullPointerException e) {
            System.out.println("Check if all required forms are filled!");
            e.printStackTrace();
        }
    }


    /**
     * Checks if the product already has a discount set to it and prompts if the user
     * want's to update the discount information
     *
     * @param p Product to discount
     * @return true if user wants to update, else false
     */
    private boolean CheckIfUserWantsToUpdateProduct (Product p) {
        if (this.discounteds.containsKey(p.getBarCode())) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Set Discount");
            alert.setHeaderText(null);
            alert.setContentText("Are you sure you want to override existing discounts?");

            ButtonType buttonTypeYes = new ButtonType("Yes", ButtonBar.ButtonData.OK_DONE);
            ButtonType buttonTypeCancel = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);

            alert
                    .getButtonTypes()
                    .setAll(buttonTypeYes, buttonTypeCancel);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == buttonTypeYes) return true;
            else if (result.get() == buttonTypeCancel) return false;
        }
        return true;
    }

    /**
     * Checks if products with the given keyword already are discounted.
     *
     * @return true if there are products with the selected keyword already discounted.
     */
    private boolean checkIfAlreadyDiscounted () {
        boolean containsDiscountedProducts = false;
        for (Product p : this.productList)
            if ((p
                    .getKeyword()
                    .equals(this.keywordSelector.getValue())) && p.getDiscount() > 0) {
                containsDiscountedProducts = true;
                break;
            }
        return containsDiscountedProducts;
    }

    /**
     * Prompts user to either update existing discounts or not
     *
     * @return true if user wants to update
     */
    private boolean checkIfUserWantsToUpdate () {
        if (this.checkIfAlreadyDiscounted()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Set Discount");
            alert.setHeaderText(null);
            alert.setContentText("Are you sure you want to override existing discounts?");

            ButtonType buttonTypeYes = new ButtonType("Yes", ButtonBar.ButtonData.OK_DONE);
            ButtonType buttonTypeCancel = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);

            alert
                    .getButtonTypes()
                    .setAll(buttonTypeYes, buttonTypeCancel);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == buttonTypeYes) return true;
            else if (result.get() == buttonTypeCancel) return false;
        }
        return true;
    }

    /**
     * Converts LocalDate to epoch second
     *
     * @param date Date to convert.
     * @return date in epoch seconds.
     */
    private static long dateToEpochSecond (LocalDate date) {
        return date
                .atStartOfDay(ZoneId.systemDefault())
                .toEpochSecond();
    }

    /**
     * Changes to product's price to the price set by the user
     */
    private void onChangeProductPrice () {
        try {
            float newPrice = Float.parseFloat(this.priceField.getText());
            Product p = (Product) this.productTable
                    .getSelectionModel()
                    .getSelectedItem();
            this.setProductPrice(p, newPrice);
            this.productTable.refresh();

        } catch (NumberFormatException e) {
            System.out.println("Not a number!");
        }
    }

    /**
     * Sends the new price to the backend, so the price is universally updated.
     *
     * @param p        Product which price we want to update
     * @param newPrice Product's new price
     */
    private void setProductPrice (Product p, float newPrice) {
        String url = String.format("http://localhost:8080/api/product/%s/price", p.getBarCode());
        String data = "{price:" + newPrice + "}";
        HttpController http = new HttpController("POST", url, "application/json", data);
        http.sendRequest();

        this.getProducts();
        this.updateProductTable();
    }
}
