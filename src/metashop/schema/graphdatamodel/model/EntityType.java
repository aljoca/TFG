package metashop.schema.graphdatamodel.model;

import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.Node;

import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;


public class EntityType {

    private final ArrayList<Label> labels;
    private final ArrayList<StructuralVariation> structuralVariations;
    private final String name;


    public EntityType(Record node) {
        this.structuralVariations = new ArrayList<>();
        this.labels = generateEntityLabels(node);
//        this.structuralVariations = generateStructuralVariations(record);
        // Para setear el nombre, concateno el nombre de todas las etiquetas
        this.name = labels.stream().map(Label::getName).collect(Collectors.joining());
    }


    public ArrayList<StructuralVariation> generateStructuralVariations(Record record) {
        ArrayList<StructuralVariation> structuralVariations = new ArrayList<>();
        return structuralVariations;
    }

    /**
     * Método para la generación de las etiquetas de un nodo.
     * @see Label
     * @param node Nodo del que se quiere extraer las etiquetas.
     * @return ArrayList de Label
     */
    public static ArrayList<Label> generateEntityLabels(Record node){
        ArrayList<Label> labels = new ArrayList<>();
        // No me queda más remedio que obtener las etiquetas así por la estructura de un nodo en Neo4J
        (node.values().get(0)).asNode().labels().forEach(label -> labels.add(new Label(label)));
        return labels;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntityType that = (EntityType) o;
        // Para comprobar si dos entidades son iguales, me basta con comparar el nombre, ya que está compuesto a partir de las etiquetas.
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(labels, structuralVariations, name);
    }

    @Override
    public String toString() {
        return "EntityType{" +
                "labels=" + labels +
                ", name='" + name + '\'' +
                ", structuralVariations=" + structuralVariations +
                "}\n";
    }
}
