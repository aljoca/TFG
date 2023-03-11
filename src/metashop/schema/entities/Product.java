package metashop.schema.entities;

import java.util.HashMap;
import java.util.Map;

public class Product {
    public Map<String, Class> getProductAttributes(){
        Map<String, Class> productAttributes = new HashMap<>();
        productAttributes.put("productId", String.class);
        productAttributes.put("imageUrl", String.class);
        productAttributes.put("description", String.class);
        productAttributes.put("price", String.class);
        return productAttributes;
    }

}
