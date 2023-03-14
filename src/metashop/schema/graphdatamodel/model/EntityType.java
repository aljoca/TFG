package metashop.schema.graphdatamodel.model;

import org.neo4j.driver.Record;
import org.neo4j.driver.Value;

import java.util.ArrayList;

public class EntityType {

    private ArrayList<Label> labels;
    private ArrayList<StructuralVariation> structuralVariations;

    public EntityType(Record record) {
        this.labels = new ArrayList<>();
        this.structuralVariations = new ArrayList<>();
    }

    public void setLabels(ArrayList<Label> labels) {
        this.labels = labels;
    }

    public void setStructuralVariations(ArrayList<StructuralVariation> structuralVariations) {
        this.structuralVariations = structuralVariations;
    }

    public static ArrayList<Label> generateEntityLabels(Record record){
        ArrayList<Label> labels = new ArrayList<>();
        for (Value recordValue: record.values()) {
            for (Value label: recordValue.values()) {
                labels.add(new Label(label.asString()));
            }
        }
        return labels;
    }

}
