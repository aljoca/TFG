package metashop.uschema.entities;

import metashop.graphdatamodel.EntityType;
import metashop.graphdatamodel.StructuralVariation;
import metashop.uschema.USchemaType;
import metashop.uschema.UStructuralVariation;
import metashop.uschema.features.UReference;

import java.util.Objects;

public abstract class UEntityType extends USchemaType{

    private final boolean root;
    private final UStructuralVariation uStructuralVariation;

    public UEntityType(String name, EntityType entityType) {
        super(name);
        this.root = true;
        this.uStructuralVariation = new UStructuralVariation(this);
        StructuralVariation structuralVariation = entityType.getStructuralVariation();
        if (Objects.nonNull(structuralVariation)){
            this.uStructuralVariation.generateFeatures(name, structuralVariation.getProperties());
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

    @Override
    public String toString() {
        return "UEntityType{" +
                "root=" + root +
                ", name='" + super.getName() + '\'' +
                '}';
    }
}
