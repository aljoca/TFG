package metashop.uschema.features;

public abstract class UFeature {

    private final String name;

    public UFeature(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
