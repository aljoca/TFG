package metashop.schema.inputdatamodel.entities;

import java.util.Date;

public class Order {

    private String __orderId;
    private String totalPrice;
    private Date orderDate;

    public Order(String __orderId, String totalPrice, Date orderDate) {
        this.__orderId = __orderId;
        this.totalPrice = totalPrice;
        this.orderDate = orderDate;
    }

    public String getOrderId() {
        return __orderId;
    }

    public String getTotalPrice() {
        return totalPrice;
    }

    public Date getOrderDate() {
        return orderDate;
    }

}
