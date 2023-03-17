package metashop.graphdatamodel;

import org.neo4j.driver.Record;
import org.neo4j.driver.Value;

import java.util.ArrayList;
import java.util.Objects;


public class EntityType {

    private final ArrayList<Label> labels;
    private final StructuralVariation structuralVariations;
    private final String name;

    private static final int ENTITY_TYPE_NAME_INDEX = 0;
    private static final int ENTITY_TYPE_LABELS_INDEX = 1;
    private static final int ENTITY_TYPE_PROPERTIES_INDEX = 2;

    public EntityType(Record node) {
        this.labels = generateEntityLabels(node);
        this.name = node.get(ENTITY_TYPE_NAME_INDEX).asString();
        this.structuralVariations = generateStructuralVariations(node);

    }

    public StructuralVariation generateStructuralVariations(Record node) {
        ArrayList<Value> properties = new ArrayList<>();
        node.values().get(ENTITY_TYPE_PROPERTIES_INDEX).values().forEach(properties::add);
        return new StructuralVariation(properties);
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
        node.values().get(ENTITY_TYPE_LABELS_INDEX).values().forEach(label -> labels.add(new Label(label.asString())));
        return labels;
    }

    public String getName() {
        return name;
    }

    public ArrayList<Label> getLabels() {
        return labels;
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
        return "\nEntityType{" +
                "labels=" + labels +
                ", name='" + name +
                "', structuralVariations=" + structuralVariations +
                "}";
    }
}
