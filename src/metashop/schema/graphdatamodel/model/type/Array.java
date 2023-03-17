package metashop.schema.graphdatamodel.model.type;

public class Array extends Type{

    private PrimitiveType primitiveType;

    public Array(String array) {
        this.primitiveType = new PrimitiveType(array);
    }

    @Override
    public String toString() {
        return "Array{" +
                "primitiveType=" + primitiveType +
                '}';
    }
}
