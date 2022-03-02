package sharedResources.structures;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sharedResources.exceptions.NoSuchCustomerException;

class CustomerTest {

    private Customer bonusCustomer, customer;

    @BeforeEach
    void setUp () throws NoSuchCustomerException {
        CustomerRegister register = CustomerRegister.getInstance();
        this.bonusCustomer = register.getCustomer(1); // Should be changed to the ID of an active bonus customer for proper testing
        this.customer = register.getCustomer(53); // Should be the ID of a non-bonus customer

    }

    @Test
    void fromJsonToJson () {
        JSONObject customerData = this.customer.toJson();
        Customer customerClone = Customer.fromJson(customerData);

        assert customerClone
                .getName()
                .equals(this.customer.getName());
        assert customerClone.activeBonus() == this.customer.activeBonus();
        assert customerClone.getId() == this.customer.getId();
    }

    @Test
    void activeBonus () {
        assert !this.customer.activeBonus();
        assert this.bonusCustomer.activeBonus();
    }
}