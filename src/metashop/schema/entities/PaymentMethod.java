package metashop.schema.entities;

import java.util.HashMap;
import java.util.Map;

public class PaymentMethod {
    public Map<String, Class> getPaymentMethodAttributes(){
        Map<String, Class> paymentMethodAttributes = new HashMap<>();
        paymentMethodAttributes.put("productId", String.class);
        paymentMethodAttributes.put("imageUrl", String.class);
        paymentMethodAttributes.put("description", String.class);
        paymentMethodAttributes.put("price", String.class);
        return paymentMethodAttributes;
    }
}
