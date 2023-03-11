package metashop.schema.entities;

import java.util.HashMap;
import java.util.Map;

public class ProductCategory {

    private String __categoryId;
    private String name;
    private String description;

    public ProductCategory(String __categoryId, String name, String description) {
        this.__categoryId = __categoryId;
        this.name = name;
        this.description = description;
    }

    public String get__categoryId() {
        return __categoryId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

}
