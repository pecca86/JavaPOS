/**
 * Class for displaying data in MarketingController.
 *
 * Represents a sold product, with variables for storing analytics.
 *
 */

package frontend.admin;

import java.util.ArrayList;
import java.util.List;

public class SoldProduct {

    private String name;
    private Integer amount;
    private Integer purchaseByMen;
    private Integer purchaseByWomen;
    private Integer purchaseByUnspec;
    private final List<Integer> ageGroup;
    private Integer barCode;


    public SoldProduct () {
        this.name = "";
        this.amount = 0;
        this.purchaseByMen = 0;
        this.purchaseByWomen = 0;
        this.purchaseByUnspec = 0;
        this.ageGroup = new ArrayList<>();
        this.ageGroup.add(Integer.MAX_VALUE);
        this.ageGroup.add(0);
    }

    public String getName () {
        return this.name;
    }

    public void setBarCode (Integer barCode) {this.barCode = barCode;}

    public Integer getBarCode () {return this.barCode;}

    public void setName (String inputName) {
        this.name = inputName;
    }

    public Integer getAmount () {
        return this.amount;
    }

    void addToAmount (Integer number) {
        this.amount = this.amount + number;
    }

    public Integer getPurchaseByMen () {
        return this.purchaseByMen;
    }

    public void addPurchaseByMen (Integer amount) {
        this.purchaseByMen = this.purchaseByMen + amount;
    }

    public Integer getPurchaseByWomen () {
        return this.purchaseByWomen;
    }

    public void addPurchaseByWomen (Integer amount) {
        this.purchaseByWomen = this.purchaseByWomen + amount;
    }

    public Integer getPurchaseByUnspec () {
        return this.purchaseByUnspec;
    }

    public void addPurchaseByUnspec (Integer amount) {
        this.purchaseByUnspec = this.purchaseByUnspec + amount;
    }

    public List<Integer> getAgeGroup () {
        return this.ageGroup;
    }

    public void addToAgeGroup (Integer age) {
        if (age < ageGroup.get(0)) {
            this.ageGroup.set(0, age);
        }
        if (age > ageGroup.get(1)) {
            this.ageGroup.set(1, age);
        }
    }
}
