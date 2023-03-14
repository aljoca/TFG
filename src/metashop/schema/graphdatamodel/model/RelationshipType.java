package metashop.schema.graphdatamodel.model;

import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.Relationship;

import java.util.ArrayList;
import java.util.Objects;


public class RelationshipType {

    private final Label label;
    private final EntityType origin;
    private final EntityType destination;
    private final ArrayList<StructuralVariation> structuralVariations;
    private final static int RELATIONSHIP_ENTITY_TYPE_INDEX = 2;


    public RelationshipType(EntityType origin, EntityType destination, Record record){
         this.origin = origin;
         this.destination = destination;
         this.label = generateRelationshipLabels(record);
         this.structuralVariations = new ArrayList<>();
     }

    /**
     * Método para extraer la etiqueta de la relación
     * @see Label
     * @param relationship Relación para la que se quiere extraer la etiqueta. En este caso, asumimos que solo puede tener una etiqueta.
     * @return Etiqueta de la relación
     */
    public static Label generateRelationshipLabels(Record relationship){
       Value relationshipValue = relationship.values().get(RELATIONSHIP_ENTITY_TYPE_INDEX);
       return new Label(((Relationship)(relationshipValue.asObject())).type());
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
                ", origin=" + origin +
                ", destination=" + destination +
                ", structuralVariations=" + structuralVariations +
                "}";
    }
}
