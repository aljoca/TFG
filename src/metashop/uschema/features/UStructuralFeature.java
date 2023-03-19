package metashop.uschema.features;

public class UStructuralFeature extends UFeature{

    private final boolean optional;

    public UStructuralFeature(String name) {
        super(name);
        this.optional = false;
    }
}
