package metashop.uschema.types;

public class UMap extends UType{

    private final UPrimitiveType uPrimitiveType;
    private final UType valueType;

    public UMap(UPrimitiveType uPrimitiveType, UType valueType) {
        this.uPrimitiveType = uPrimitiveType;
        this.valueType = valueType;
    }
}
