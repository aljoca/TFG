package metashop;

import metashop.uschema.URelationshipType;
import metashop.uschema.USchemaModel;
import metashop.uschema.UStructuralVariation;
import metashop.uschema.entities.UEntityType;
import metashop.uschema.features.UAttribute;
import metashop.uschema.features.UKey;
import metashop.uschema.features.UReference;
import metashop.uschema.types.UList;
import metashop.uschema.types.UPrimitiveType;
import org.apache.commons.lang3.StringUtils;
import org.neo4j.driver.Record;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MySqlSchemaGenerator {

    private static boolean migrateData = true;
    public static HashMap<String, List<String>> tablePrimaryKeys = new HashMap<>();

    public static void createMySQLSchemaFromUSchema(ArrayList<Record> incomingRelationships, ArrayList<Record> outgoingRelationships, USchemaModel uSchemaModel){
        HashMap<String, String> relationshipCardinality = calculateRelationshipCardinality(uSchemaModel.getuRelationships(), incomingRelationships, outgoingRelationships);
        for (UEntityType uEntity: uSchemaModel.getUEntities().values()) {
            createEntityTable(uEntity);
            if (migrateData){
                MySqlDataMigrator.migrarDatosPrueba(uEntity.getName(), MetaShopSchema.getDataEntity(uEntity.getName()));
            }
        }
        // Recorro otra vez la lista de entidades porque ya sé que están todas las tablas necesarias creadas
        for (UEntityType uEntity: uSchemaModel.getUEntities().values()) {
            ArrayList<UReference> references = uEntity.getUStructuralVariation().getReferences();
            for (UReference uReference: references) {
                switch (relationshipCardinality.get(uReference.getName())) {
                    case "1:1" -> {
                        addForeignKeyToTable(uEntity.getName(), uReference.getUEntityTypeDestination().getUStructuralVariation().getKey());
                        if (migrateData){
                            MySqlDataMigrator.migrarDatosRelaciones1To1(uEntity.getName(), MetaShopSchema.getDataRelationships(uReference.getName()));
                        }
                    }
                    case "1:N" -> {
                        addForeignKeyToTable(uReference.getUEntityTypeDestination().getName(), uEntity.getUStructuralVariation().getKey());
                        if (migrateData){
                            MySqlDataMigrator.migrarDatosRelaciones1ToN(uReference.getUEntityTypeDestination().getName(), MetaShopSchema.getDataRelationships(uReference.getName()));
                        }
                    }
                    case "N:M" -> {
                        String tableName = uEntity.getName() + "_" + StringUtils.lowerCase(uReference.getName()) + "_" + uReference.getUEntityTypeDestination().getName();
                        createRelationshipTable(tableName, uEntity, uReference.getUEntityTypeDestination(), uReference.getUStructuralVariationFeaturedBy());
                        if (migrateData){

                        }
                    }
                }

            }
        }
    }

    private static void createEntityTable(UEntityType uEntity){
        ArrayList<String> properties = new ArrayList<>();
        HashMap<String, UAttribute> attributes = uEntity.getUStructuralVariation().getAttributes();

        UKey key = uEntity.getUStructuralVariation().getKey();
        StringBuilder primaryKey = new StringBuilder();
        tablePrimaryKeys.put(uEntity.getName(), new ArrayList<>());
        key.getUAttributes().forEach(uAttribute -> {
            tablePrimaryKeys.get(uEntity.getName()).add(uAttribute.getName());
            primaryKey.append(uAttribute.getName()).append(",");
        });
        String pk = StringUtils.chop(primaryKey.toString());

        for (UAttribute uAttribute: uEntity.getUStructuralVariation().getKey().getUAttributes()) {
            properties.add(uAttribute.getName() + " " + transformAtributeTypeToMySQL((UPrimitiveType)uAttribute.getType()) + transformMandatoryToMySQL(uAttribute.isMandatory()));
        }
        for (UAttribute uAttribute: attributes.values()) {
            if (uAttribute.getType() instanceof UPrimitiveType){
                if (!uAttribute.getName().startsWith("__"))
                    properties.add(uAttribute.getName() + " " + transformAtributeTypeToMySQL((UPrimitiveType)uAttribute.getType()) + transformMandatoryToMySQL(uAttribute.isMandatory()));
            }
            else {
                // AQUÍ DEBERÍA CONTROLAR SI ESTOY MIGRANDO UNA COLECCIÓN
                //createCollectionAttributeTable(uEntity.getName(), pk, uAttribute);
            }
        }
        StringBuilder printPrimaryKey = new StringBuilder("PRIMARY KEY(");
        properties.add(printPrimaryKey.append(pk).append(")").toString());
        StringBuilder create = new StringBuilder();
        properties.forEach(property -> create.append(property).append(","));
        System.out.println("CREATE TABLE " + uEntity.getName() + "(" + StringUtils.chop(create.toString()) + ");");
    }

    private static void addForeignKeyToTable(String table, UKey foreignKey){
        String fkReference = "";
        String referencesTo = "";

        for (UAttribute uAttribute: foreignKey.getUAttributes()) {
            printAlterTableAddColumn(table, uAttribute.getName() + "Ref", transformAtributeTypeToMySQL((UPrimitiveType) uAttribute.getType()), transformMandatoryToMySQL(uAttribute.isMandatory()));
            fkReference += uAttribute.getName() + "Ref, ";
            referencesTo += uAttribute.getName() + ", ";
        }
        fkReference = StringUtils.substring(fkReference, 0,fkReference.length()-2);
        referencesTo = StringUtils.substring(referencesTo, 0,referencesTo.length()-2);
        printAlterTableForeignKey(table, fkReference, StringUtils.substring(foreignKey.getName(), 4), referencesTo);
    }

    private static void createRelationshipTable(String tableName, UEntityType originEntity, UEntityType destinationEntity, UStructuralVariation relationshipStructuralVariation){
        StringBuilder originPK = new StringBuilder();
        StringBuilder originPKWithoutType = new StringBuilder();

        for (UAttribute uAttribute: originEntity.getUStructuralVariation().getKey().getUAttributes()) {
            originPK.append(uAttribute.getName()).append(" ").append(transformAtributeTypeToMySQL((UPrimitiveType) uAttribute.getType())).append(",");
            originPKWithoutType.append(uAttribute.getName()).append(",");
        }

        StringBuilder destinationPK = new StringBuilder();
        StringBuilder destinationPKWithoutType = new StringBuilder();
        StringBuilder destinationPKWithoutRef = new StringBuilder();
        StringBuilder destinationFK = new StringBuilder();

        for (UAttribute uAttribute: destinationEntity.getUStructuralVariation().getKey().getUAttributes()) {
            destinationPKWithoutRef.append(uAttribute.getName()).append(",");
            destinationPK.append(uAttribute.getName()).append("Ref ").append(transformAtributeTypeToMySQL((UPrimitiveType) uAttribute.getType())).append(",");
            destinationPKWithoutType.append(uAttribute.getName()).append("Ref,");
            destinationFK.append(uAttribute.getName()).append("Ref,");
        }

        printCreateTable(tableName, originPK + StringUtils.chop(destinationPK.toString()), originPKWithoutType + StringUtils.chop(destinationPKWithoutType.toString()));
        printAlterTableForeignKey(tableName, StringUtils.chop(originPKWithoutType.toString()), originEntity.getName(), StringUtils.chop(originPKWithoutType.toString()));
        printAlterTableForeignKey(tableName, StringUtils.chop(destinationFK.toString()), destinationEntity.getName(), StringUtils.chop(destinationPKWithoutRef.toString()));

        for (UAttribute uAttribute: relationshipStructuralVariation.getAttributes().values()) {
            if (uAttribute.getType() instanceof UPrimitiveType) {
                printAlterTableAddColumn(tableName, uAttribute.getName(), transformAtributeTypeToMySQL((UPrimitiveType) uAttribute.getType()), transformMandatoryToMySQL(uAttribute.isMandatory()));
            }
            else {
                createCollectionAttributeTable(tableName, (originPK + StringUtils.chop(destinationPK.toString())), (originPKWithoutType + StringUtils.chop(destinationPKWithoutType.toString())), uAttribute);
            }
        }
    }

    private static void createCollectionAttributeTable(String tableName, String primaryKeyWithType, String primaryKey, UAttribute uAttribute){
        String tablePartialPrimaryKey = "position INT(50)," + uAttribute.getName() + " " + transformAtributeTypeToMySQL((UPrimitiveType) ((UList) uAttribute.getType()).getUType());
        printCreateTable(tableName+uAttribute.getName(), primaryKeyWithType + "," + tablePartialPrimaryKey, (primaryKey + "," + "position," + uAttribute.getName()));
    }

    private static void printAlterTableForeignKey(String tableName, String primaryKeysWithoutType, String referenceTableName, String primaryKeyWithoutRef){
        System.out.println("ALTER TABLE " + tableName + " ADD FOREIGN KEY(" + primaryKeysWithoutType + ") REFERENCES " + referenceTableName + "(" + primaryKeyWithoutRef + ");");
    }

    private static void printAlterTableAddColumn(String tableName, String attributeName, String attributeType, String attributeMandatory){
        System.out.println("ALTER TABLE " + tableName + " ADD COLUMN " + attributeName + " " + attributeType + " " + attributeMandatory + ";");
    }

    private static void printCreateTable(String tableName, String attributes, String primaryKey){
        System.out.println("CREATE TABLE " + tableName + "(" + attributes + ", PRIMARY KEY(" + primaryKey + "));");
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
            // Para un tipo de relación, si se cumple que del nodo origen solo sale una relación de ese tipo y para el nodo destino llegan varias relaciones de ese tipo, o viceversa
            // y además se cumple que la relación no tiene atributos, la cardinalidad es 1:N
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
