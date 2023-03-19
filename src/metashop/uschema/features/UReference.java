package metashop.uschema.features;

import metashop.uschema.UStructuralVariation;
import metashop.uschema.entities.UEntityType;

public class UReference extends ULogicalFeature {

    private final UEntityType uEntityTypeDestination;
    private final UStructuralVariation uStructuralVariation;
    private final int lowerBoundCardinality;

    // Tengo que ver cómo calcular el upperBoundCardinality
    private int upperBoundCardinality;

    public UReference(String referenceName, UEntityType destination, UStructuralVariation uStructuralVariation, int upperBoundCardinality) {
        super(referenceName);
        this.uEntityTypeDestination = destination;
        this.uStructuralVariation = uStructuralVariation;
        this.lowerBoundCardinality = 1;
        this.upperBoundCardinality = upperBoundCardinality;
    }

    public UEntityType getUEntityTypeDestination() {
        return uEntityTypeDestination;
    }

    public UStructuralVariation getUStructuralVariation() {
        return uStructuralVariation;
    }

    @Override
    public String toString() {
        return "UReference{" +
                "uEntityTypeDestination=" + uEntityTypeDestination +
                '}';
    }
}
