package metashop.uschema.entities;

import metashop.graphdatamodel.EntityType;
import metashop.graphdatamodel.StructuralVariation;
import metashop.uschema.UStructuralVariation;
import metashop.uschema.features.UReference;

import java.util.Objects;

public abstract class UEntityType {

    private final boolean root;
    private final String name;
    private final UStructuralVariation uStructuralVariation;

    public UEntityType(String name, EntityType entityType) {
        this.name = name;
        this.root = true;
        this.uStructuralVariation = new UStructuralVariation();
        StructuralVariation structuralVariation = entityType.getStructuralVariation();
        if (Objects.nonNull(structuralVariation)){
            this.uStructuralVariation.generateUAttributes(structuralVariation.getProperties());
        }
    }

    public boolean isRoot() {
        return root;
    }

    public String getName() {
        return name;
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
                ", name='" + name + '\'' +
                '}';
    }
}
