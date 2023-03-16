package metashop.schema.graphdatamodel.model;

import metashop.MetaShopSchema;
import metashop.schema.graphdatamodel.model.type.Array;
import metashop.schema.graphdatamodel.model.type.PrimitiveType;
import metashop.schema.graphdatamodel.model.type.Type;


public class Property {

    private String name;
    private Type type;

    public Property(String name, String type) {
        this.name = name;
        this.type = getType(type);
    }

    private static Type getType(String type){
        return (MetaShopSchema.types.contains(type)) ?  new PrimitiveType(type) : new Array();
    }

    @Override
    public String toString() {
        return "Property{" +
                "name='" + name +
                ", type=" + type +
                '}';
    }
}
