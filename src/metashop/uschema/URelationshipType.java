package metashop.uschema;

import metashop.graphdatamodel.RelationshipType;
import metashop.graphdatamodel.StructuralVariation;
import java.util.Objects;

public class URelationshipType {

    private final String name;
    private final UStructuralVariation uStructuralVariation;

    public URelationshipType(RelationshipType relationshipType) {
        this.name = relationshipType.getName();

        // Aquí viene la parte chunga, que es distinguir el tipo de relación de cada propiedad de la relación.

        this.uStructuralVariation = new UStructuralVariation();
        StructuralVariation structuralVariation = relationshipType.getStructuralVariation();
        if (Objects.nonNull(structuralVariation)){
            this.uStructuralVariation.generateUAttributes(structuralVariation.getProperties());
        }
    }

    public String getName() {
        return name;
    }

    public UStructuralVariation getuStructuralVariation() {
        return uStructuralVariation;
    }

    @Override
    public String toString() {
        return "URelationshipType{" +
                "name='" + name + '\'' +
                ", uStructuralVariation=" + uStructuralVariation +
                '}';
    }
}
