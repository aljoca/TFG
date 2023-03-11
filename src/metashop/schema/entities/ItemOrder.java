package metashop.schema.entities;

import java.util.HashMap;
import java.util.Map;

public class ItemOrder {

    private String __productId;
    private String productName;
    private Integer quantity;
    private Double subPrice;

    public ItemOrder(String __productId, String productName, Integer quantity, Double subPrice) {
        this.__productId = __productId;
        this.productName = productName;
        this.quantity = quantity;
        this.subPrice = subPrice;
    }

    public String getItemOrderId() {
        return __productId;
    }

    public String getProductName() {
        return productName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Double getSubPrice() {
        return subPrice;
    }

}
