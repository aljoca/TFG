package metashop.uschema.entities;

import metashop.graphdatamodel.EntityType;

public class UEntityTypeSingleLabeled extends UEntityType{

    public UEntityTypeSingleLabeled(String name, EntityType entityType) {
        super(name, entityType);
    }

    @Override
    public String toString() {
        return "UEntityTypeSingleLabeled{}";
    }
}
