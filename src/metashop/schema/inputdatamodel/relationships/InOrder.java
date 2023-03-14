package metashop.schema.inputdatamodel.relationships;

import metashop.schema.inputdatamodel.entities.Order;
import metashop.schema.inputdatamodel.entities.Product;

public class InOrder {

    private Product product;
    private Order order;

    public InOrder(Product product, Order order) {
        this.product = product;
        this.order = order;
    }

    public Product getProduct() {
        return product;
    }

    public Order getOrder() {
        return order;
    }
}
