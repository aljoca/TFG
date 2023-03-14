package metashop.schema.graphdatamodel.model;

import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.Relationship;

import java.util.ArrayList;
import java.util.Objects;


public class RelationshipType {

    private Label label;
    private EntityType origin;
    private EntityType destination;
    private ArrayList<StructuralVariation> structuralVariations;

     public RelationshipType(Record record){
         this.label = generateRelationshipLabels(record);
         this.structuralVariations = new ArrayList<>();
     }

    public static Label generateRelationshipLabels(Record record){
       Value relationship = record.values().get(0);
       return new Label(((Relationship)(relationship.asObject())).type());

    }

    public Label getLabel() {
        return label;
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
