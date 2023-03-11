package metashop.schema.entities;

import java.util.ArrayList;
import java.util.Date;

public class Order {

    private String __orderId;
    private String totalPrice;
    private Date orderDate;
    private ArrayList<OrderItem> orderItems;
    private ArrayList<Discount> discounts;

    public Order(String __orderId, String totalPrice, Date orderDate) {
        this.__orderId = __orderId;
        this.totalPrice = totalPrice;
        this.orderDate = orderDate;
        this.orderItems = new ArrayList<>();
        this.discounts = new ArrayList<>();
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

    public ArrayList<OrderItem> getOrderItems() {
        return orderItems;
    }

    public ArrayList<Discount> getDiscounts() {
        return discounts;
    }
}
