package metashop.schema.inputdatamodel.relationships;

import metashop.schema.inputdatamodel.entities.Discount;
import metashop.schema.inputdatamodel.entities.Order;

public class AppliedTo {

    private Discount discount;
    private Order order;

    public AppliedTo(Discount discount, Order order) {
        this.discount = discount;
        this.order = order;
    }

    public Discount getDiscount() {
        return discount;
    }

    public Order getOrder() {
        return order;
    }
}
