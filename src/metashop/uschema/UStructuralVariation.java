package metashop.uschema;

import metashop.graphdatamodel.Property;
import metashop.uschema.features.UAttribute;
import metashop.uschema.features.UFeature;
import metashop.uschema.features.UKey;
import metashop.uschema.features.UReference;

import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;

public class UStructuralVariation {

    private static int U_STRUCTURAL_VARIATION_IDENTIFIER = 1;
    private final int id;
    private final HashMap<String, UFeature> features;
    private final USchemaType container;

    public UStructuralVariation(USchemaType container) {
        this.container = container;
        this.features = new HashMap<>();
        // Necesito que las referencias no se añadan de nuevo a la structuralVariation
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
        /* TODO Debo preguntar una cosa: ¿los atributos de una relación irían en una tabla aparte?
            Por ejemplo, tengo la relación IN_ORDER, que relaciona un producto con un pedido. Esta relación
            tiene los atributos quantity y subprice. La relación representaría un "ItemPedido".
            Mi idea es que si una relación tiene propiedades debería crear una tabla intermedia que contenga el idOrigen,
            idDestino y los atributos de las relaciones.
         */
        /*
            TODO Campos que considero compuestos: PaymentMethod, Discount
         */
        ArrayList<UAttribute> keys = new ArrayList<>();
        properties.forEach(property -> {
            UAttribute uFeature = new UAttribute(property);
            if (property.getName().startsWith("__")){
                keys.add(uFeature);
            }
            features.put(property.getName(), uFeature);
            if (!keys.isEmpty()){
                String keyName = "KEY_" + name;
                features.put(keyName, new UKey(keyName, keys));
            }
        });
    }

    public HashMap<String, UFeature> getFeatures() {
        return features;
    }

    public HashMap<String, UAttribute> getAttributes(){
        HashMap<String, UAttribute> attributes = new HashMap<>();
        for (UFeature uFeature: this.features.values()) {
            if (uFeature instanceof UAttribute){
                attributes.put(uFeature.getName(), (UAttribute) uFeature);
            }
        }
        return attributes;
    }

    public UKey getKey(){
        for (UFeature uFeature: this.features.values()) {
            if (uFeature instanceof UKey){
               return (UKey) uFeature;
            }
        }
        return null;
    }

    public ArrayList<UReference> getReferences(){
        ArrayList<UReference> references = new ArrayList<>();
        for (UFeature uFeature: this.features.values()){
            if (uFeature instanceof UReference){
                references.add((UReference) uFeature);
            }
        }
        return references;
    }

    public USchemaType getContainer() {
        return container;
    }

    @Override
    public String toString() {
        return "UStructuralVariation{" +
                "id=" + id +
                ", features=" + features +
                '}';
    }
}
