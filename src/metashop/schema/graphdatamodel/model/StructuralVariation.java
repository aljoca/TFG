package metashop.schema.graphdatamodel.model;

import org.neo4j.driver.Value;

import java.util.ArrayList;

public class StructuralVariation {

    private ArrayList<Property> properties;
    private final static int PROPERTY_NAME_INDEX = 0;
    private final static int PROPERTY_TYPE_INDEX = 1;

    public StructuralVariation(ArrayList<Value> propertiesValues) {
        this.properties = new ArrayList<>();
        propertiesValues.forEach(property -> this.properties.add(
                new Property(property.get(PROPERTY_NAME_INDEX).asString(), property.get(PROPERTY_TYPE_INDEX).asString())));
    }

    @Override
    public String toString() {
        return "StructuralVariation{" +
                "properties=" + properties +
                '}';
    }
}
