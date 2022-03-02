package sharedResources.structures;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Represents a customer from the customer database
 */
public class Customer {
    private String firstName;
    private String lastName;
    private String sex;
    private String birthDate;
    private final int customerNo;
    private Address address;
    private final HashMap<Long, BonusCard> bonusCards;

    /**
     * Represents a customer within the system
     *
     * @param id The numeric customer ID
     */
    private Customer (int id) {
        this.customerNo = id;
        this.bonusCards = new HashMap<>();
    }

    /**
     * @param data Create a customer object from raw JSON data
     * @return The customer instance
     */
    static Customer fromJson (JSONObject data) {
        Customer customer = new Customer(data.getInt("customerNo"))
                .setName(data.optString("firstName"), data.optString("lastName"))
                .setSex(data.optString("sex"))
                .setBirthDate(data.optString("birthDate"))
                .setAddress(data.getJSONObject("address"));

        if (data.has("bonusCard")) {
            JSONArray cards = data.optJSONArray("bonusCard");
            if (cards == null)
                cards = new JSONArray().put(data.getJSONObject("bonusCard"));
            for (Object cardDataRaw : cards)
                customer.addBonusCard((JSONObject) cardDataRaw);
        }
        return customer;
    }

    private void addBonusCard (JSONObject data) {
        BonusCard card = new BonusCard(data.getLong("number"), data.getInt("goodThruYear"), data.getInt("goodThruMonth"), data.getString("holderName"));
        card.setBlocked(data.getBoolean("blocked"));
        card.setExpired(data.getBoolean("expired"));
        this.bonusCards.put(card.getNumber(), card);
    }

    public boolean activeBonus () {
        boolean value = false;
        for (BonusCard card : this.bonusCards.values())
            if (!card.isBlocked() && !card.isExpired()) value = true;
        return value;
    }

    public int getId () {
        return this.customerNo;
    }

    private Customer setName (String first, String last) {
        this.firstName = first;
        this.lastName = last;
        return this;
    }

    public String getName () {
        return this.firstName + " " + this.lastName;
    }

    private Customer setSex (String sex) {
        this.sex = sex;
        return this;
    }

    public String getSex () {
        return this.sex;
    }

    private Customer setBirthDate (String birthDate) {
        this.birthDate = birthDate;
        return this;
    }

    private String getBirthDate () {
        return this.birthDate;
    }

    private Customer setAddress (JSONObject data) {
        this.address = new Address(data);
        return this;
    }

    private Address getAddress () {
        return this.address;
    }

    /**
     * @return The JSON representation of the customer
     */
    public JSONObject toJson () {
        JSONObject customer = new JSONObject();
        customer.put("customerNo", this.customerNo);
        customer.put("firstName", this.firstName);
        customer.put("lastName", this.lastName);
        customer.put("sex", this.sex);
        customer.put("birthDate", this.birthDate);
        customer.put("address", this.address.toJson());
        JSONArray bonusCards = new JSONArray();
        for (BonusCard card : this.bonusCards.values())
            bonusCards.put(card.toJson());
        customer.put("bonusCards", bonusCards);
        return customer;
    }

    @Override
    public String toString () {
        return String.format("""
                CUSTOMER
                ID: %d
                Name: %s
                Birth date: %s
                Sex: %s""", this.customerNo, this.firstName + " " + this.lastName, this.birthDate, this.sex);
    }

    private class BonusCard {

        private final long number;
        private final int year;
        private final int month;
        private final String holderName;
        private boolean expired;
        private boolean blocked;

        BonusCard (long number, int year, int month, String holder) {
            this.number = number;
            this.year = year;
            this.month = month;
            this.holderName = holder;
        }

        boolean isExpired () {
            return this.expired;
        }

        boolean isBlocked () {
            return this.blocked;
        }

        void setBlocked (boolean blocked) {
            this.blocked = blocked;
        }

        void setExpired (boolean expired) {
            this.expired = expired;
        }

        long getNumber () {
            return this.number;
        }

        JSONObject toJson () {
            JSONObject card = new JSONObject();
            card.put("number", this.number);
            card.put("goodThruYear", this.year);
            card.put("goodThruMonth", this.month);
            card.put("holderName", this.holderName);
            card.put("expired", this.expired);
            card.put("blocked", this.blocked);
            return card;
        }
    }

    private class Address {
        private final String country;
        private final String streetAddress;
        private final String postOffice;
        private final int postalCode;

        Address (JSONObject data) {
            this.country = data.getString("country");
            this.streetAddress = data.getString("streetAddress");
            this.postOffice = data.getString("postOffice");
            this.postalCode = data.getInt("postalCode");
        }

        @Override
        public String toString () {
            return String.format("%s, %d %s, %s", this.streetAddress, this.postalCode, this.postOffice.toUpperCase(), this.country);
        }

        JSONObject toJson () {
            JSONObject data = new JSONObject();
            data.put("country", this.country);
            data.put("streetAddress", this.streetAddress);
            data.put("postOffice", this.postOffice);
            data.put("postalCode", this.postalCode);
            return data;
        }

    }

}