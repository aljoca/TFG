package metashop.uschema.types;

public class UList extends UType{

    private UType uType;

    public UList(UType uType) {
        this.uType = uType;
    }

    public UType getuType() {
        return uType;
    }
}
