package metashop.uschema;

import metashop.graphdatamodel.Property;
import metashop.uschema.features.UAttribute;
import metashop.uschema.features.UFeature;
import metashop.uschema.features.UKey;
import metashop.uschema.features.UReference;

import java.util.ArrayList;
import java.util.HashMap;

public class UStructuralVariation {

    private static int U_STRUCTURAL_VARIATION_IDENTIFIER = 1;
    private final int id;
    private final HashMap<String, UFeature> features;

    public UStructuralVariation() {
        this.features = new HashMap<>();
        this.id = U_STRUCTURAL_VARIATION_IDENTIFIER++;
    }

    public void addFeature(UFeature uFeature){
        features.put(uFeature.getName(), uFeature);
    }

    /**
     * Método para generar features.
     *
     * @param properties
     */
    public void generateFeatures(String name, ArrayList<Property> properties){
        // Para cada propiedad, creo un atributo nuevo
        ArrayList<UAttribute> keys = new ArrayList<>();
        properties.forEach(property -> {
            UAttribute uFeature = new UAttribute(property);
            // Si el nombre del atributo empieza por "__" lo añado a la lista de atributos de una key, que finalmente será parte de una PK y una FK.
            // Esto lo hago porque las PK y FK pueden ser compuestas.
            if (property.getName().startsWith("__")){
                keys.add(uFeature);
            }
            features.put(property.getName(), uFeature);
            // Si la lista  de atributos de la key no está vacía, creo la Key con el nombre "KEY_" + nombre de la tabla de la que es key.
        });
        if (!keys.isEmpty()){
            String keyName = "KEY_" + name;
            features.put(keyName, new UKey(keyName, keys));
        }
    }

    public HashMap<String, UFeature> getFeatures() {
        return features;
    }

    /**
     * Método para obtener los atributos de una structural variation.
     * @see UAttribute
     * @return HashMap con los atributos de la structural variation
     */
    public HashMap<String, UAttribute> getAttributes(){
        HashMap<String, UAttribute> attributes = new HashMap<>();
        for (UFeature uFeature: this.features.values()) {
            if (uFeature instanceof UAttribute){
                attributes.put(uFeature.getName(), (UAttribute) uFeature);
            }
        }
        return attributes;
    }

    /**
     * Método para encontrar la key de una structural variation.
     * @see UKey
     * @return Key de la structural variation
     */
    public UKey getKey(){
        for (UFeature uFeature: this.features.values()) {
            if (uFeature instanceof UKey){
               return (UKey) uFeature;
            }
        }
        return null;
    }

    /**
     * Método para encontrar las referencias de una structural variation
     * @see UReference
     * @return Referencias de la structural variation
     */
    public ArrayList<UReference> getReferences(){
        ArrayList<UReference> references = new ArrayList<>();
        for (UFeature uFeature: this.features.values()){
            if (uFeature instanceof UReference){
                references.add((UReference) uFeature);
            }
        }
        return references;
    }

    @Override
    public String toString() {
        return "UStructuralVariation{" +
                "id=" + id +
                ", features=" + features +
                '}';
    }
}
