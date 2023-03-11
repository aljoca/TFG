package metashop.schema.entities;

import java.util.HashMap;
import java.util.Map;

public class PaymentMethod {

    private String __paymentMethodId;
    private String name;
    private String info;

    public PaymentMethod(String __paymentMethodId, String name, String info) {
        this.__paymentMethodId = __paymentMethodId;
        this.name = name;
        this.info = info;
    }

    public String getPaymentMethodId() {
        return __paymentMethodId;
    }

    public String getName() {
        return name;
    }

    public String getInfo() {
        return info;
    }

}
