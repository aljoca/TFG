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


    /**
     * Método para migrar el esquema y los datos de USchema a MySQL.
     * @param incomingRelationships ArrayList de relaciones entrantes a un nodo con su máxima cardinalidad.
     * @param outgoingRelationships ArrayList de relaciones salientes de un nodo con su máxima cardinalidad.
     * @param uSchemaModel Modelo USchema con el que realizar la migración.
     */
    public static void migrateSchemaAndDataNeo4jToMySql(ArrayList<Record> incomingRelationships, ArrayList<Record> outgoingRelationships, USchemaModel uSchemaModel, boolean migrateData){
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

    private static void createEntityTable(UEntityType uEntity){
        try {
            Statement stmt=MetaShopSchema.con.createStatement();
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
            stmt.execute("CREATE TABLE " + uEntity.getName() + "(" + StringUtils.chop(create.toString()) + ");");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void addForeignKeyToTable(String table, UKey foreignKey, String relationshipName){
        String fkReference = "";
        String referencesTo = "";

        for (UAttribute uAttribute: foreignKey.getUAttributes()) {
            printAlterTableAddColumn(table, uAttribute.getName() + "_" + relationshipName, transformAtributeTypeToMySQL((UPrimitiveType) uAttribute.getType()), transformMandatoryToMySQL(uAttribute.isMandatory()));
            fkReference += uAttribute.getName() + "_" + relationshipName + ", ";
            referencesTo += uAttribute.getName() + ", ";
        }
        fkReference = StringUtils.substring(fkReference, 0,fkReference.length()-2);
        referencesTo = StringUtils.substring(referencesTo, 0,referencesTo.length()-2);
        printAlterTableForeignKey(table, fkReference, StringUtils.substring(foreignKey.getName(), 4), referencesTo);
    }

    private static void createRelationshipTable(String tableName, UEntityType originEntity, UEntityType destinationEntity, UStructuralVariation relationshipStructuralVariation, String relationshipName){
        StringBuilder originPK = new StringBuilder();
        StringBuilder originPKWithoutType = new StringBuilder();
        StringBuilder originPkWithoutRef = new StringBuilder();

        for (UAttribute uAttribute: originEntity.getUStructuralVariation().getKey().getUAttributes()) {
            originPK.append(uAttribute.getName()).append(" ").append(transformAtributeTypeToMySQL((UPrimitiveType) uAttribute.getType())).append(",");
            originPkWithoutRef.append(uAttribute.getName()).append(",");
            originPKWithoutType.append(uAttribute.getName()).append(",");
        }

        StringBuilder destinationPK = new StringBuilder();
        StringBuilder destinationPKWithoutType = new StringBuilder();
        StringBuilder destinationPKWithoutRef = new StringBuilder();
        StringBuilder destinationFK = new StringBuilder();

        for (UAttribute uAttribute: destinationEntity.getUStructuralVariation().getKey().getUAttributes()) {
            destinationPKWithoutRef.append(uAttribute.getName()).append(",");
            destinationPK.append(uAttribute.getName()).append(relationshipName).append(" ").append(transformAtributeTypeToMySQL((UPrimitiveType) uAttribute.getType())).append(",");
            destinationPKWithoutType.append(uAttribute.getName()).append(relationshipName).append(",");
            destinationFK.append(uAttribute.getName()).append(relationshipName).append(",");
        }

        printCreateTable(tableName, originPK + StringUtils.chop(destinationPK.toString()), originPKWithoutType + StringUtils.chop(destinationPKWithoutType.toString()));
        printAlterTableForeignKey(tableName, StringUtils.chop(originPKWithoutType.toString()), originEntity.getName(), StringUtils.chop(originPkWithoutRef.toString()));
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

    /**
     * Método genérico para añadir una foreign key a una tabla con sus respectivas referencias
     * @param tableName
     * @param primaryKeysWithoutType
     * @param referenceTableName
     * @param primaryKeyWithoutRef
     */
    private static void printAlterTableForeignKey(String tableName, String primaryKeysWithoutType, String referenceTableName, String primaryKeyWithoutRef){
        try {
            Statement stmt=MetaShopSchema.con.createStatement();
            System.out.println("ALTER TABLE " + tableName + " ADD FOREIGN KEY(" + primaryKeysWithoutType + ") REFERENCES " + referenceTableName + "(" + primaryKeyWithoutRef + ");");
            stmt.execute("ALTER TABLE " + tableName + " ADD FOREIGN KEY(" + primaryKeysWithoutType + ") REFERENCES " + referenceTableName + "(" + primaryKeyWithoutRef + ");");
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

    private static void printCreateTable(String tableName, String attributes, String primaryKey){
        try {
            Statement stmt=MetaShopSchema.con.createStatement();
            System.out.println("CREATE TABLE " + tableName + "(" + attributes + ", PRIMARY KEY(" + primaryKey + "));");
            stmt.execute("CREATE TABLE " + tableName + "(" + attributes + ", PRIMARY KEY(" + primaryKey + "));");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static HashMap<String, String> calculateRelationshipCardinality(HashMap<String, URelationshipType> relationshipTypes, ArrayList<Record> incomingRelationships, ArrayList<Record> outgoingRelationships){
        HashMap<String, String> relationshipsCardinality = new HashMap<>();
        HashMap<String, Integer> incoming = new HashMap<>();
        HashMap<String, Integer> outgoing = new HashMap<>();
        // Obtengo la cardinalidad de las relaciones entrantes, es decir, el número máximo de relaciones que del mismo tipo de relación que llegan a un nodo.
        incomingRelationships.forEach(incomingRelationship -> incoming.put(incomingRelationship.values().get(0).asString(), incomingRelationship.values().get(1).asInt()));
        // Obtengo la cardinalidad de las relaciones salientes, es decir, el número máximo de relaciones que del mismo tipo de relación que salen de un nodo.
        outgoingRelationships.forEach(outgoingRelationship -> outgoing.put(outgoingRelationship.values().get(0).asString(), outgoingRelationship.values().get(1).asInt()));
        // Para cada una de las relaciones compruebo su cardinalidad saliente y entrante.
        outgoing.keySet().forEach(relationship -> {
            int out = outgoing.get(relationship);
            int in = incoming.get(relationship);
            if (out == 1 && in == 1){
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

    public static String transformMandatoryToMySQL(boolean mandatory){
        return (mandatory ? "NOT NULL" : "");
    }

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
