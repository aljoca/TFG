package metashop.schema.graphdatamodel;

import metashop.schema.graphdatamodel.model.EntityType;
import metashop.schema.graphdatamodel.model.GraphSchema;
import metashop.schema.graphdatamodel.model.Label;
import metashop.schema.graphdatamodel.model.Tuple;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public abstract class InputModelToGraphSchema {

//    static GraphSchema generateEntitiesGraphSchemaFromModel(Class... entities){
//        for (Class entity: entities) {
//
//        }
//    }
//
//    static EntityType generateEntityTypeFromModel(Class entity){
//        EntityType entityType = new EntityType(entity);
//        entityType.addLabel(new Label(getEntityMap(entity).getClassName()));
//        entityType.addStructuralVariations();
//    }
//
//    static Tuple getEntityMap(Class entity){
//        Map<String, String> mappedEntity = new HashMap<>();
//        for (Field f: entity.getDeclaredFields()) {
//            f.setAccessible(true);
//            mappedEntity.put(f.getName(), f.getType().getSimpleName());
//        }
//        Tuple result = new Tuple(entity.getSimpleName(), mappedEntity);
//        return result;
//    }

    public static void main(String[] args) {

//        GraphSchema graphSchema = generateEntitiesGraphSchemaFromModel();

    }
}
