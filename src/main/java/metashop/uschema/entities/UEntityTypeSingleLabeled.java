package metashop.uschema.entities;

import metashop.graphdatamodel.EntityType;

public class UEntityTypeSingleLabeled extends UEntityType{

    public UEntityTypeSingleLabeled(EntityType entityType) {
        super(entityType);
    }

    @Override
    public String toString() {
        return "UEntityTypeSingleLabeled{}";
    }
}
