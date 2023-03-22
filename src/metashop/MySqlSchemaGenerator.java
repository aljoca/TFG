package metashop;

import metashop.uschema.URelationshipType;
import metashop.uschema.USchemaModel;
import metashop.uschema.UStructuralVariation;
import metashop.uschema.entities.UEntityType;
import metashop.uschema.features.UAttribute;
import metashop.uschema.features.UKey;
import metashop.uschema.features.UReference;
import metashop.uschema.types.UPrimitiveType;
import org.apache.commons.lang3.StringUtils;
import org.neo4j.driver.Record;

import java.util.ArrayList;
import java.util.HashMap;

public class MySqlSchemaGenerator {

    public static void createMySQLSchemaFromUSchema(ArrayList<Record> incomingRelationships, ArrayList<Record> outgoingRelationships, USchemaModel uSchemaModel){
        HashMap<String, String> relationshipCardinality = calculateRelationshipCardinality(uSchemaModel.getuRelationships(), incomingRelationships, outgoingRelationships);
        for (UEntityType uEntity: uSchemaModel.getUEntities().values()) {
            createEntityTable(uEntity);
            ArrayList<UReference> references = uEntity.getUStructuralVariation().getReferences();
        }

        // Recorro otra vez la lista de entidades porque ya sé que están todas las tablas necesarias creadas
        for (UEntityType uEntity: uSchemaModel.getUEntities().values()) {
            ArrayList<UReference> references = uEntity.getUStructuralVariation().getReferences();
            for (UReference uReference: references) {
                switch (relationshipCardinality.get(uReference.getName())) {
                    case "1:1" -> {
                        addForeignKeyToTable(uEntity.getName(), uReference.getUEntityTypeDestination().getUStructuralVariation().getKey());
                    }
                    case "1:N" -> {
                        addForeignKeyToTable(uReference.getUEntityTypeDestination().getName(), uEntity.getUStructuralVariation().getKey());
                    }
                    case "N:M" -> {
                        String tableName = uEntity.getName() + "_" + StringUtils.lowerCase(uReference.getName()) + "_" + uReference.getUEntityTypeDestination().getName();
                        createRelationshipTable(tableName, uEntity, uReference.getUEntityTypeDestination(), uReference.getUStructuralVariationFeaturedBy());
                    }
                }
            }
        }
    }

    private static void createEntityTable(UEntityType uEntity){
        ArrayList<String> properties = new ArrayList<>();
        HashMap<String, UAttribute> attributes = uEntity.getUStructuralVariation().getAttributes();
        UKey key = uEntity.getUStructuralVariation().getKey();
        for (UAttribute uAttribute: attributes.values()) {
            properties.add(uAttribute.getName() + " " + transformAtributeTypeToMySQL((UPrimitiveType)uAttribute.getType()) + transformMandatoryToMySQL(uAttribute.isMandatory()));
        }
        StringBuilder primaryKey = new StringBuilder("PRIMARY KEY(");
        key.getUAttributes().forEach(uAttribute -> primaryKey.append(uAttribute.getName()).append(","));
        String pk = primaryKey.substring(0, primaryKey.length()-1);
        properties.add(pk + ")");
        StringBuilder create = new StringBuilder("CREATE TABLE " + uEntity.getName() + "(");
        properties.forEach(property -> create.append(property).append(","));
        String createTable = create.subSequence(0, create.length()-1) + ");";
        System.out.println(createTable);
    }

    private static void addForeignKeyToTable(String table, UKey foreignKey){
        String addForeignKey = "ALTER TABLE " + table + " ADD FOREIGN KEY (";
        String fkReference = "";
        String referencesTo = "";

        for (UAttribute uAttribute: foreignKey.getUAttributes()) {
            System.out.println("ALTER TABLE " + table + " ADD COLUMN " + uAttribute.getName() + "Ref "
                    + transformAtributeTypeToMySQL((UPrimitiveType) uAttribute.getType()) + " " + transformMandatoryToMySQL(uAttribute.isMandatory()) + ";");
            fkReference += uAttribute.getName() + "Ref, ";
            referencesTo += uAttribute.getName() + ", ";
        }
        fkReference = StringUtils.substring(fkReference, 0,fkReference.length()-2);
        referencesTo = StringUtils.substring(referencesTo, 0,referencesTo.length()-2);
        System.out.println(addForeignKey + fkReference + ") REFERENCES " + StringUtils.substring(foreignKey.getName(), 4) + "(" + referencesTo + ");");
    }

    private static void createRelationshipTable(String tableName, UEntityType originEntity, UEntityType destinationEntity, UStructuralVariation relationshipStructuralVariation){
        UKey originKey = originEntity.getUStructuralVariation().getKey();
        UKey destinationKey = destinationEntity.getUStructuralVariation().getKey();
        String createTable = "CREATE TABLE " + tableName + "(" ;

        StringBuilder originPK = new StringBuilder();
        StringBuilder originPKWithoutType = new StringBuilder();

        for (UAttribute uAttribute: originKey.getUAttributes()) {
            originPK.append(uAttribute.getName()).append(" ").append(transformAtributeTypeToMySQL((UPrimitiveType) uAttribute.getType())).append(",");
            originPKWithoutType.append(uAttribute.getName()).append(",");
        }

        StringBuilder destinationPK = new StringBuilder();
        StringBuilder destinationPKWithoutType = new StringBuilder();
        StringBuilder destinationPKWithoutRef = new StringBuilder();
        StringBuilder destinationFK = new StringBuilder();

        for (UAttribute uAttribute: destinationKey.getUAttributes()) {
            destinationPKWithoutRef.append(uAttribute.getName()).append(",");
            destinationPK.append(uAttribute.getName()).append("Ref ").append(transformAtributeTypeToMySQL((UPrimitiveType) uAttribute.getType())).append(",");
            destinationPKWithoutType.append(uAttribute.getName()).append("Ref,");
            destinationFK.append(uAttribute.getName()).append("Ref,");
        }

        System.out.println(createTable + originPK + StringUtils.substring(destinationPK.toString(), 0, destinationPK.length()-1)
                + ", PRIMARY KEY(" + originPKWithoutType + StringUtils.substring(destinationPKWithoutType.toString(), 0, destinationPKWithoutType.length()-1) + "));");
        System.out.println("ALTER TABLE " + tableName + " ADD FOREIGN KEY(" + StringUtils.substring(originPKWithoutType.toString(), 0, originPKWithoutType.length()-1) + ") REFERENCES "
                + originEntity.getName() + "(" + StringUtils.substring(originPKWithoutType.toString(), 0, originPKWithoutType.length()-1) + ");");
        System.out.println("ALTER TABLE " + tableName + " ADD FOREIGN KEY(" + StringUtils.substring(destinationFK.toString(), 0, destinationFK.length()-1) + ") REFERENCES "
                + destinationEntity.getName() + "(" + StringUtils.substring(destinationPKWithoutRef.toString(), 0, destinationPKWithoutRef.length()-1) + ");");

        for (UAttribute uAttribute: relationshipStructuralVariation.getAttributes().values()) {
            System.out.println("ALTER TABLE " + tableName + " ADD COLUMN " + uAttribute.getName() + " "
                    + transformAtributeTypeToMySQL((UPrimitiveType) uAttribute.getType()) + " " + transformMandatoryToMySQL(uAttribute.isMandatory()) + ";");
        }
    }

    public static HashMap<String, String> calculateRelationshipCardinality(HashMap<String, URelationshipType> relationshipTypes, ArrayList<Record> incomingRelationships, ArrayList<Record> outgoingRelationships){
        HashMap<String, String> relationshipsCardinality = new HashMap<>();
        HashMap<String, Integer> incoming = new HashMap<>();
        HashMap<String, Integer> outgoing = new HashMap<>();
        incomingRelationships.forEach(incomingRelationship -> incoming.put(incomingRelationship.values().get(0).asString(), incomingRelationship.values().get(1).asInt()));
        outgoingRelationships.forEach(outgoingRelationship -> outgoing.put(outgoingRelationship.values().get(0).asString(), outgoingRelationship.values().get(1).asInt()));
        outgoing.keySet().forEach(relationship -> {
            int out = outgoing.get(relationship);
            int in = incoming.get(relationship);
            if (out == 1 && in == 1){
                relationshipsCardinality.put(relationship, "1:1");
            }
            else if (((out == 1 && in > 1) || (out > 1 && in == 1)) && relationshipTypes.get(relationship).getuStructuralVariation().getAttributes().isEmpty()){
                relationshipsCardinality.put(relationship, "1:N");
            }
            else {
                relationshipsCardinality.put(relationship, "N:M");
            }
        });
        return  relationshipsCardinality;
    }

    public static String transformMandatoryToMySQL(boolean mandatory){
        return (mandatory ? "NOT NULL" : "");
    }

    public static String transformAtributeTypeToMySQL(UPrimitiveType type){
        // TODO Estoy hay que revisarlo, evidentemente
        return switch (type.getName()) {
            case "Long" -> "INT(50)";
            case "Double" -> "FLOAT(10,2)";
            case "Boolean" -> "BOOL";
            case "String" -> "VARCHAR(50)";
            default -> null;
        };
    }
}
