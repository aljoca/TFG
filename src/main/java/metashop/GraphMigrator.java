package metashop;

import metashop.graphdatamodel.GraphSchemaModel;
import metashop.uschema.USchemaModel;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.types.Node;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class GraphMigrator implements AutoCloseable{
    private static Driver driver;
    private final static String uri = "neo4j://127.0.0.1:7687";
    private final static String user = "neo4j";
    private final static String password = "12345678";
    public final static List<String> types = List.of("Double", "Long", "Boolean", "String", "Date");
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

    private static ArrayList<Record> getIncomingRelationships(){
        try (Session session = driver.session()) {
            return session.executeWrite(tx -> {
                Query query = new Query("""
                        // Relaciones entrantes de un NODO
                        MATCH ()-[r]->(n)
                        WITH labels(n) as l, n, type(r) AS relType, count(r) AS count
                        UNWIND l as label
                        return DISTINCT label, relType, max(count)          
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
                        WITH labels(n) as l, n, type(r) AS relType, count(r) AS count
                        UNWIND l as label
                        return DISTINCT label, relType, max(count)
                        """);
                Result result = tx.run(query);
                return new ArrayList<>(result.list());
            });
        }
    }

    public static ArrayList<Record> getDataEntity(String labels, int labelsSize){
        try (Session session = driver.session()) {
            return session.executeWrite(tx -> {
                Query query = new Query("MATCH (n) WHERE all(label IN [" + labels + "] WHERE label IN labels(n)) AND size(labels(n)) = " + labelsSize + " RETURN n");
                Result result = tx.run(query);
                return new ArrayList<>(result.list());
            });
        }
    }

    /**
     * Método para realizar la búsqueda de nodos y relaciones.
     * @param relationshipName Nombre de la relación.
     * @param labelsOrigin Etiquetas del nodo origen formateadas para el match.
     * @param labelsDestination Etiquetas del nodo destino formateadas para el match.
     * @param labelsArrayOrigin Etiquetas del nodo origen formateadas para el where.
     * @param labelsArrayDestination Etiquetas del nodo destino formateadas para el where.
     * @param labelsSizeOrigin Número de etiquetas del nodo origen
     * @param labelsSizeDestination Número de etiquetas del nodo destino
     * @return Lista de nodos y relaciones
     */
    public static ArrayList<Record> getDataRelationships(String relationshipName, String labelsOrigin, String labelsDestination, String labelsArrayOrigin,
                                                         String labelsArrayDestination, int labelsSizeOrigin, int labelsSizeDestination){
        try (Session session = driver.session()) {
            return session.executeWrite(tx -> {
                Query query = new Query("MATCH (n" + labelsOrigin + ")-[r:" + relationshipName + "]->(m" + labelsDestination +
                        ") WHERE all(label IN [ " + labelsArrayOrigin + "] WHERE label IN labels(n)) AND size(labels(n)) = " + labelsSizeOrigin + " " +
                        "AND all(label IN [ " + labelsArrayDestination + "] WHERE label IN labels(m)) AND size(labels(m)) = " + labelsSizeDestination + " return n, m, r");
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
        //driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
        driver = GraphDatabase.driver(uri);

        // Obtenemos un esquema general de Neo4J para poder trabajar con USchema.
        GraphSchemaModel graphSchema = GraphSchemaModel.getGraphSchemaModel(args[0], getNodes(), getRelationships());
        System.out.println(graphSchema);

        // Obtenemos el USchema resultante del esquema general que hemos obtenido estudiando la estructura de los datos en Neo4J
        USchemaModel uSchemaModel = USchemaModel.getUSchemaModel(graphSchema);
        System.out.println(uSchemaModel);
        try {
            //con= DriverManager.getConnection("jdbc:mysql://localhost:3306/migracion1","root","12345678");
            con= DriverManager.getConnection("jdbc:mysql://localhost:3306/migracion1", "root", "12345678");

            Class.forName("com.mysql.cj.jdbc.Driver");
            // Migramos USchema al esquema relacional MySQL, así como sus datos si el flag "migrateData" está a true
            final HashMap<String, String> relationshipsCardinality = MySqlMigrationUtils.calculateRelationshipCardinality(uSchemaModel.getuRelationships(), getIncomingRelationships(), getOutgoingRelationships());
            MySqlSchemaGenerator.migrateToMySqlSchema(relationshipsCardinality, uSchemaModel);
            MySqlDataMigrator.migrateDataToMySql(relationshipsCardinality, uSchemaModel, graphSchema);
            con.close();
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

}