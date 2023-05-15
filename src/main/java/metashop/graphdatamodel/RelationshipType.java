package metashop.graphdatamodel;

import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
import java.util.ArrayList;
import java.util.Objects;


public class RelationshipType {

    private final Label label;
    private EntityType origin;
    private EntityType destination;
    private final StructuralVariation structuralVariation;
    private final static int RELATIONSHIP_LABEL_INDEX = 0;
    public final static int ORIGIN_ENTITY_TYPE_INDEX = 1;
    public final static int DESTINATION_ENTITY_TYPE_INDEX = 2;
    private static final int RELATIONSHIP_PROPERTIES_INDEX = 3;

    public RelationshipType(Record relationship){
         this.label = generateRelationshipLabel(relationship);
         this.structuralVariation = generateStructuralVariation(relationship);
     }

    /**
     * Método para generar la Structural Variation de la relación
     * @see StructuralVariation
     * @param relationship Relación para la que se quiere generar la Structural Variation. En este caso, asumimos que solo puede tener una Structural Variation
     * @return
     */
    public StructuralVariation generateStructuralVariation(Record relationship) {
        ArrayList<Value> properties = new ArrayList<>();
        relationship.values().get(RELATIONSHIP_PROPERTIES_INDEX).values().forEach(properties::add);
        return (!properties.isEmpty()) ? new StructuralVariation(properties) : null;
    }

    /**
     * Método para generar la etiqueta de la relación
     * @see Label
     * @param relationship Relación para la que se quiere generar la etiqueta. En este caso, asumimos que solo puede tener una etiqueta.
     * @return Etiqueta de la relación
     */
    public static Label generateRelationshipLabel(Record relationship){
       String relationshipValue = relationship.values().get(RELATIONSHIP_LABEL_INDEX).asString();
       return new Label(relationshipValue);
    }

    public StructuralVariation getStructuralVariation() {
        return structuralVariation;
    }

    public EntityType getOrigin() {
        return origin;
    }

    public EntityType getDestination() {
        return destination;
    }

    public void setOrigin(EntityType origin) {
        this.origin = origin;
    }

    public void setDestination(EntityType destination) {
        this.destination = destination;
    }


    public String getName(){
        return this.label.getName();
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
                ",origin='" + origin.getName() +
                "',destination='" + destination.getName() +
                "',structuralVariations=" + structuralVariation +
                "}";
    }
}
