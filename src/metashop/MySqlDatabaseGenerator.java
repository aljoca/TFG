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

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Clase para la generación del esquema MySQL y la correspondiente migración de datos partiendo de USchema.
 */
public class MySqlDatabaseGenerator {

    // HashMap para almacenar el nombre de las entidades con la lista de nombres de su clave primaria.
    public static HashMap<String, List<String>> entityPrimaryKeys = new HashMap<>();
    // HashMap para almacenar el nombre de las relaciones junto con la lista de nombres de sus atributos.
    public static HashMap<String, List<String>> relationshipAttributes = new HashMap<>();


    /**
     * Método para migrar el esquema y los datos de USchema a MySQL.
     *
     * @param incomingRelationships ArrayList de relaciones entrantes a un nodo con su máxima cardinalidad.
     * @param outgoingRelationships ArrayList de relaciones salientes de un nodo con su máxima cardinalidad.
     * @param uSchemaModel Modelo USchema con el que realizar la migración.
     */
    public static void migrateSchemaAndDataFromNeo4jToMySql(ArrayList<Record> incomingRelationships, ArrayList<Record> outgoingRelationships, USchemaModel uSchemaModel, boolean migrateData){
        // Calculo la cardinalidad de las relaciones.
        final HashMap<String, String> relationshipCardinality = calculateRelationshipCardinality(uSchemaModel.getuRelationships(), incomingRelationships, outgoingRelationships);
        // Por cada tipo de entidad creo una tabla.
        for (UEntityType uEntity: uSchemaModel.getUEntities().values()) {
            createEntityTable(uEntity);
            if (migrateData){
                MySqlDataMigrator.migrateEntityData(uEntity.getName(), MetaShopSchema.getDataEntity(uEntity.getName()));
            }
        }
        // Recorro otra vez la lista de entidades porque ya sé que están todas las tablas necesarias creadas
        for (UEntityType uEntity: uSchemaModel.getUEntities().values()) {
            // Obtengo las referencias de la entidad que estamos recorriendo.
            final ArrayList<UReference> references = uEntity.getUStructuralVariation().getReferences();
            // Para cada tipo de entidad, recorro sus referencias para ver qué tipo de relaciones tiene
            for (UReference uReference: references) {
                // Para cada referencia, consultamos su cardinalidad. Dependiendo de dicho dato, sabremos si tenemos que crear una tabla intermedia
                // o simplemente añadir una referencia en la tabla correspondiente.
                String relationshipName = "_" + StringUtils.lowerCase(uReference.getName());
                ArrayList<Record> relationships = MetaShopSchema.getDataRelationships(uReference.getName());
                switch (relationshipCardinality.get(uReference.getName())) {
                    case "1:1" -> {
                        // Si la cardinalidad es 1:1 quiere decir que debemos añadir una foreignKey en la tabla correspondiente a la entidad origen.
                        // p.e. User - RECOMMENDED_BY -> User | Un usuario sólo puede ser recomendado por otro usuario
                        addForeignKeyToTable(uEntity.getName(), uReference.getUEntityTypeDestination().getUStructuralVariation().getKey(), StringUtils.lowerCase(uReference.getName()));
                        if (migrateData){
                            MySqlDataMigrator.migrateRelationshipData1To1(uEntity.getName(), relationships, relationshipName);
                        }
                    }
                    case "1:N" -> {
                        // Si la cardinalidad es 1:N quiere decir que debemos añadir una foreignKey en la tabla correspondiente a la entidad origen.
                        // p.e. User - ORDERS -> Order | Un usuario puede realizar 1 o más pedidos que solo van a pertenecer a él.
                        addForeignKeyToTable(uReference.getUEntityTypeDestination().getName(), uEntity.getUStructuralVariation().getKey(),  StringUtils.lowerCase(uReference.getName()));
                        if (migrateData){
                            MySqlDataMigrator.migreateRelationshipData1ToN(uReference.getUEntityTypeDestination().getName(), relationships, relationshipName);
                        }
                    }
                    case "N:M" -> {
                        // Si la cardinalidad es N:M quiere decir que debemos crear una tabla intermedia para no repetir la información de las tablas implicadas en la relación.
                        // p.e. Product - IN_ORDER -> Order | Un producto puede estar incluído en un pedido o en muchos pedidos. Un pedido puede tener muchos productos.
                        // p.e. Product - CATEGORIZED -> ProductCategory | Un producto puede pertenecer a una categoría o a muchas categorías. Una categoría puede tener muchos productos.
                        final String tableName = uEntity.getName() + relationshipName + "_" + uReference.getUEntityTypeDestination().getName();
                        createRelationshipTable(tableName, uEntity, uReference.getUEntityTypeDestination(), uReference.getUStructuralVariationFeaturedBy(), relationshipName);
                        if (migrateData){
                            MySqlDataMigrator.migrateRelationshipDataNToM(tableName, relationships, relationshipName);
                        }
                    }
                }

            }
        }
    }

    /**
     * Método para crear la tabla correspondiente a una entidad de USchema.
     * @param uEntity Entidad de USchema.
     */
    private static void createEntityTable(UEntityType uEntity){
            // Genero la primaryKey (solo los nombres de la columna) que voy a añadir a la tabla.
            MySqlDatabaseGenerator.generatePrimaryKey(uEntity.getName(), uEntity.getUStructuralVariation().getKey());
            // Lista para guardar las primaryKeys con sus respectivos tipos.
            final ArrayList<String> primaryKey = new ArrayList<>();
            // Lista para guardar las primaryKeys sin sus respectivos tipos.
            final ArrayList<String> primaryKeyWithoutType = new ArrayList<>();
            // Lista para guardar los atributos con sus respectivos tipos.
            final ArrayList<String> attributesList = new ArrayList<>();

            // Recorro la lista de keys de la entidad, ya que puede ser una key compuesta.
            for (UAttribute uAttribute: uEntity.getUStructuralVariation().getKey().getUAttributes()) {
                primaryKey.add(uAttribute.getName() + " " + transformAtributeTypeToMySQL((UPrimitiveType)uAttribute.getType()) + isMandatoryToMySQL(uAttribute.isMandatory()));
                primaryKeyWithoutType.add(uAttribute.getName());
            }

            // Recorro la lista de atributos de la entidad.
            for (UAttribute uAttribute: uEntity.getUStructuralVariation().getAttributes().values()) {
                if (uAttribute.getType() instanceof UPrimitiveType){
                    // Si es de tipo primitivo, compruebo si empieza por "__" (significaría que es una key). Si no es una key, la añado a la lista de atributos
                    if (!uAttribute.getName().startsWith("__")) {
                        attributesList.add(uAttribute.getName() + " " + transformAtributeTypeToMySQL((UPrimitiveType) uAttribute.getType()) + isMandatoryToMySQL(uAttribute.isMandatory()));
                    }
                }
                else {
                    attributesList.add(uAttribute.getName() + " JSON" + isMandatoryToMySQL(uAttribute.isMandatory()));
                }
            }
            String attributes = attributesList.isEmpty() ? "" : ", " + String.join(",", attributesList);
            createTable(uEntity.getName(), String.join(",",primaryKey) + attributes, String.join(",", primaryKeyWithoutType));
    }

    /**
     * Método para generar una entrada de la colección de entidades con su respectiva lista de atributos que conforman la primary key.
     *
     * @param entityName Nombre de la entidad.
     * @param key Representación de la primary key en USchema.
     */
    private static void generatePrimaryKey(String entityName, UKey key) {
        // Añado una entrada en el HashMap de la entidad para guardar los nombres de sus primaryKeys
        entityPrimaryKeys.put(entityName, new ArrayList<>());
        key.getUAttributes().forEach(uAttribute -> {
            entityPrimaryKeys.get(entityName).add(uAttribute.getName());
        });
    }

    /**
     * Método para añadir una foreign key a una tabla.
     *
     * @param tableName Nombre de la tabla a la que se quiere añadir una foreign key.
     * @param foreignK Primary key que se quiere añadir como foreign key a la tabla.
     * @param relationshipName Nombre de la relación para la que se está generando la foreign key.
     */
    private static void addForeignKeyToTable(String tableName, UKey foreignK, String relationshipName){
        ArrayList<String> foreignKeyAttributes = new ArrayList<>();
        ArrayList<String> reference = new ArrayList<>();

        for (UAttribute uAttribute: foreignK.getUAttributes()) {
            // Creo la variable para que quede más legible.
            String attributeName = uAttribute.getName() + "_" + relationshipName;
            // Creo una columna con el nombre del atributo seguido de "_" y el nombre de la relación.
            alterTableAddColumn(tableName, attributeName , transformAtributeTypeToMySQL((UPrimitiveType) uAttribute.getType()), isMandatoryToMySQL(uAttribute.isMandatory()));

            // Añado el nombre del atributo con la nueva nomenclatura como foreignKey, y el nombre original del atributo como referencia.
            foreignKeyAttributes.add(attributeName);
            reference.add(uAttribute.getName());
        }
        // Creo las variables para que quede más legible.
        String foreignKey = String.join(",", foreignKeyAttributes);
        String referenceTableName = StringUtils.substring(foreignK.getName(), 4); // Hago esto porque foreignK tiene el formato "KEY_Entidad"
        String primaryKeyReference = String.join(",", reference);

        // Añado la foreignKey a la tabla correspondiente.
        alterTableForeignKey(tableName, foreignKey, referenceTableName , primaryKeyReference);
    }

    /**
     * Método para crear una tabla intermedia.
     *
     * @see UEntityType
     * @see UStructuralVariation
     * @param tableName Nombre de la tabla intermedia.
     * @param originEntity Representación de la entidad origen en USchema.
     * @param destinationEntity Representación de la entidad destino en USchema.
     * @param relationshipStructuralVariation Structural Variation de la relación para la que se está generando la tabla.
     * @param relationshipName Nombre de la relación para la que se está generando la tabla.
     */
    private static void createRelationshipTable(String tableName, UEntityType originEntity, UEntityType destinationEntity, UStructuralVariation relationshipStructuralVariation, String relationshipName){
        ArrayList<String> originAttributes = new ArrayList<>();
        ArrayList<String> originAttributesWithoutType = new ArrayList<>();
        MySqlDatabaseGenerator.relationshipAttributes.put(relationshipName, new ArrayList<>());

        for (UAttribute uAttribute: relationshipStructuralVariation.getAttributes().values()) {
            MySqlDatabaseGenerator.relationshipAttributes.get(relationshipName).add(uAttribute.getName());
        }

        for (UAttribute uAttribute: originEntity.getUStructuralVariation().getKey().getUAttributes()) {
            originAttributes.add(uAttribute.getName() + " " + transformAtributeTypeToMySQL((UPrimitiveType) uAttribute.getType()));
            originAttributesWithoutType.add(uAttribute.getName());
        }

        ArrayList<String> destinationAttributes = new ArrayList<>();
        ArrayList<String> destinationAttributesWithoutType = new ArrayList<>();
        ArrayList<String> destinationPKReferences = new ArrayList<>();

        for (UAttribute uAttribute: destinationEntity.getUStructuralVariation().getKey().getUAttributes()) {
            destinationPKReferences.add(uAttribute.getName());
            destinationAttributes.add(uAttribute.getName() + relationshipName + " " + transformAtributeTypeToMySQL((UPrimitiveType) uAttribute.getType()));
            destinationAttributesWithoutType.add(uAttribute.getName() + relationshipName);
        }
        // Creo las variables para que sea más legible
        String originPk = String.join(",", originAttributesWithoutType);
        String destinationFk = String.join(",", destinationAttributesWithoutType);
        String primaryKey = originPk + "," + destinationFk;
        String attributes = String.join(",", originAttributes) + "," + String.join(",", destinationAttributes);

        // Primero creo la tabla intermedia para la entidad origen y destino.
        createTable(tableName, attributes, primaryKey);
        // Añado las foreignKey de la entidad origen
        alterTableForeignKey(tableName, originPk, originEntity.getName(), originPk);
        // Añado las foreignKey de la entidad destino
        alterTableForeignKey(tableName, destinationFk, destinationEntity.getName(), String.join(",", String.join(",", destinationPKReferences)));
        for (UAttribute uAttribute: relationshipStructuralVariation.getAttributes().values()) {
            if (uAttribute.getType() instanceof UPrimitiveType) {
                alterTableAddColumn(tableName, uAttribute.getName(), transformAtributeTypeToMySQL((UPrimitiveType) uAttribute.getType()), isMandatoryToMySQL(uAttribute.isMandatory()));
            }
            else {
                alterTableAddColumn(tableName, uAttribute.getName(), "JSON", isMandatoryToMySQL(uAttribute.isMandatory()));
            }
        }
    }

    /**
     * Método para la creación de una tabla.
     *
     * @param tableName Nombre de la tabla a crear.
     * @param attributes Nombres de las columnas de la tabla.
     * @param primaryKey Atributos por los que está compuesta la primary key.
     */
    private static void createTable(String tableName, String attributes, String primaryKey){
        try {
            Statement stmt=MetaShopSchema.con.createStatement();
            System.out.println("CREATE TABLE " + tableName + "(" + attributes + ", PRIMARY KEY(" + primaryKey + "));");
            stmt.execute("CREATE TABLE " + tableName + "(" + attributes + ", PRIMARY KEY(" + primaryKey + "));");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Método genérico para añadir una foreign key a una tabla con sus respectivas referencias.
     *
     * @param tableName Nombre de la tabla a la que se va a insertar la foreign key.
     * @param foreignKey Nombre de la foreign key a insertar. Puede estar compuesta de varios atributos.
     * @param referenceTableName Nombre de la tabla a la que referencia la foreign key.
     * @param primaryKeyReference Nombre de la primary key a la que va a hacer referencia la foreign key.
     */
    private static void alterTableForeignKey(String tableName, String foreignKey, String referenceTableName, String primaryKeyReference){
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
     *
     * @param tableName Nombre de la tabla a la que se va a insertar la columna.
     * @param attributeName Nombre de la columna.
     * @param attributeType Tipo de la columna.
     * @param attributeMandatory Obligatoriedad de rellenar la columna.
     */
    private static void alterTableAddColumn(String tableName, String attributeName, String attributeType, String attributeMandatory){
        try {
            Statement stmt=MetaShopSchema.con.createStatement();
            System.out.println("ALTER TABLE " + tableName + " ADD COLUMN " + attributeName + " " + attributeType + " " + attributeMandatory + ";");
            stmt.execute("ALTER TABLE " + tableName + " ADD COLUMN " + attributeName + " " + attributeType + " " + attributeMandatory + ";");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Método para calcular la cardinalidad total de una relación.
     *
     * @param relationshipTypes HashMap de con el nombre de la relación y su tipo correspondiente en USchema.
     * @param incomingRelationships Relaciones que entran a un nodo con su cardinalidad correspondiente.
     * @param outgoingRelationships Relaciones que salen de un nodo con su cardinalidad correspondiente.
     * @return HashMap con el nombre de la relación y su cardinalidad total.
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
     * Método para obtener la cardinalidad de una relación de entrada o salida.
     *
     * @param relationshipsCardinality Relación con su cardinalidad.
     * @return Relaciones con su cardinalidad. Dependiendo del parámetro del método, devuelve la cardinalidad de salida o de entrada de una relación.
     */
    private static HashMap<String, Integer> getRelationshipCardinality(ArrayList<Record> relationshipsCardinality){
        HashMap<String, Integer> relationshipCardinality = new HashMap<>();
        relationshipsCardinality.forEach(relationship -> relationshipCardinality.put(relationship.values().get(0).asString(), relationship.values().get(1).asInt()));
        return relationshipCardinality;
    }

    /**
     *  Método para añadir la sentencia necesaria cuando un atributo no es obligatorio.
     *
     * @param mandatory Indica si el atributo es necesario o no.
     * @return Sentencia MySQL.
     */
    public static String isMandatoryToMySQL(boolean mandatory){
        return (mandatory ? " NOT NULL" : "");
    }

    /**
     * Método para calcular el tipo primitivo correspondiente en MySQL.
     * @param type Tipo de USchema.
     * @return Tipo válido en MySQL.
     */
    public static String transformAtributeTypeToMySQL(UPrimitiveType type){
        return switch (type.getName()) {
            case "Long" -> "INT(50)";
            case "Double" -> "FLOAT(10,2)";
            case "Boolean" -> "BOOL";
            case "String" -> "VARCHAR(200)";
            default -> null;
        };
    }
}
