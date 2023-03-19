package metashop.uschema.features;

import metashop.graphdatamodel.Property;
import metashop.graphdatamodel.type.Type;
import metashop.uschema.types.UType;

public class UAttribute extends UStructuralFeature {

    private final UType type;

    public UAttribute(Property property) {
        super(property.getName());
        this.type = generateUType(property.getType());
    }

    private UType generateUType(Type type){
        return null;
    }
}
