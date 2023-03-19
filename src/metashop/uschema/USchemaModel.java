package metashop.uschema;

import metashop.graphdatamodel.EntityType;
import metashop.graphdatamodel.GraphSchemaModel;
import metashop.graphdatamodel.RelationshipType;
import metashop.uschema.entities.UEntityType;
import metashop.uschema.entities.UEntityTypeMultiLabeled;
import metashop.uschema.entities.UEntityTypeSingleLabeled;
import metashop.uschema.features.UReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class USchemaModel {

    private final String uName;
    private final HashMap<String, UEntityType> uEntities;
    private final HashMap<String, URelationshipType> uRelationships;

    public USchemaModel(GraphSchemaModel graphSchemaModel) {
        this.uName = graphSchemaModel.getName();
        this.uEntities = new HashMap<>();
        this.uRelationships = new HashMap<>();
        fillUEntities(graphSchemaModel.getEntities());
        processRelationships(graphSchemaModel.getRelationships());
    }

    private void fillUEntities(HashMap<String, EntityType> graphModelEntities){
        graphModelEntities.forEach((entityName, entityType) -> processEntity(entityType));
    }

    private void processRelationships(List<RelationshipType> graphModelRelationships){
        graphModelRelationships.forEach(relationship -> {
            String relationshipName = relationship.getName();
            URelationshipType uRelationshipType = new URelationshipType(relationship);
            uRelationships.put(relationshipName, uRelationshipType);
            UReference uReference = new UReference(relationship.getName(), uEntities.get(relationship.getDestination().getName()), uRelationshipType.getuStructuralVariation(), relationship.getMaxCardinality());
            uEntities.get(relationship.getOrigin().getName()).addReference(uReference);
        });

    }

    private void processEntity(EntityType entityType){

        if (entityType.getLabels().size() == 1) {
            if (!this.uEntities.containsKey(entityType.getName()))
                this.uEntities.put(entityType.getName(), new UEntityTypeSingleLabeled(entityType.getName()));
        }
        else {
            List<UEntityType> parentEntities = new LinkedList<>();
            entityType.getLabels().forEach(label -> {
                final String labelName = label.getName();
                    /*
                        Compruebo si existe alguna entidad en la colección de entidades que tenga el mismo nombre, para no
                        crear dos veces el mismo tipo de entidad.
                        Esto puede pasar, por ejemplo, si primero viene una entidad con la etiqueta "Actor" y seguidamente
                        llega otra con las etiquetas "Actor" y "Director".
                        Si no existe, creo la entidad, la añado a la colección de entidades y a la lista de entidades "padre".
                        Si existe, solamente la añado a la lista de entidades "padre".
                    */
                if (!this.uEntities.containsKey(labelName)) {
                    UEntityType uEntityType = new UEntityTypeSingleLabeled(labelName);
                    parentEntities.add(uEntityType);
                    this.uEntities.put(label.getName(), uEntityType);
                } else {
                    parentEntities.add(this.uEntities.get(labelName));
                }
            });
            this.uEntities.put(entityType.getName(), new UEntityTypeMultiLabeled(entityType.getName(), parentEntities));
        }
    }

    @Override
    public String toString() {
        return "USchemaModel{" +
                "uName='" + uName + '\'' +
                ", \nuEntities=" + uEntities +
                ", \nuRelationships=" + uRelationships +
                '}';
    }
}
