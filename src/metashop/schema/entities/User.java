package metashop.schema.entities;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class User {
    public Map<String, Class> geUserAttributes(){
        Map<String, Class> productAttributes = new HashMap<>();
        productAttributes.put("___userId", String.class);
        productAttributes.put("email", String.class);
        productAttributes.put("password", String.class);
        productAttributes.put("country", String.class);
        productAttributes.put("isPremium", Boolean.class);
        productAttributes.put("shoppingAddress", String.class);
        productAttributes.put("shopOpinion", String.class);
        productAttributes.put("registerDate", Date.class);
        return productAttributes;
    }
}
