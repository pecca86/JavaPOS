package sharedResources.structures;

import org.json.JSONObject;
import org.json.XML;
import sharedResources.exceptions.NoSuchCustomerException;
import sharedResources.utils.HttpController;

public class CustomerRegister {

    private static CustomerRegister instance;

    private CustomerRegister () {}

    public static CustomerRegister getInstance () {
        if (instance == null) instance = new CustomerRegister();
        return instance;
    }

    /**
     * @param customerNo Customer number
     * @return Customer
     * @throws NoSuchCustomerException Customer not found
     */
    public Customer getCustomer (int customerNo) throws NoSuchCustomerException {
        HttpController http = new HttpController("GET", "http://localhost:9004/rest/findByCustomerNo/" + customerNo);
        http.sendRequest();

        if (http.getStatus() == 404)
            throw new NoSuchCustomerException("No customer by ID " + customerNo);
        else if (http.getStatus() >= 400)
            throw new RuntimeException(http.getResponse());
        String response = http.getResponse();
        JSONObject data = XML.toJSONObject(response);
        JSONObject customerData = data.getJSONObject("customer");
        return Customer.fromJson(customerData);

    }

    /**
     * @param cardNo Card number
     * @param year   Good through year
     * @param month  Good through month
     * @return Customer
     * @throws NoSuchCustomerException Customer not found
     */
    public Customer getCustomerByCard (Long cardNo, int year, int month) throws NoSuchCustomerException {
        HttpController http = new HttpController("GET", String.format("http://localhost:9004/rest/findByBonusCard/%d/%d/%d", cardNo, year, month));
        http.sendRequest();

        if (http.getStatus() == 404)
            throw new NoSuchCustomerException("No customer found for that card");
        String response = http.getResponse();
        JSONObject data = XML.toJSONObject(response);
        JSONObject customerData = data.getJSONObject("customer");
        System.out.println(customerData);
        return Customer.fromJson(customerData);

    }

}
