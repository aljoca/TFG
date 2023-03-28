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

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MySqlSchemaGenerator {

    public static HashMap<String, List<String>> tablePrimaryKeys = new HashMap<>();
    public static HashMap<String, List<String>> tableRelationshipAttributes = new HashMap<>();


    /**
     * Método para migrar el esquema y los datos de USchema a MySQL.
     * @param incomingRelationships ArrayList de relaciones entrantes a un nodo con su máxima cardinalidad.
     * @param outgoingRelationships ArrayList de relaciones salientes de un nodo con su máxima cardinalidad.
     * @param uSchemaModel Modelo USchema con el que realizar la migración.
     */
    public static void migrateSchemaAndDataFromNeo4jToMySql(ArrayList<Record> incomingRelationships, ArrayList<Record> outgoingRelationships, USchemaModel uSchemaModel, boolean migrateData){
        // Calculo la cardinalidad de las relaciones.
        HashMap<String, String> relationshipCardinality = calculateRelationshipCardinality(uSchemaModel.getuRelationships(), incomingRelationships, outgoingRelationships);
        // Por cada tipo de entidad creo una tabla.
        for (UEntityType uEntity: uSchemaModel.getUEntities().values()) {
            createEntityTable(uEntity);
            if (migrateData){
                MySqlDataMigrator.migrarDatosPrueba(uEntity.getName(), MetaShopSchema.getDataEntity(uEntity.getName()));
            }
        }
        // Recorro otra vez la lista de entidades porque ya sé que están todas las tablas necesarias creadas
        for (UEntityType uEntity: uSchemaModel.getUEntities().values()) {
            // Obtengo las referencias de la entidad que estamos recorriendo.
            ArrayList<UReference> references = uEntity.getUStructuralVariation().getReferences();
            for (UReference uReference: references) {
                // Para cada referencia, consultamos su cardinalidad. Dependiendo de dicho dato, sabremos si tenemos que crear una tabla intermedia
                // o simplemente añadir una referencia en la tabla correspondiente.
                switch (relationshipCardinality.get(uReference.getName())) {
                    case "1:1" -> {
                        // Si la cardinalidad es 1:1 quiere decir que debemos añadir una foreignKey en la tabla correspondiente a la entidad origen.
                        // p.e. User - RECOMMENDED_BY -> User | Un usuario sólo puede ser recomendado por otro usuario
                        addForeignKeyToTable(uEntity.getName(), uReference.getUEntityTypeDestination().getUStructuralVariation().getKey(), StringUtils.lowerCase(uReference.getName()));
                        if (migrateData){
                            MySqlDataMigrator.migrarDatosRelaciones1To1(uEntity.getName(), MetaShopSchema.getDataRelationships(uReference.getName()), "_" + StringUtils.lowerCase(uReference.getName()));
                        }
                    }
                    case "1:N" -> {
                        // Si la cardinalidad es 1:N quiere decir que debemos añadir una foreignKey en la tabla correspondiente a la entidad origen.
                        // p.e. User - ORDERS -> Order | Un usuario puede realizar 1 o más pedidos que solo van a pertenecer a él.
                        addForeignKeyToTable(uReference.getUEntityTypeDestination().getName(), uEntity.getUStructuralVariation().getKey(),  StringUtils.lowerCase(uReference.getName()));
                        if (migrateData){
                            MySqlDataMigrator.migrarDatosRelaciones1ToN(uReference.getUEntityTypeDestination().getName(), MetaShopSchema.getDataRelationships(uReference.getName()), "_" + StringUtils.lowerCase(uReference.getName()));
                        }
                    }
                    case "N:M" -> {
                        // Si la cardinalidad es N:M quiere decir que debemos crear una tabla intermedia para no repetir la información de las tablas implicadas en la relación.
                        // p.e. Product - IN_ORDER -> Order | Un producto puede estar incluído en un pedido o en muchos pedidos. Un pedido puede tener muchos productos.
                        // p.e. Product - CATEGORIZED -> ProductCategory | Un producto puede pertenecer a una categoría o a muchas categorías. Una categoría puede tener muchos productos.
                        String tableName = uEntity.getName() + "_" + StringUtils.lowerCase(uReference.getName()) + "_" + uReference.getUEntityTypeDestination().getName();
                        createRelationshipTable(tableName, uEntity, uReference.getUEntityTypeDestination(), uReference.getUStructuralVariationFeaturedBy(), "_" + StringUtils.lowerCase(uReference.getName()));
                        if (migrateData){
                            MySqlDataMigrator.migrarDatosRelacionesNToM(tableName, MetaShopSchema.getDataRelationships(uReference.getName()), "_" + StringUtils.lowerCase(uReference.getName()));
                        }
                    }
                }

            }
        }
    }

    /**
     *
     * @param uEntity
     */
    private static void createEntityTable(UEntityType uEntity){
        try {
            Statement stmt=MetaShopSchema.con.createStatement();

            // Genero la primaryKey (solo los nombres de la columna) que voy a añadir a la tabla.
            MySqlSchemaGenerator.generatePrimayKey(uEntity.getName(), uEntity.getUStructuralVariation().getKey());
            // Lista para guardar las primaryKeys con sus respectivos tipos.
            ArrayList<String> printPrimaryKeyList = new ArrayList<>();
            // Lista para guardar los atributos con sus respectivos tipos.
            ArrayList<String> printAttributesList = new ArrayList<>();
            // Lista para guardar las primaryKeys sin sus respectivos tipos.
            ArrayList<String> printPrimaryKeyWithoutTypeList = new ArrayList<>();

            // Recorro la lista de keys de la entidad, ya que puede ser una key compuesta.
            for (UAttribute uAttribute: uEntity.getUStructuralVariation().getKey().getUAttributes()) {
                /* TODO Ver qué pasaría cuando los atributos no son de tipo primitivo.
                    Según he visto, en MySQL un JSON no puede ser una clave, por lo que no debería tener en cuenta esto
                */
                printPrimaryKeyList.add(uAttribute.getName() + " " + transformAtributeTypeToMySQL((UPrimitiveType)uAttribute.getType()) + transformMandatoryToMySQL(uAttribute.isMandatory()));
                printPrimaryKeyWithoutTypeList.add(uAttribute.getName());
            }

            // Recorro la lista de atributos de la entidad.
            for (UAttribute uAttribute: uEntity.getUStructuralVariation().getAttributes().values()) {
                if (uAttribute.getType() instanceof UPrimitiveType){
                    // Si es de tipo primitivo, compruebo si empieza por "__" (significaría que es una key). Si no es una key, la añado a la lista de atributos
                    if (!uAttribute.getName().startsWith("__")) {
                        printAttributesList.add(uAttribute.getName() + " " + transformAtributeTypeToMySQL((UPrimitiveType) uAttribute.getType()) + transformMandatoryToMySQL(uAttribute.isMandatory()));
                    }
                }
                else {
                    // AQUÍ DEBERÍA CONTROLAR SI ESTOY MIGRANDO UNA COLECCIÓN
                    //createCollectionAttributeTable(uEntity.getName(), pk, uAttribute);
                    printAttributesList.add(uAttribute.getName() + " JSON" + transformMandatoryToMySQL(uAttribute.isMandatory()));
                }
            }
            String printAttributes = printAttributesList.isEmpty() ? "" : ", " + String.join(",", printAttributesList);
            System.out.println("CREATE TABLE " + uEntity.getName() + "(" + String.join(",",printPrimaryKeyList) + printAttributes + ", PRIMARY KEY(" + String.join(",", printPrimaryKeyWithoutTypeList) + "));");
            stmt.execute("CREATE TABLE " + uEntity.getName() + "(" + String.join(",",printPrimaryKeyList)  + printAttributes + ", PRIMARY KEY(" + String.join(",", printPrimaryKeyWithoutTypeList) + "));");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @param entityName
     * @param key
     */
    private static void generatePrimayKey(String entityName, UKey key) {
        // Añado una entrada en el HashMap de la entidad para guardar los nombres de sus primaryKeys
        tablePrimaryKeys.put(entityName, new ArrayList<>());
        key.getUAttributes().forEach(uAttribute -> {
            tablePrimaryKeys.get(entityName).add(uAttribute.getName());
        });
    }

    /**
     *
     * @param table
     * @param foreignK
     * @param relationshipName
     */
    private static void addForeignKeyToTable(String table, UKey foreignK, String relationshipName){
        ArrayList<String> foreignKeyAttributes = new ArrayList<>();
        ArrayList<String> reference = new ArrayList<>();

        for (UAttribute uAttribute: foreignK.getUAttributes()) {
            /* TODO Tendría que ver qué pasa si el tipo del atributo no es PrimitiveType.
                Según he visto, en MySQL un JSON no puede ser una clave, por lo que no debería tener en cuenta esto.
             */
            // Creo la variable para que quede más legible.
            String attributeName = uAttribute.getName() + "_" + relationshipName;
            // Creo una columna con el nombre del atributo seguido de "_" y el nombre de la relación.
            printAlterTableAddColumn(table, attributeName , transformAtributeTypeToMySQL((UPrimitiveType) uAttribute.getType()), transformMandatoryToMySQL(uAttribute.isMandatory()));

            // Añado el nombre del atributo con la nueva nomenclatura como foreignKey, y el nombre original del atributo como referencia.
            foreignKeyAttributes.add(attributeName);
            reference.add(uAttribute.getName());
        }
        // Creo las variables para que quede más legible.
        String foreignKey = String.join(",", foreignKeyAttributes);
        String referenceTableName = StringUtils.substring(foreignK.getName(), 4);
        String primaryKeyReference = String.join(",", reference);

        // Añado la foreignKey a la tabla correspondiente.
        printAlterTableForeignKey(table, foreignKey, referenceTableName , primaryKeyReference);
    }

    /**
     *
     * @param tableName
     * @param originEntity
     * @param destinationEntity
     * @param relationshipStructuralVariation
     * @param relationshipName
     */
    private static void createRelationshipTable(String tableName, UEntityType originEntity, UEntityType destinationEntity, UStructuralVariation relationshipStructuralVariation, String relationshipName){
        ArrayList<String> originAttributes = new ArrayList<>();
        ArrayList<String> originAttributesWithoutType = new ArrayList<>();
        MySqlSchemaGenerator.tableRelationshipAttributes.put(relationshipName, new ArrayList<>());

        for (UAttribute uAttribute: relationshipStructuralVariation.getAttributes().values()) {
            MySqlSchemaGenerator.tableRelationshipAttributes.get(relationshipName).add(uAttribute.getName());
        }

        for (UAttribute uAttribute: originEntity.getUStructuralVariation().getKey().getUAttributes()) {
            /* TODO Tendría que ver qué pasa si el tipo del atributo no es PrimitiveType.
                Según he visto, en MySQL un JSON no puede ser una clave, por lo que no debería tener en cuenta esto.
             */            originAttributes.add(uAttribute.getName() + " " + transformAtributeTypeToMySQL((UPrimitiveType) uAttribute.getType()));
            originAttributesWithoutType.add(uAttribute.getName());
        }

        ArrayList<String> destinationAttributes = new ArrayList<>();
        ArrayList<String> destinationAttributesWithoutType = new ArrayList<>();
        ArrayList<String> destinationPKReferences = new ArrayList<>();

        for (UAttribute uAttribute: destinationEntity.getUStructuralVariation().getKey().getUAttributes()) {
            destinationPKReferences.add(uAttribute.getName());
            /* TODO Tendría que ver qué pasa si el tipo del atributo no es PrimitiveType.
                Según he visto, en MySQL un JSON no puede ser una clave, por lo que no debería tener en cuenta esto.
             */            destinationAttributes.add(uAttribute.getName() + relationshipName + " " + transformAtributeTypeToMySQL((UPrimitiveType) uAttribute.getType()));
            destinationAttributesWithoutType.add(uAttribute.getName() + relationshipName);
        }
        // Creo las variables para que sea más legible
        String originPk = String.join(",", originAttributesWithoutType);
        String destinationFk = String.join(",", destinationAttributesWithoutType);
        String primaryKey = originPk + "," + destinationFk;
        String attributes = String.join(",", originAttributes) + "," + String.join(",", destinationAttributes);

        // Primero creo la tabla intermedia para la entidad origen y destino.
        printCreateTable(tableName, attributes, primaryKey);
        // Añado las foreignKey de la entidad origen
        printAlterTableForeignKey(tableName, originPk, originEntity.getName(), originPk);
        // Añado las foreignKey de la entidad destino
        printAlterTableForeignKey(tableName, destinationFk, destinationEntity.getName(), String.join(",", String.join(",", destinationPKReferences)));
        for (UAttribute uAttribute: relationshipStructuralVariation.getAttributes().values()) {
            if (uAttribute.getType() instanceof UPrimitiveType) {
                printAlterTableAddColumn(tableName, uAttribute.getName(), transformAtributeTypeToMySQL((UPrimitiveType) uAttribute.getType()), transformMandatoryToMySQL(uAttribute.isMandatory()));
            }
            else {
                printAlterTableAddColumn(tableName, uAttribute.getName(), "JSON", transformMandatoryToMySQL(uAttribute.isMandatory()));
            }
        }
    }

    /**
     *
     * @param tableName
     * @param attributes
     * @param primaryKey
     */
    private static void printCreateTable(String tableName, String attributes, String primaryKey){
        try {
            Statement stmt=MetaShopSchema.con.createStatement();
            System.out.println("CREATE TABLE " + tableName + "(" + attributes + ", PRIMARY KEY(" + primaryKey + "));");
            stmt.execute("CREATE TABLE " + tableName + "(" + attributes + ", PRIMARY KEY(" + primaryKey + "));");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Método genérico para añadir una foreign key a una tabla con sus respectivas referencias
     * @param tableName
     * @param foreignKey
     * @param referenceTableName
     * @param primaryKeyReference
     */
    private static void printAlterTableForeignKey(String tableName, String foreignKey, String referenceTableName, String primaryKeyReference){
        try {
            Statement stmt=MetaShopSchema.con.createStatement();
            System.out.println("ALTER TABLE " + tableName + " ADD FOREIGN KEY(" + foreignKey + ") REFERENCES " + referenceTableName + "(" + primaryKeyReference + ");");
            stmt.execute("ALTER TABLE " + tableName + " ADD FOREIGN KEY(" + foreignKey + ") REFERENCES " + referenceTableName + "(" + primaryKeyReference + ");");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Método genérico para insertar un atributo a una tabla.
     * @param tableName
     * @param attributeName
     * @param attributeType
     * @param attributeMandatory
     */
    private static void printAlterTableAddColumn(String tableName, String attributeName, String attributeType, String attributeMandatory){
        try {
            Statement stmt=MetaShopSchema.con.createStatement();
            System.out.println("ALTER TABLE " + tableName + " ADD COLUMN " + attributeName + " " + attributeType + " " + attributeMandatory + ";");
            stmt.execute("ALTER TABLE " + tableName + " ADD COLUMN " + attributeName + " " + attributeType + " " + attributeMandatory + ";");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @param relationshipTypes
     * @param incomingRelationships
     * @param outgoingRelationships
     * @return
     */
    public static HashMap<String, String> calculateRelationshipCardinality(HashMap<String, URelationshipType> relationshipTypes, ArrayList<Record> incomingRelationships, ArrayList<Record> outgoingRelationships){
        HashMap<String, String> relationshipsCardinality = new HashMap<>();
        // Obtengo la cardinalidad de las relaciones entrantes, es decir, el número máximo de relaciones que del mismo tipo de relación que llegan a un nodo.
        HashMap<String, Integer> incomingRel = getRelationshipCardinality(incomingRelationships);
        // Obtengo la cardinalidad de las relaciones salientes, es decir, el número máximo de relaciones que del mismo tipo de relación que salen de un nodo.
        HashMap<String, Integer> outgoingRel = getRelationshipCardinality(outgoingRelationships);

        // Para cada una de las relaciones compruebo su cardinalidad saliente y entrante.
        outgoingRel.keySet().forEach(relationship -> {
            int out = outgoingRel.get(relationship);
            int in = incomingRel.get(relationship);
            if ((out == 1 && in == 1) && relationshipTypes.get(relationship).getuStructuralVariation().getAttributes().isEmpty()){
                relationshipsCardinality.put(relationship, "1:1");
            }
            // Para un tipo de relación, si se cumple que del nodo origen solo sale una relación de ese tipo (out == 1) y para el nodo destino llegan varias relaciones de ese tipo (in > 1), o viceversa
            // y además se cumple que la relación no tiene atributos, la cardinalidad es 1:N
            else if (((out == 1 && in > 1) || (out > 1 && in == 1)) && relationshipTypes.get(relationship).getuStructuralVariation().getAttributes().isEmpty()){
                relationshipsCardinality.put(relationship, "1:N");
            }
            else {
                // Si no se cumple ninguno de los casos anteriores, inferimos que la cardinalidad es de muchos a muchos.
                relationshipsCardinality.put(relationship, "N:M");
            }
        });
        return  relationshipsCardinality;
    }

    /**
     *
     * @param relationshipsCardinality
     * @return
     */
    private static HashMap<String, Integer> getRelationshipCardinality(ArrayList<Record> relationshipsCardinality){
        HashMap<String, Integer> relationshipCardinality = new HashMap<>();
        relationshipsCardinality.forEach(incomingRelationship -> relationshipCardinality.put(incomingRelationship.values().get(0).asString(), incomingRelationship.values().get(1).asInt()));
        return relationshipCardinality;
    }

    /**
     *
     * @param mandatory
     * @return
     */
    public static String transformMandatoryToMySQL(boolean mandatory){
        return (mandatory ? "NOT NULL" : "");
    }

    /**
     *
     * @param type
     * @return
     */
    public static String transformAtributeTypeToMySQL(UPrimitiveType type){
        // TODO Estoy hay que revisarlo, evidentemente
        return switch (type.getName()) {
            case "Long" -> "INT(50)";
            case "Double" -> "FLOAT(10,2)";
            case "Boolean" -> "BOOL";
            case "String" -> "VARCHAR(200)";
            default -> null;
        };
    }
}
