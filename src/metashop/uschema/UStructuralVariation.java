package metashop.uschema;

import metashop.graphdatamodel.Property;
import metashop.graphdatamodel.RelationshipType;
import metashop.graphdatamodel.StructuralVariation;
import metashop.uschema.features.UAttribute;
import metashop.uschema.features.UFeature;
import metashop.uschema.features.UReference;

import java.util.ArrayList;
import java.util.HashMap;

public class UStructuralVariation {

    private static int U_STRUCTURAL_VARIATION_IDENTIFIER = 1;
    private final int id;
    private final HashMap<String, UFeature> features;

    public UStructuralVariation() {
        this.features = new HashMap<>();
        // Necesito que las referencias no se añadan de nuevo a la structuralVariation
        this.id = U_STRUCTURAL_VARIATION_IDENTIFIER++;
    }

    public void addFeature(UFeature uFeature){
        features.put(uFeature.getName(), uFeature);
    }
    public void generateUAttributes(ArrayList<Property> properties){

    }

    @Override
    public String toString() {
        return "UStructuralVariation{" +
                "id=" + id +
                ", features=" + features +
                '}';
    }
}
