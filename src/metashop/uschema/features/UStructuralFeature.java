package metashop.uschema.features;

public class UStructuralFeature extends UFeature{

    private final boolean optional;

    public UStructuralFeature(String name, boolean mandatory) {
        super(name, mandatory);
        this.optional = false;
    }
}
