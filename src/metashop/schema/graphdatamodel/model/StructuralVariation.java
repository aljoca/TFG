package metashop.schema.graphdatamodel.model;

import org.neo4j.driver.Value;

import java.util.ArrayList;

public class StructuralVariation {

    private ArrayList<Property> properties;

    public StructuralVariation(ArrayList<Value> propertiesValues) {
        this.properties = new ArrayList<>();
        propertiesValues.forEach(property -> this.properties.add(new Property(property.get(0).asString(), property.get(1).asString())));
    }

    @Override
    public String toString() {
        return "StructuralVariation{" +
                "properties=" + properties +
                '}';
    }
}
