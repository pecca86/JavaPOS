package backend;

import backend.structures.Transaction;
import org.json.JSONArray;
import org.json.JSONObject;
import sharedResources.exceptions.MissingDataException;
import sharedResources.exceptions.NoSuchCustomerException;
import sharedResources.productCatalog.Product;
import sharedResources.structures.Customer;
import sharedResources.structures.CustomerRegister;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class for keeping track of sales.
 * Aggregates the sales data for the marketing and sales departments
 */
class Analytics { //implements Serializable

    private static Analytics instance;

    private final ArrayList<Transaction> sales;
    private final CustomerRegister customers;

    private Analytics () {
        this.sales = new ArrayList<>();
        this.customers = CustomerRegister.getInstance();
    }

    /**
     * Get the analytics singleton
     *
     * @return The analytics instance
     */
    static Analytics getInstance () {
        if (instance == null) instance = new Analytics();
        return instance;
    }

    /**
     * @param data Sales data in JSON form
     * @throws MissingDataException Thrown if the customer ID is missing and a customer object was passed
     */
    void recordSale (JSONObject data) throws MissingDataException, NoSuchCustomerException {

        JSONArray sales = data.getJSONArray("sales");
        Customer customer = null;
        if (data.has("customer")) customer = CustomerRegister
                .getInstance()
                .getCustomer(data.getInt("customer"));

        Transaction transaction = Transaction.fromJson(sales);
        transaction.setCustomer(customer);

        this.sales.add(transaction);

    }

    /**
     * Get a list of all sales in JSON format
     *
     * @return A JSONObject containing the sales records
     */
    JSONObject getSales () {
        JSONArray sales = new JSONArray();
        for (Transaction sale : this.sales) sales.put(sale.toJson());
        return new JSONObject().put("sales", sales);
    }

    /**
     * Returns an object with individual numbers for each product sold mapped by days since epoch,
     * multiply with 60*60*24 to get a UNIX timestamp
     *
     * @return JSON object with the data
     */
    JSONObject getSalesByDate () {
        // HashMap<timestamp / (60*60*24) [days since epoch], Counter<barcode>>
        HashMap<Long, Counter<Integer>> grouper = new HashMap<>();
        for (Transaction transaction : this.sales) {
            long ts = transaction.getTimestamp();
            long day = ts / (60 * 60 * 24);
            if (!grouper.containsKey(day)) grouper.put(day, new Counter<>());
            for (Product item : transaction.getItems())
                grouper
                        .get(day)
                        .add(item.getBarCode());
        }
        JSONObject data = new JSONObject();
        for (Long day : grouper.keySet())
            data.put(day.toString(), grouper
                    .get(day)
                    .toJson());
        return data;
    }

    /**
     * Get a list of sales by customer ID
     *
     * @param customer Customer ID
     * @return A JSONObject containing the sales records
     * @throws NoSuchCustomerException No customer matching the ID
     */
    JSONObject getSales (int customer) throws NoSuchCustomerException {
        JSONArray sales = new JSONArray();
        Customer c = this.customers.getCustomer(customer);
        if (c == null)
            throw new NoSuchCustomerException(String.format("No customer by number %d", customer));
        for (Transaction sale : this.sales)
            if (sale.getCustomer() != null && sale
                    .getCustomer()
                    .getId() == customer) sales.put(sale.toJson());

        return new JSONObject()
                .put("customer", c.toJson())
                .put("sales", sales);
    }

    /**
     * Get the most popular items for a time range, mapped by their product IDs
     *
     * @param rangeStart UNIX timestamp for the start of the time range
     * @param rangeEnd   UNIX timestamp for the end of the time range
     * @return A JSONObject containing the number of sales mapped by product id
     */
    JSONObject getPopular (int rangeStart, int rangeEnd) {
        System.out.println("START: " + rangeStart + " - end: " + rangeEnd);
        Counter<Integer> counter = new Counter<>();
        this.sales.forEach((Transaction transaction) -> {
            if (transaction.getTimestamp() > rangeStart && transaction.getTimestamp() < rangeEnd)
                for (Product item : transaction.getItems())
                    counter.add(item.getBarCode());
        });
        return counter.toJson();
    }

    /**
     * @param rangeStart beginning of range from which to count
     * @param rangeEnd   end of range from which to count
     * @return JSON object with the data
     */
    JSONArray getPopularBonus (int rangeStart, int rangeEnd) throws NoSuchCustomerException {
        System.out.println("Calculating popular products among bonus customers");
        JSONArray total = new JSONArray();
        CustomerRegister customerRegister = CustomerRegister.getInstance();

        HashMap<Integer, Counter<Integer>> salesData = new HashMap<>();
        for (Transaction transaction : this.sales) {
            if (transaction.getTimestamp() < rangeStart && transaction.getTimestamp() > rangeEnd)
                continue;

            Customer customer = transaction.getCustomer();
            if (customer == null) continue;

            Counter<Integer> counter;
            if (salesData.containsKey(customer.getId()))
                counter = salesData.get(customer.getId());
            else {
                counter = new Counter<>();
                salesData.put(customer.getId(), counter);
            }
            for (Product item : transaction.getItems())
                counter.add(item.getBarCode());
        }

        salesData.forEach((Integer customerId, Counter<Integer> counter) -> {
            JSONObject data = new JSONObject();
            try {
                data.put("customer", customerRegister
                        .getCustomer(customerId)
                        .toJson());
            } catch (NoSuchCustomerException e) {
                // This should never happen as this class doesn't add the customer object to a transaction unless it's a valid customer
                e.printStackTrace();
            }
            data.put("sales", counter.toJson());
            total.put(data);
        });

        return total;
    }

}





