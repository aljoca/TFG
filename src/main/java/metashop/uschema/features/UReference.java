package metashop.uschema.features;

import metashop.uschema.UStructuralVariation;
import metashop.uschema.entities.UEntityType;

public class UReference extends ULogicalFeature {

    private final UEntityType uEntityTypeDestination;
    private final UStructuralVariation featuredBy;

    public UReference(String referenceName, UEntityType destination, UStructuralVariation uStructuralVariation) {
        super(referenceName, true);
        this.uEntityTypeDestination = destination;
        this.featuredBy = uStructuralVariation;
        uStructuralVariation.addFeature(this);
    }

    public UEntityType getUEntityTypeDestination() {
        return uEntityTypeDestination;
    }

    public UStructuralVariation getUStructuralVariationFeaturedBy() {
        return featuredBy;
    }

    @Override
    public String toString() {
        return "UReference{" +
                "uEntityTypeDestination=" + uEntityTypeDestination +
                '}';
    }
}
