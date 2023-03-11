package metashop.schema.entities;

public class OrderItem {

    private String __orderItemId;
    private String orderItemName;
    private Integer quantity;
    private Double subPrice;

    public OrderItem(String __orderItemId, String orderItemName, Integer quantity, Double subPrice) {
        this.__orderItemId = __orderItemId;
        this.orderItemName = orderItemName;
        this.quantity = quantity;
        this.subPrice = subPrice;
    }

    public String getOrderItemId() {
        return __orderItemId;
    }

    public String getOrderItemName() {
        return orderItemName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Double getSubPrice() {
        return subPrice;
    }

}
