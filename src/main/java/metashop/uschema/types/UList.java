package metashop.uschema.types;

public class UList extends UType{

    private final UType uType;

    public UList(UType uType) {
        this.uType = uType;
    }

    public UType getUType() {
        return uType;
    }
}
