package sharedResources.productCatalog;

import org.json.JSONObject;
import sharedResources.structures.Customer;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Product implements Cloneable {
    private int productId;
    private int barCode;
    private String name;
    private float vat;
    private String keyword;

    private float price;
    private float discount;
    private long discountFrom;
    private long discountUntil;
    private boolean bonusOnlyDiscount;

    // Formats floats with . as separator
    private final NumberFormat nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
    private DecimalFormat decimalFormat = new DecimalFormat("##.00");

    public Product () {}

    public Product (int productId, int barCode, String name, float vat, String keyword) {
        this.productId = productId;
        this.barCode = barCode;
        this.name = name;
        this.vat = vat;
        this.keyword = keyword;
    }

    public Product (int productId, int barCode, String name, float vat, String keyword, float price) {
        this.productId = productId;
        this.barCode = barCode;
        this.name = name;
        this.vat = vat;
        this.keyword = keyword;
        this.price = price;
    }

    @Override
    public Product clone () throws CloneNotSupportedException {
        return (Product) super.clone();
    }
    
    public String getName () {
        return this.name;
    }

    public Integer getBarCode () {
        return this.barCode;
    }

    public float getVat () {
        return this.vat;
    }

    public String getKeyword () {
        return this.keyword;
    }

    /**
     * Get the price for a bonus customer
     *
     * @param customer
     * @return
     */
    public float getDiscountedPrice (Customer customer) {
        this.decimalFormat = (DecimalFormat) this.nf;
        long now = System.currentTimeMillis() / 1000;
        if (this.discount == 0 || (this.bonusOnlyDiscount && customer == null) || (this.discountFrom > now || this.discountUntil < now))
            return Float.parseFloat(this.decimalFormat.format(this.price));

        if ((this.bonusOnlyDiscount && customer != null && customer.activeBonus()) || !this.bonusOnlyDiscount)
            return Float.parseFloat(this.decimalFormat.format((1 - this.discount) * this.price));

        return Float.parseFloat(this.decimalFormat.format(this.price));
    }

    public float getDiscountedPrice () {
        this.decimalFormat = (DecimalFormat) this.nf;
        long now = System.currentTimeMillis() / 1000;
        float discountedPrice = this.discountUntil > now && this.discountFrom < now && !this.bonusOnlyDiscount ? this.discount : 0;
        return Float.parseFloat(this.decimalFormat.format((1 - discountedPrice) * this.price));
    }

    public float getPrice () {
        return this.price;
    }

    public static Product fromJson (JSONObject data) {
        int productId = data.optInt("id", -1);
        if (productId == -1)
            productId = data.getInt("productId"); // To ensure consistency across everything, the product catalog database uses id, but we want to use productId for less ambiguity
        Product product = new Product(productId, data.getInt("barCode"), data.getString("name"), data.getFloat("vat"), data.getString("keyword"), data.optFloat("price", 0));
        product.setDiscount(data.optFloat("discount", 0), data.optLong("discountFrom", 0), data.optLong("discountUntil", 0), data.optBoolean("bonusOnlyDiscount", false));
        return product;
    }

    public JSONObject toJson () {
        JSONObject product = new JSONObject();
        product.put("productId", this.productId);
        product.put("barCode", this.barCode);
        product.put("name", this.name);
        product.put("vat", this.vat);
        product.put("keyword", this.keyword);
        product.put("price", this.price);
        product.put("discount", this.discount);
        product.put("discountUntil", this.discountUntil);
        product.put("discountFrom", this.discountFrom);
        product.put("bonusOnlyDiscount", this.bonusOnlyDiscount);
        return product;
    }

    @Override
    public String toString () {
        return "{" + "\n productId='" + this.productId + '\'' + ",\n description='" + this.barCode + '\'' + ",\n name='" + this.name + '\'' + ",\n vat=" + this.vat + ",\n keyword=" + this.keyword + "\n" + '}';
    }

    public int getProductId () {
        return this.productId;
    }

    // SETTERS
    public void setProductId (int productId) {
        this.productId = productId;
    }

    public void setBarCode (int barCode) {
        this.barCode = barCode;
    }

    public void setName (String name) {
        this.name = name;
    }

    public void setVat (float vat) {
        this.vat = vat;
    }

    public void setKeyword (String keyword) {
        this.keyword = keyword;
    }

    public void setPrice (float price) {
        this.price = price;
    }

    public void setDiscount (float discount, long from, long until, boolean bonusOnly) {
        this.bonusOnlyDiscount = bonusOnly;
        this.discount = discount;
        this.discountFrom = from;
        this.discountUntil = until;
    }

    public float getDiscount () {
        return this.discount;
    }

    public boolean getBonusOnlyDiscount () {
        return this.bonusOnlyDiscount;
    }

    public String getDiscountFrom () {
        Date date = new Date(this.discountFrom * 1000);
        DateFormat format = new SimpleDateFormat("dd.MM.yyyy");
        format.setTimeZone(TimeZone.getTimeZone(ZoneId.systemDefault()));
        return format.format(date);
    }

    public String getDiscountUntil () {
        Date date = new Date(this.discountUntil * 1000);
        DateFormat format = new SimpleDateFormat("dd.MM.yyyy");
        format.setTimeZone(TimeZone.getTimeZone(ZoneId.systemDefault()));

        return format.format(date);
    }
}