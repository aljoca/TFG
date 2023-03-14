package metashop.schema.graphdatamodel.model;

import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.Node;

import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;

public class EntityType {

    private ArrayList<Label> labels;
    private ArrayList<StructuralVariation> structuralVariations;
    private final String name;

    public EntityType(Record record) {
        this.structuralVariations = new ArrayList<>();
        this.labels = generateEntityLabels(record);
        //this.structuralVariations = generateStructuralVariations(record);
        this.name = labels.stream().map(Label::getName).collect(Collectors.joining());
    }


    public ArrayList<StructuralVariation> generateStructuralVariations(Record record) {
        ArrayList<StructuralVariation> structuralVariations = new ArrayList<>();
        return structuralVariations;
    }

    public static ArrayList<Label> generateEntityLabels(Record record){
        ArrayList<Label> labels = new ArrayList<>();
        for (Value node: record.values()) {
            for (String label: ((Node)(node.asObject())).labels()) {
                labels.add(new Label(label));
            }
        }
        return labels;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntityType that = (EntityType) o;
        return Objects.equals(labels, that.labels) && Objects.equals(name, that.name); // && Objects.equals(structuralVariations, that.structuralVariations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(labels, structuralVariations, name);
    }

    @Override
    public String toString() {
        return "\nEntityType{" +
                "labels=" + labels +
                ", name='" + name + '\'' +
                ", structuralVariations=" + structuralVariations +
                "}";
    }
}
