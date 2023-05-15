package metashop.graphdatamodel;

import metashop.GraphMigrator;
import metashop.graphdatamodel.type.Array;
import metashop.graphdatamodel.type.PrimitiveType;
import metashop.graphdatamodel.type.Type;
import org.apache.commons.lang3.StringUtils;


public class Property {

    private final String name;
    private final Type type;
    private final boolean mandatory;

    public Property(String name, String type, boolean mandatory) {
        this.name = name;
        this.type = getType(type);
        this.mandatory = mandatory;
    }

    private static Type getType(String type){
        return (GraphMigrator.types.contains(type)) ?  new PrimitiveType(type) : new Array(StringUtils.substringBefore(type, "Array"));
    }

    public String getName() {
        return name;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Property{" +
                "name='" + name +
                "', type=" + type +
                '}';
    }
}
