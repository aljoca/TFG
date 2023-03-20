package metashop.uschema.types;

public class UPrimitiveType extends UType{

    private final String name;
    public UPrimitiveType(String name) {

        this.name = name;

    }

    public String getName() {
        return name;
    }
}
