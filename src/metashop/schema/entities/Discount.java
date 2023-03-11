package metashop.schema.entities;

import java.util.HashMap;
import java.util.Map;

public class Discount {
    public Map<String, Class> getDiscountAttributes(){
        Map<String, Class> discountAttributes = new HashMap<>();
        discountAttributes.put("productId", String.class);
        discountAttributes.put("imageUrl", String.class);
        discountAttributes.put("description", String.class);
        discountAttributes.put("price", String.class);
        return discountAttributes;
    }
}
