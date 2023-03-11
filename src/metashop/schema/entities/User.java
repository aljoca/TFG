package metashop.schema.entities;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class User {
    public Map<String, Class> geUserAttributes(){
        Map<String, Class> usertAttributes = new HashMap<>();
        usertAttributes.put("___userId", Integer.class);
        usertAttributes.put("email", String.class);
        usertAttributes.put("password", String.class);
        usertAttributes.put("country", String.class);
        usertAttributes.put("isPremium", Boolean.class);
        usertAttributes.put("shoppingAddress", String.class);
        usertAttributes.put("shopOpinion", String.class);
        usertAttributes.put("registerDate", Date.class);
        return usertAttributes;
    }
}
