package metashop;

import metashop.uschema.USchemaModel;
import metashop.uschema.entities.UEntityType;
import metashop.uschema.features.UAttribute;
import metashop.uschema.features.UKey;
import metashop.uschema.types.UPrimitiveType;

import java.util.ArrayList;
import java.util.HashMap;

public class MySqlSchemaGenerator {

    public static void createMySQLSchemaFromUSchema(USchemaModel uSchemaModel){
        for (UEntityType uEntity: uSchemaModel.getuEntities().values()) {
            ArrayList<String> properties = new ArrayList<>();
            HashMap<String, UAttribute> attributes = uEntity.getUStructuralVariation().getAttributes();
            UKey key = uEntity.getUStructuralVariation().getKey();

            for (UAttribute uAttribute: attributes.values()) {
                    properties.add(uAttribute.getName() + " " + transformAtributeTypeToMySQL((UPrimitiveType)uAttribute.getType()) + transformMandatoryToMySQL(uAttribute.isMandatory()));
            }
            StringBuilder primaryKey = new StringBuilder("PRIMARY KEY(");
            for (UAttribute uAttribute: key.getUAttributes()) {
                primaryKey.append(uAttribute.getName()).append(",");
            }
            String pk = primaryKey.substring(0, primaryKey.length()-1);
            properties.add(pk + ")");
            StringBuilder create = new StringBuilder("CREATE TABLE " + uEntity.getName() + "(");
            for (String property: properties) {
                create.append(property).append(",");
            }
            String createTable = create.subSequence(0, create.length()-1) + ");";
            System.out.println(createTable);
        }
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
