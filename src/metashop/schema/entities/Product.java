package metashop.schema.entities;

import java.util.HashMap;
import java.util.Map;

public class Product {
    public Map<String, String> getProductAttributes(){
        Map<String, String> productAttributes = new HashMap<>();
        productAttributes.put("productId", "String");
        productAttributes.put("imageUrl", "String");
        productAttributes.put("description", "String");
        productAttributes.put("price", "String");
        return productAttributes;
    }

}
