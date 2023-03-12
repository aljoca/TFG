package metashop.schema.graphdatamodel.model;

import java.util.ArrayList;

public class EntityType {

    private ArrayList<Label> labels;
    private ArrayList<StructuralVariation> structuralVariations;

    /**
     *
     * @param entities Es variable porque puede tener varias etiquetas
     */
    public EntityType(Class... entities) {
        this.labels = new ArrayList<>();
        this.structuralVariations = new ArrayList<>();
        for (Class entity: entities) {
            this.labels.add(new Label(entity.getSimpleName()));
        }
    }

    public void addLabel(Label label){
        this.labels.add(label);
    }

    public void addStructuralVariations(StructuralVariation structuralVariation){

    }

}
