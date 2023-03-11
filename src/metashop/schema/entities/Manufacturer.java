package metashop.schema.entities;

import java.util.HashMap;
import java.util.Map;

public class Manufacturer {

    private Integer __manufacturer_id;
    private String name;

    private Map<String, Class> manufacturerAttributes;

    Manufacturer(){
        manufacturerAttributes = new HashMap();
    }

    public Map<String, Class> getManufacturerAttributes(){
        Map<String, Class> manufacturerAttributes = new HashMap<>();
        manufacturerAttributes.put("__manufacturer_id", Integer.class);
        manufacturerAttributes.put("name", String.class);
        return manufacturerAttributes;
    }
}
