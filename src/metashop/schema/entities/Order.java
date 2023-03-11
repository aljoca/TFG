package metashop.schema.entities;

import java.util.HashMap;
import java.util.Map;

public class Order {
    public Map<String, Class> getOrderAttributes(){
        Map<String, Class> orderAttributes = new HashMap<>();
        orderAttributes.put("productId", String.class);
        orderAttributes.put("imageUrl", String.class);
        orderAttributes.put("description", String.class);
        orderAttributes.put("price", String.class);
        return orderAttributes;
    }
}
