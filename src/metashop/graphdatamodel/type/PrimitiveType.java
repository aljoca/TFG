package metashop.graphdatamodel.type;

public class PrimitiveType extends Type{
    private final String name;

    public PrimitiveType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "PrimitiveType{" +
                "name='" + name +
                "'}";
    }
}
