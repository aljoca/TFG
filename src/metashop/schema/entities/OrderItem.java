package metashop.schema.entities;

public class OrderItem {

    private String __orderItemId;
    private String name;
    private Integer quantity;
    private Double subPrice;

    public OrderItem(String __orderItemId, String name, Integer quantity, Double subPrice) {
        this.__orderItemId = __orderItemId;
        this.name = name;
        this.quantity = quantity;
        this.subPrice = subPrice;
    }

    public String getOrderItemId() {
        return __orderItemId;
    }

    public String getName() {
        return name;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Double getSubPrice() {
        return subPrice;
    }

}
