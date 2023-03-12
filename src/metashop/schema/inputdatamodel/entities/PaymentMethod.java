package metashop.schema.inputdatamodel.entities;

public class PaymentMethod {

    private String __paymentMethodId;
    private String name;

    public PaymentMethod(String __paymentMethodId, String name, String info) {
        this.__paymentMethodId = __paymentMethodId;
        this.name = name;
    }

    public String getPaymentMethodId() {
        return __paymentMethodId;
    }

    public String getName() {
        return name;
    }

}
