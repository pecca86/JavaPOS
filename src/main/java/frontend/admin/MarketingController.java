/**
 * Class for displaying stored data in a tableView.
 *
 * User can view which products have been sold and how much per time period. User can also check which products bonus
 * customers have bought per time period, and also which products a certain bonus customer have bought.
 */

package frontend.admin;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import sharedResources.utils.HttpController;

import java.net.URL;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

public class MarketingController implements Initializable {

    // Time zone for date stuff
    private final ZoneId tz = ZoneId.systemDefault();

    // Controls
    @FXML
    private Button showPopular, showPopularBonus, getCustomer;

    // Input
    @FXML
    private DatePicker startDate, endDate;
    @FXML
    private TextField customer;

    // Output
    @FXML
    private TableView tableView;
    @FXML
    private Label outputTitle;

    @Override
    public void initialize (URL url, ResourceBundle resourceBundle) {
        this.tableView.setEditable(false);
        this.showPopular.setOnAction(ActionEvent -> this.popular(false));
        this.showPopularBonus.setOnAction(ActionEvent -> this.popular(true));
        this.getCustomer.setOnAction(ActionEvent -> this.customer());
    }

    /**
     * Displays all sales within a given time period in a tableView
     *
     * @param bonus true if displaying sales by bonus customers, false if displaying all sales
     */
    private void popular (boolean bonus) {
        LocalDate startDate = this.startDate.getValue();
        LocalDate endDate = this.endDate.getValue();
        if (startDate == null) {
            this.outputTitle.setText("Missing start date.");
            return;
        } else if (endDate == null) {
            this.outputTitle.setText("Missing end date");
            return;
        } else {
            this.outputTitle.setText("Fetching data...");
            this.outputTitle.setText(String.format("Popular items from %s to %s", startDate, endDate));
        }

        long start = startDate
                .atStartOfDay(this.tz)
                .toEpochSecond();
        long end = endDate
                .atStartOfDay(this.tz)
                .toEpochSecond();
        System.out.println(start);
        System.out.println(end);

        // Is executed when pushing popular (bonus) button
        if (bonus) {

            // Fetching data
            HttpController http = new HttpController("GET",
                    String.format("http://localhost:8080/api/popular/%d-%d/%s", start, end, bonus ? "bonus" : ""));
            http.sendRequest();
            JSONArray response = new JSONArray(http.getResponse());

            // Clear table
            this.tableView
                    .getItems()
                    .clear();
            this.tableView
                    .getColumns()
                    .clear();

            // Column for barcode
            TableColumn<SoldProduct, Integer> barcodeColumn = new TableColumn<>("Barcode");
            barcodeColumn.setCellValueFactory(
                    new PropertyValueFactory<SoldProduct, Integer>("barCode"));

            // Column for product
            TableColumn<SoldProduct, String> productColumn = new TableColumn<>("Product");
            productColumn.setCellValueFactory(
                    new PropertyValueFactory<SoldProduct, String>("name"));

            // Column for amount sold
            TableColumn<SoldProduct, Integer> amountColumn = new TableColumn<SoldProduct, Integer>("Amount");
            amountColumn.setCellValueFactory(
                    new PropertyValueFactory<SoldProduct, Integer>("amount"));

            // Column for age group
            TableColumn<SoldProduct, List<Integer>> ageColumn = new TableColumn<SoldProduct, List<Integer>>("Age Group");
            ageColumn.setCellValueFactory(
                    new PropertyValueFactory<SoldProduct, List<Integer>>("ageGroup"));

            // Columns for gender distribution
            TableColumn<SoldProduct, Integer> menColumn = new TableColumn<SoldProduct, Integer>("Amount purchased by men");
            menColumn.setCellValueFactory(
                    new PropertyValueFactory<SoldProduct, Integer>("purchaseByMen"));

            TableColumn<SoldProduct, Integer> womenColumn = new TableColumn<SoldProduct, Integer>("Amount purchased by women");
            womenColumn.setCellValueFactory(
                    new PropertyValueFactory<SoldProduct, Integer>("purchaseByWomen"));

            TableColumn<SoldProduct, Integer> unspecColumn = new TableColumn<SoldProduct, Integer>("Amount purchased by unspecified gender");
            unspecColumn.setCellValueFactory(
                    new PropertyValueFactory<SoldProduct, Integer>("purchaseByUnspec"));

            // Add columns to table
            this.tableView
                    .getColumns()
                    .add(barcodeColumn);
            this.tableView
                    .getColumns()
                    .add(productColumn);
            this.tableView
                    .getColumns()
                    .add(amountColumn);
            this.tableView
                    .getColumns()
                    .add(ageColumn);
            this.tableView
                    .getColumns()
                    .add(menColumn);
            this.tableView
                    .getColumns()
                    .add(womenColumn);
            this.tableView
                    .getColumns()
                    .add(unspecColumn);

            // Keep track of type of products sold
            HashMap<String, SoldProduct> soldProducts = new HashMap<>();

            // Check that sales isn't null
            if (response == null) {
                this.outputTitle.setText("There's no sales to view");
                return;
            }

            // Go through JSONObjects in the array
            for(int i = 0; i < response.length(); i++) {
                JSONObject object = response.getJSONObject(i);
                String[] sales = JSONObject.getNames(object.getJSONObject("sales"));
                JSONObject customer = object.getJSONObject("customer");

                for (String barcode : sales) {

                    // If the product already doesn't exist in table, create a new SoldProduct
                    if (!soldProducts.containsKey(barcode)) {
                        http.setUrl(String.format("http://localhost:8080/api/productcatalog/barcode/%s", barcode));
                        http.sendRequest();
                        String name = (new JSONObject(http.getResponse())).getString("name");
                        SoldProduct soldProduct = new SoldProduct();
                        soldProduct.setName(name);
                        soldProduct.setBarCode(Integer.parseInt(barcode));
                        soldProducts.put(barcode, soldProduct);
                    }

                    // Add amount sold
                    int amount = object.getJSONObject("sales").getInt(barcode);
                    soldProducts.get(barcode).addToAmount(amount);

                    // Add gender distribution
                    String sex = customer.getString("sex");
                    if (sex.equals("MALE")) {
                        soldProducts.get(barcode).addPurchaseByMen(amount);
                    } else if (sex.equals("FEMALE")) {
                        soldProducts.get(barcode).addPurchaseByWomen(amount);
                    } else {
                        soldProducts.get(barcode).addPurchaseByUnspec(amount);
                    }

                    // Add age
                    String birthdateJson = customer.getString("birthDate");
                    String birthdate = birthdateJson.substring(0, 10);
                    int age = Period.between(LocalDate.parse(birthdate), java.time.LocalDate.now()).getYears();
                    soldProducts.get(barcode).addToAgeGroup(age);

                }

            }

            // Add all products to table
            for (SoldProduct item : soldProducts.values()) {
                this.tableView
                        .getItems()
                        .add(item);
            }

        }

        // Is executed when pushing popular button
        else {

            // Fetching data
            HttpController http = new HttpController("GET",
                    String.format("http://localhost:8080/api/popular/%d-%d/%s", start, end, bonus ? "bonus" : ""));
            http.sendRequest();
            JSONObject response = new JSONObject(http.getResponse());

            // Clear table
            this.tableView
                    .getItems()
                    .clear();
            this.tableView
                    .getColumns()
                    .clear();

            // Column for barcode
            TableColumn<SoldProduct, Integer> barcodeColumn = new TableColumn<>("Barcode");
            barcodeColumn.setCellValueFactory(
                    new PropertyValueFactory<SoldProduct, Integer>("barCode"));

            // Column for product
            TableColumn<SoldProduct, String> productColumn = new TableColumn<>("Product");
            productColumn.setCellValueFactory(
                    new PropertyValueFactory<SoldProduct, String>("name"));

            // Column for amount sold
            TableColumn<SoldProduct, Integer> amountColumn = new TableColumn<SoldProduct, Integer>("Amount");
            amountColumn.setCellValueFactory(
                    new PropertyValueFactory<SoldProduct, Integer>("amount"));

            // Add columns to table
            this.tableView
                    .getColumns()
                    .add(barcodeColumn);
            this.tableView
                    .getColumns()
                    .add(productColumn);
            this.tableView
                    .getColumns()
                    .add(amountColumn);

            // Array with barcodes for products sold
            String[] barcodes = JSONObject.getNames(response);

            // Check that barcodes isn't null
            if (barcodes == null) {
                this.outputTitle.setText("There's no sales to view");
                return;
            }

            // Add all products in array to table
            for (String barcode : barcodes) {
                http.setUrl(String.format("http://localhost:8080/api/productcatalog/barcode/%s", barcode));
                http.sendRequest();
                String name = (new JSONObject(http.getResponse())).getString("name");

                // Create new SoldProduct
                SoldProduct soldProduct = new SoldProduct();
                soldProduct.setName(name);
                soldProduct.setBarCode(Integer.parseInt(barcode));
                soldProduct.addToAmount(response.getInt(barcode));

                // Add to table
                this.tableView
                        .getItems()
                        .add(soldProduct);
            }

        }

    }

    /**
     * Display all sales by a given customer
     *
     */
    private void customer () {

        // Get customer by input
        String customerInput = this.customer.getText();
        int customerId;
        try {
            customerId = Integer.parseInt(customerInput);
        } catch (NumberFormatException ex) {
            this.outputTitle.setText("Invalid customer ID supplied, must be integer.");
            return;
        }

        // Fetching data
        HttpController http = new HttpController("GET",
                String.format("http://localhost:8080/api/sales/%d", customerId));
        http.sendRequest();
        String response = http.getResponse();

        // Clear table
        this.tableView
                .getItems()
                .clear();
        this.tableView
                .getColumns()
                .clear();

        // Column for barcode
        TableColumn<SoldProduct, Integer> barcodeColumn = new TableColumn<SoldProduct, Integer>("Barcode");
        barcodeColumn.setCellValueFactory(
                new PropertyValueFactory<SoldProduct, Integer>("barCode"));

        // Column for product
        TableColumn<SoldProduct, String> productColumn = new TableColumn<SoldProduct, String>("Product");
        productColumn.setCellValueFactory(
                new PropertyValueFactory<SoldProduct, String>("name"));

        // Column for amount sold
        TableColumn<SoldProduct, Integer> amountColumn = new TableColumn<SoldProduct, Integer>("Amount");
        amountColumn.setCellValueFactory(
                new PropertyValueFactory<SoldProduct, Integer>("amount"));

        // Add columns to table
        this.tableView
                .getColumns()
                .add(barcodeColumn);
        this.tableView
                .getColumns()
                .add(productColumn);
        this.tableView
                .getColumns()
                .add(amountColumn);

        JSONObject json = new JSONObject(response);
        JSONObject customer = json.getJSONObject("customer");
        JSONArray sales = json.getJSONArray("sales");
        this.outputTitle.setText(String.format("Sales to customer %s %s (%d)",
                customer.optString("firstName", "N/A"),
                customer.optString("lastName", "N/A"),
                customer.getInt("customerNo")));

        // Keep track of type of products sold
        HashMap<String, SoldProduct> soldProducts = new HashMap<>();

        // Check that sales isn't empty
        if (sales.isEmpty()) {
            this.outputTitle.setText("There's no sales to view");
            return;
        }

        // Go through all sales by that customer
        for (int i = 0; i < sales.length(); i++) {

            JSONObject sale = sales.getJSONObject(i);
            JSONArray items = sale.getJSONArray("items");

            // Go through items in that sale
            for (int j = 0; j < items.length(); j++) {

                JSONObject item = items.getJSONObject(j);
                String barcode = Integer.toString(item.getInt("barCode"));

                // If the product already doesn't exist in table, create a new SoldProduct
                if (!soldProducts.containsKey(barcode)) {
                    http.setUrl(String.format("http://localhost:8080/api/productcatalog/barcode/%s", barcode));
                    http.sendRequest();
                    String name = (new JSONObject(http.getResponse())).getString("name");
                    SoldProduct soldProduct = new SoldProduct();
                    soldProduct.setName(name);
                    soldProduct.setBarCode(Integer.parseInt(barcode));
                    soldProducts.put(barcode, soldProduct);
                }

                // Add to amount
                soldProducts.get(barcode).addToAmount(1);

            }

        }

        // Add all products to table
        for (SoldProduct item : soldProducts.values()) {
            this.tableView
                    .getItems()
                    .add(item);
        }

    }

}
