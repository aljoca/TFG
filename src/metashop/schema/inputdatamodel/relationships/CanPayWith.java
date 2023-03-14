package metashop.schema.inputdatamodel.relationships;

import metashop.schema.inputdatamodel.entities.PaymentMethod;
import metashop.schema.inputdatamodel.entities.User;

public class CanPayWith {

    private User user;
    private PaymentMethod paymentMethod;

    public CanPayWith(User user, PaymentMethod paymentMethod) {
        this.user = user;
        this.paymentMethod = paymentMethod;
    }

    public User getUser() {
        return user;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }
}
