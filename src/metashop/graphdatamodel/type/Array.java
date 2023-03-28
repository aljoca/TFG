package metashop.graphdatamodel.type;

public class Array extends Type{

    private final PrimitiveType primitiveType;

    public Array(String array) {
        this.primitiveType = new PrimitiveType(array);
    }

    public PrimitiveType getPrimitiveType() {
        return primitiveType;
    }

    @Override
    public String toString() {
        return "Array{" +
                "primitiveType=" + primitiveType +
                '}';
    }
}
