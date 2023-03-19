package metashop.uschema.entities;

import metashop.uschema.UStructuralVariation;
import metashop.uschema.features.UReference;

public abstract class UEntityType {

    private final boolean root;
    private final String name;
    private final UStructuralVariation uStructuralVariation;

    public UEntityType(String name) {
        this.name = name;
        this.root = true;
        this.uStructuralVariation = new UStructuralVariation();
    }

    public boolean isRoot() {
        return root;
    }

    public String getName() {
        return name;
    }

    public UStructuralVariation getuStructuralVariation() {
        return uStructuralVariation;
    }

    public void addReference(UReference uReference){
        this.uStructuralVariation.addFeature(uReference);
    }

    @Override
    public String toString() {
        return "UEntityType{" +
                "root=" + root +
                ", name='" + name + '\'' +
                '}';
    }
}
