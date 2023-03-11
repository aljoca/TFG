package metashop.schema.entities;

import java.util.HashMap;
import java.util.Map;

public class ItemOrder {
    public Map<String, Class> getItemOrderAttributes(){
        Map<String, Class> itemOrderAttributes = new HashMap<>();
        itemOrderAttributes.put("productId", String.class);
        itemOrderAttributes.put("imageUrl", String.class);
        itemOrderAttributes.put("description", String.class);
        itemOrderAttributes.put("price", String.class);
        return itemOrderAttributes;
    }
}
