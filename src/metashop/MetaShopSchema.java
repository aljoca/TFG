package metashop;

import metashop.graphdatamodel.GraphSchemaModel;
import metashop.graphdatamodel.Label;
import metashop.graphdatamodel.type.PrimitiveType;
import metashop.uschema.USchemaModel;
import metashop.uschema.entities.UEntityType;
import metashop.uschema.features.UAttribute;
import metashop.uschema.features.UFeature;
import metashop.uschema.types.UPrimitiveType;
import metashop.uschema.types.UType;
import org.apache.commons.lang3.StringUtils;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.types.Node;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class MetaShopSchema implements AutoCloseable{
    private static Driver driver;
    private final static String uri = "bolt://localhost:7687";
    private final static String user = "neo4j";
    private final static String password = "12345678";
    public final static List<String> types = List.of("Double", "Long", "Boolean", "String");
    public static Connection con;



    /**
     * Método para la obtención de todos los nodos existentes en la BBDD
     * @return ArrayList de nodos
     */
    private static ArrayList<Record> getNodes() {
        try (Session session = driver.session()) {
            return session.executeWrite(tx -> {
                Query query = new Query("""
                        CALL apoc.meta.nodeTypeProperties() YIELD nodeLabels, propertyName, propertyTypes, mandatory
                        WITH nodeLabels, collect([propertyName, propertyTypes[0],mandatory]) as type_data_properties
                        RETURN reduce(s = '', x IN nodeLabels | s + x) as nodeName, nodeLabels AS nodeType, type_data_properties AS properties
                        """);
                Result resultNodes = tx.run(query);
                return new ArrayList<>(resultNodes.list());
            });
        }
    }

    /**
     * Método para la obtención de todas las relaciones existentes en la BBDD
     * Esta consulta tiene la particulareidad de que cada elemento contiene el nodo origen, el nodo destino y la relación.
     * Haciendo solo la query de las relaciones sería más costoso, ya que solo vienen las referencias de los nodos relacionados.
     * @return ArrayList de relaciones
     */
    private static ArrayList<Record> getRelationships(){
        try (Session session = driver.session()) {
            return session.executeWrite(tx -> {
                Query query = new Query("""
                        CALL apoc.meta.relTypeProperties() YIELD relType, sourceNodeLabels, targetNodeLabels, propertyName, propertyTypes, mandatory
                        WITH relType,  reduce(s = '', x IN sourceNodeLabels | s + x) as sourceNodeName,
                        reduce(s = '', x IN targetNodeLabels | s + x) as targetNodeName, [val IN collect([propertyName, propertyTypes[0] ,mandatory]) WHERE val[0] IS NOT null] as properties
                        RETURN apoc.text.replace(relType, '[`:]', '') as relType, sourceNodeName, targetNodeName, properties               
                        """);
                Result result = tx.run(query);
                return new ArrayList<>(result.list());
            });
        }
    }

    private static ArrayList<Record> getRelationshipsCardinality(){
        try (Session session = driver.session()) {
            return session.executeWrite(tx -> {
                Query query = new Query("""
                        MATCH (n)-[r]->()
                        WITH id(n) AS id_nodo, labels(n) AS etiquetas, type(r) AS tipo_relacion, COUNT(*) AS cardinalidad
                        UNWIND etiquetas AS etiqueta
                        WITH etiqueta, tipo_relacion, MAX(cardinalidad) AS max_cardinalidad
                        RETURN etiqueta, tipo_relacion, max_cardinalidad            
                        """);
                Result result = tx.run(query);
                return new ArrayList<>(result.list());
            });
        }
    }

    private static ArrayList<Record> getIncomingRelationships(){
        try (Session session = driver.session()) {
            return session.executeWrite(tx -> {
                Query query = new Query("""
                        // Relaciones entrantes de un NODO
                        MATCH ()-[r]->(n)
                        WITH n, type(r) AS rel_type, count(r) AS count
                        return DISTINCT rel_type, max(count)          
                        """);
                Result result = tx.run(query);
                return new ArrayList<>(result.list());
            });
        }
    }

    private static ArrayList<Record> getOutgoingRelationships(){
        try (Session session = driver.session()) {
            return session.executeWrite(tx -> {
                Query query = new Query("""
                        // Relaciones salientes de un NODO
                        MATCH (n)-[r]->()
                        WITH n, type(r) AS rel_type, count(r) AS count
                        return DISTINCT rel_type, max(count)
                                  
                        """);
                Result result = tx.run(query);
                return new ArrayList<>(result.list());
            });
        }
    }


    private static ArrayList<Record> getDataNodes(String condition){
        try (Session session = driver.session()) {
            return session.executeWrite(tx -> {
                Query query = new Query("MATCH (n) WHERE (" + condition + ") WITH labels(n) AS Etiquetas, [prop in keys(n) | {name: prop, value: n[prop]}] AS Atributos  RETURN Etiquetas, Atributos");
                Result result = tx.run(query);
                return new ArrayList<>(result.list());
            });
        }
    }


//    private static void migrateData(GraphSchemaModel graphSchemaModel, USchemaModel uSchemaModel){
//
//        for (String entityName: uSchemaModel.getuEntities().keySet()) {
//            StringBuilder condition = new StringBuilder();
//            ArrayList<Label> labels = graphSchemaModel.getEntities().get(entityName).getLabels();
//            ArrayList<Record> dataNodes;
//            if (labels.size() > 1) {
//                labels.forEach(label -> condition.append("n:").append(label.getName()).append(" AND "));
//                String finalCondition = StringUtils.substring(condition.toString(), 0, condition.length() - 4);
//                dataNodes = getDataNodes(finalCondition);
//            }
//            else {
//                dataNodes = getDataNodes("n:" + labels.get(0).getName() + " AND size(labels(n)) < 2");
//            }
//        }
//    }

    public static ArrayList<Record> getDataEntity(String label){
        try (Session session = driver.session()) {
            return session.executeWrite(tx -> {
                Query query = new Query("MATCH (u:" + label + ") return u");
                Result result = tx.run(query);
                return new ArrayList<>(result.list());
            });
        }
    }

    public static ArrayList<Record> getDataRelationships(String relationshipName){
        try (Session session = driver.session()) {
            return session.executeWrite(tx -> {
                Query query = new Query("MATCH (n)-[r:" + relationshipName + "]->(m) return n, m, r");
                Result result = tx.run(query);
                return new ArrayList<>(result.list());
            });
        }
    }

    public static String appendLabels(Node node){
        StringBuilder labels = new StringBuilder();
        node.labels().forEach(labels::append);
        return labels.toString();
    }


    @Override
    public void close(){
        driver.close();
    }

    public static void main(String... args) {
        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
        // Obtenemos un esquema general de Neo4J para poder trabajar con USchema.
        GraphSchemaModel graphSchema = new GraphSchemaModel("MetaShop", getNodes(), getRelationships(), getRelationshipsCardinality());
        System.out.println(graphSchema);

        // Obtenemos el USchema resultante del esquema general que hemos obtenido estudiando la estructura de los datos en Neo4J
        USchemaModel uSchemaModel = new USchemaModel(graphSchema);
        System.out.println(uSchemaModel);
        try {
            con= DriverManager.getConnection("jdbc:mysql://localhost:3306/migracion1","root","12345678");
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Migramos USchema al esquema relacional MySQL, así como sus datos si el flag "migrateData" está a true
            MySqlSchemaGenerator.migrateSchemaAndDataNeo4jToMySql(getIncomingRelationships(), getOutgoingRelationships(), uSchemaModel, true);
            con.close();
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

}