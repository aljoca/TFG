package metashop.uschema.features;

import metashop.graphdatamodel.Property;
import metashop.graphdatamodel.type.Array;
import metashop.graphdatamodel.type.PrimitiveType;
import metashop.graphdatamodel.type.Type;
import metashop.uschema.types.UList;
import metashop.uschema.types.UPrimitiveType;
import metashop.uschema.types.UType;

public class UAttribute extends UStructuralFeature {

    private final UType type;

    public UAttribute(Property property) {
        super(property.getName(), property.isMandatory());
        this.type = generateUType(property.getType());
    }

    private UType generateUType(Type type){
        if  (type instanceof PrimitiveType) {
            return new UPrimitiveType(((PrimitiveType) type).getName());
        }
        else{
            UPrimitiveType uPrimitiveType = new UPrimitiveType(((Array)type).getPrimitiveType().getName());
            return new UList(uPrimitiveType);
        }
    }

    public UType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "UAttribute{" +
                "type=" + type +
                '}';
    }
}
