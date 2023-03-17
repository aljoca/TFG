package metashop.schema.graphdatamodel.model.type;

public class PrimitiveType extends Type{
    private final String name;

    public PrimitiveType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "PrimitiveType{" +
                "name='" + name +
                "'}";
    }
}
