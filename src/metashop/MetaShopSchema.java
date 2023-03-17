package metashop;

import metashop.schema.graphdatamodel.model.GraphSchema;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public abstract class MetaShopSchema implements AutoCloseable{
    private static Driver driver;
    private final static String uri = "bolt://localhost:7687";
    private final static String user = "neo4j";
    private final static String password = "12345678";
    public final static List<String> types = List.of("Double", "Long", "Boolean", "String");


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

    @Override
    public void close(){
        driver.close();
    }

    public static void main(String... args) {
        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
        GraphSchema graphSchema = new GraphSchema("MetaShop", getNodes(), getRelationships());
        System.out.println(graphSchema);
    }

}