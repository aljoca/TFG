package metashop.uschema.entities;

import metashop.graphdatamodel.EntityType;
import metashop.graphdatamodel.StructuralVariation;
import metashop.uschema.USchemaType;
import metashop.uschema.UStructuralVariation;
import metashop.uschema.features.UReference;

import java.util.Objects;

public abstract class UEntityType extends USchemaType{

    private final UStructuralVariation uStructuralVariation;

    public UEntityType(EntityType entityType) {
        super(entityType.getName());
        this.uStructuralVariation = new UStructuralVariation();
        StructuralVariation structuralVariation = entityType.getStructuralVariation();
        if (Objects.nonNull(structuralVariation)){
            this.uStructuralVariation.generateFeatures(entityType.getName(), structuralVariation.getProperties());
        }
    }

    public String getName() {
        return super.getName();
    }

    public UStructuralVariation getUStructuralVariation() {
        return uStructuralVariation;
    }

    public void addReference(UReference uReference){
        this.uStructuralVariation.addFeature(uReference);
    }
}
