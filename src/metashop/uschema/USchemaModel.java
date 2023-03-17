package metashop.uschema;

import metashop.graphdatamodel.EntityType;
import metashop.graphdatamodel.GraphSchemaModel;
import metashop.uschema.entities.UEntityType;
import metashop.uschema.entities.UEntityTypeMultiLabeled;
import metashop.uschema.entities.UEntityTypeSingleLabeled;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class USchemaModel {

    private final String uName;
    private final HashMap<String, UEntityType> uEntities;

    public USchemaModel(GraphSchemaModel graphSchemaModel) {
        this.uName = graphSchemaModel.getName();
        this.uEntities = new HashMap<>();
        fillUEntities(graphSchemaModel.getEntities());
    }

    private void fillUEntities(HashMap<String, EntityType> graphModelEntities){
        graphModelEntities.forEach((entityName, entityType) -> uEntities.put(entityName, processEntity(entityType)));
    }

    private UEntityType processEntity(EntityType entityType){

        if (entityType.getLabels().size() == 1) {
            return new UEntityTypeSingleLabeled(entityType.getName());
        }

        List<UEntityType> uEntityTypes = new LinkedList<>();
        entityType.getLabels().forEach(label -> {
                    final String labelName = label.getName();
                    if (!this.uEntities.containsKey(labelName)) {
                        UEntityType uEntityType = new UEntityTypeSingleLabeled(labelName);
                        uEntityTypes.add(uEntityType);
                        this.uEntities.put(label.getName(), uEntityType);
                    }
                    else {
                        uEntityTypes.add(this.uEntities.get(labelName));
                    }
                }
               );
        return new UEntityTypeMultiLabeled(entityType.getName(), uEntityTypes);
    }

    @Override
    public String toString() {
        return "USchemaModel{" +
                "uName='" + uName + '\'' +
                ", uEntities=" + uEntities +
                '}';
    }
}
