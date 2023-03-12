package metashop.schema.graphdatamodel.model;

import java.util.Map;

public class Tuple {

    private String className;
    private Map<String, String> properties;

    public Tuple(String className, Map<String, String> properties) {
        this.className = className;
        this.properties = properties;
    }

    public String getClassName() {
        return className;
    }

    public Map<String, String> getProperties() {
        return properties;
    }
}
