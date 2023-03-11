package metashop.schema.entities;

import java.util.HashMap;
import java.util.Map;

public class ProductCategory {
    public Map<String, Class> getProductCategoryAttributes(){
        Map<String, Class> productCategoryAttributes = new HashMap<>();
        productCategoryAttributes.put("productId", String.class);
        productCategoryAttributes.put("imageUrl", String.class);
        productCategoryAttributes.put("description", String.class);
        productCategoryAttributes.put("price", String.class);
        return productCategoryAttributes;
    }
}
