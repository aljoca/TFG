package metashop.uschema.entities;

public abstract class UEntityType {

    private final boolean root;
    private final String name;

    public UEntityType(String name) {
        this.name = name;
        this.root = true;
    }

    public boolean isRoot() {
        return root;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "\nUEntityType{" +
                "root=" + root +
                ", name='" + name + '\'' +
                '}';
    }
}
