package metashop.uschema.entities;

import metashop.graphdatamodel.EntityType;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class UEntityTypeMultiLabeled extends UEntityType{

    private final HashMap<String, UEntityType> parents;

    public UEntityTypeMultiLabeled(EntityType entityType, List<UEntityType> parentEntities) {
        super(entityType);
        this.parents = (HashMap<String, UEntityType>) parentEntities.stream().collect(Collectors.toMap(UEntityType::getName, parentEntity -> parentEntity));
    }

    @Override
    public String toString() {
        return "UEntityTypeMultiLabeled{" +
                "parents=" + parents +
                "}";
    }
}
