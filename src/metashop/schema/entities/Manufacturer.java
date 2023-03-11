package metashop.schema.entities;

import java.util.HashMap;
import java.util.Map;

public class Manufacturer {

    private Integer __manufacturerId;
    private String name;

    public Manufacturer(Integer __manufacturerId, String name) {
        this.__manufacturerId = __manufacturerId;
        this.name = name;
    }

    public Integer getManufacturerId() {
        return __manufacturerId;
    }

    public String getName() {
        return name;
    }
}
