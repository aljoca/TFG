package metashop.schema.inputdatamodel.relationships;

import metashop.schema.inputdatamodel.entities.Order;
import metashop.schema.inputdatamodel.entities.User;

public class Orders {

    private User user;
    private Order order;

    public Orders(User user, Order order) {
        this.user = user;
        this.order = order;
    }

    public User getUser() {
        return user;
    }

    public Order getOrder() {
        return order;
    }
}
