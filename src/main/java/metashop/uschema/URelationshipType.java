package metashop.uschema;

import metashop.graphdatamodel.RelationshipType;
import metashop.graphdatamodel.StructuralVariation;
import java.util.Objects;

public class URelationshipType extends USchemaType{

    private final UStructuralVariation uStructuralVariation;

    public URelationshipType(RelationshipType relationshipType) {
        super(relationshipType.getName());
        this.uStructuralVariation = new UStructuralVariation();
        StructuralVariation structuralVariation = relationshipType.getStructuralVariation();
        if (Objects.nonNull(structuralVariation)){
            this.uStructuralVariation.generateFeatures(super.getName(), structuralVariation.getProperties());
        }
    }

    public String getName() {
        return super.getName();
    }

    public UStructuralVariation getuStructuralVariation() {
        return uStructuralVariation;
    }

    @Override
    public String toString() {
        return "URelationshipType{" +
                "name='" + super.getName() + '\'' +
                ", uStructuralVariation=" + uStructuralVariation +
                '}';
    }
}
