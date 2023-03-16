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
    public final static List<String> types = List.of("float", "integer", "boolean", "string");


    /**
     * Método para la obtención de todos los nodos existentes en la BBDD
     * @return ArrayList de nodos
     */
    private static ArrayList<Record> getNodes() {
        try (Session session = driver.session()) {
            return session.executeWrite(tx -> {
                /*
                    Explicación de la siguiente query en Cypher:
                        Obtengo los nodos con sus labels y propiedades.
                        Agrupo las labels y elimino los duplicados que aparecen al unir todas las propiedades.
                        Devuelvo una tabla tipo-propiedades.

                        UNION

                        Obtengo los nodos que no tienen propiedades
                        Devuelvo una tabla tipo-propiedades
                */
                Query query = new Query("""
                        MATCH (n)
                        WITH labels(n) as tipo, keys(n) as propiedades, n
                        UNWIND propiedades AS propiedad
                        WITH tipo, propiedad, n[propiedad] AS valor
                        WITH tipo, propiedad, valor,
                        CASE
                             WHEN valor IS NULL THEN 'null'
                             WHEN (valor + '') =~ '^-?[0-9]+$' THEN 'integer'
                             WHEN (valor + '') =~ '^-?[0-9]+\\.[0-9]+$' THEN 'float'
                             WHEN toBoolean(valor) IS NOT NULL THEN 'boolean'
                             ELSE 'string'
                             END AS tipo_dato
                        RETURN reduce(s = '', x IN tipo | s + x) AS tipoNodo, tipo as labels, collect(DISTINCT [propiedad, tipo_dato]) AS propiedades_tipo_dato              
                        UNION
                        MATCH (n)
                        WHERE size(keys(n)) = 0
                        WITH labels(n) as tipo, [] as propiedades_tipo_dato
                        return  reduce(s = '', x IN tipo | s + x) AS tipoNodo, tipo as labels, propiedades_tipo_dato
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
                /*
                    Explicación de la siguiente query en Cypher:
                        Obtengo las relaciones con sus tipos y propiedades.
                        Agrupo las labels y elimino los duplicados que aparecen al unir todas las propiedades.
                        Devuelvo una tabla tipo-propiedades.

                        UNION

                        Obtengo los nodos que no tienen propiedades
                        Devuelvo una tabla tipo-propiedades
                 */
                Query query = new Query("""
                        MATCH ()-[r]->()
                        WITH type(r) as tipo, keys(r) as propiedades, r
                        UNWIND propiedades AS propiedad
                        WITH tipo, propiedad, r[propiedad] AS valor, startNode(r) as origen, endNode(r) as destino
                        WITH tipo, propiedad, valor, origen, destino,
                             CASE
                                  WHEN valor IS NULL THEN 'null'
                                  WHEN (valor + '') =~ '^-?[0-9]+$' THEN 'integer'
                                  WHEN (valor + '') =~ '^-?[0-9]+\\.[0-9]+$' THEN 'float'
                                  WHEN toBoolean(valor) IS NOT NULL THEN 'boolean'
                                  ELSE 'string'
                             END AS tipo_dato
                        RETURN tipo, collect(DISTINCT [propiedad, tipo_dato]) AS propiedades_tipo_dato, reduce(s = '', x IN labels(origen) | s + x) AS origin, reduce(s = '', x IN labels(destino) | s + x) AS destination
                        UNION
                        MATCH ()-[r]->()
                        WHERE size(keys(r)) = 0
                        WITH type(r) as tipo, [] as propiedades_tipo_dato, startNode(r) as origen, endNode(r) as destino
                        RETURN tipo, propiedades_tipo_dato, reduce(s = '', x IN labels(origen) | s + x) AS origin, reduce(s = '', x IN labels(destino) | s + x) AS destination
                                                
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