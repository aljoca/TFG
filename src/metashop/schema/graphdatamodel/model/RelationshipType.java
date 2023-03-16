package metashop.schema.graphdatamodel.model;

import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.Relationship;

import java.util.ArrayList;
import java.util.Objects;


public class RelationshipType {

    private final Label label;
    private EntityType origin;
    private EntityType destination;
    private final StructuralVariation structuralVariation;
    private final static int RELATIONSHIP_LABEL_INDEX = 0;
    private static final int RELATIONSHIP_PROPERTIES_INDEX = 1;

    public RelationshipType(Record relationship){
         this.label = generateRelationshipLabel(relationship);
         this.structuralVariation = generateStructuralVariation(relationship);
     }

    public StructuralVariation generateStructuralVariation(Record node) {
        ArrayList<Value> properties = new ArrayList<>();
        node.values().get(RELATIONSHIP_PROPERTIES_INDEX).values().forEach(properties::add);
        return new StructuralVariation(properties);
    }

    /**
     * Método para extraer la etiqueta de la relación
     * @see Label
     * @param relationship Relación para la que se quiere extraer la etiqueta. En este caso, asumimos que solo puede tener una etiqueta.
     * @return Etiqueta de la relación
     */
    public static Label generateRelationshipLabel(Record relationship){
       String relationshipValue = relationship.values().get(RELATIONSHIP_LABEL_INDEX).asString();
       return new Label(relationshipValue);
    }

    public void setOrigin(EntityType origin) {
        this.origin = origin;
    }

    public void setDestination(EntityType destination) {
        this.destination = destination;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RelationshipType that = (RelationshipType) o;
        // Para que una relación sea exactamente la misma me valdría con comprobar solamente la etiqueta.
        // Considerando que esto sirve solamente para generar el USchema
        return Objects.equals(label, that.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(label, origin, destination);
    }

    @Override
    public String toString() {
        return "\nRelationshipType{" +
                "label=" + label +
                ",origin=" + origin +
                ",destination=" + destination +
                ",structuralVariations=" + structuralVariation +
                "}";
    }
}
