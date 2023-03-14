package metashop.schema.graphdatamodel.model;

public class Label {

    private String name;

    public Label(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Label{" +
                "name='" + name + '\'' +
                '}';
    }
}
