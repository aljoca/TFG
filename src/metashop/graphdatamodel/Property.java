package metashop.graphdatamodel;

import metashop.MetaShopSchema;
import metashop.graphdatamodel.type.Array;
import metashop.graphdatamodel.type.PrimitiveType;
import metashop.graphdatamodel.type.Type;
import org.apache.commons.lang3.StringUtils;


public class Property {

    private final String name;
    private final Type type;

    public Property(String name, String type) {
        this.name = name;
        this.type = getType(type);
    }

    private static Type getType(String type){
        return (MetaShopSchema.types.contains(type)) ?  new PrimitiveType(type) : new Array(StringUtils.substringBefore(type, "Array"));
    }

    @Override
    public String toString() {
        return "Property{" +
                "name='" + name +
                "', type=" + type +
                '}';
    }
}
