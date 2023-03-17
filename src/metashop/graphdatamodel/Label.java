package metashop.graphdatamodel;

import java.util.Objects;

public class Label {

    private String name;

    public Label(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Label label = (Label) o;
        return Objects.equals(name, label.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "Label{" +
                "name='" + name + '\'' +
                '}';
    }
}
