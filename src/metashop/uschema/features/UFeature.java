package metashop.uschema.features;

public abstract class UFeature {

    private final String name;
    private final boolean mandatory;

    public UFeature(String name, boolean mandatory) {
        this.name = name;
        this.mandatory = mandatory;
    }

    public String getName() {
        return name;
    }

    public boolean isMandatory() {
        return mandatory;
    }
}
