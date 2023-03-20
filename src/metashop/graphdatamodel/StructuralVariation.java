package metashop.graphdatamodel;

import org.neo4j.driver.Value;

import java.util.ArrayList;

public class StructuralVariation {

    private ArrayList<Property> properties;
    private final static int PROPERTY_NAME_INDEX = 0;
    private final static int PROPERTY_TYPE_INDEX = 1;
    private final static int PROPERT_MANDATORY_INDEX = 2;


    public StructuralVariation(ArrayList<Value> propertiesValues) {
        this.properties = new ArrayList<>();
        propertiesValues.forEach(property -> this.properties.add(
                new Property(property.get(PROPERTY_NAME_INDEX).asString(), property.get(PROPERTY_TYPE_INDEX).asString(), property.get(PROPERT_MANDATORY_INDEX).asBoolean())));
    }

    public ArrayList<Property> getProperties() {
        return properties;
    }

    @Override
    public String toString() {
        return "StructuralVariation{" +
                "properties=" + properties +
                '}';
    }
}
