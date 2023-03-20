package metashop.uschema.features;

import java.util.ArrayList;

public class UKey extends ULogicalFeature{

    private final ArrayList<UAttribute> uAttributes;

    public UKey(String name, ArrayList<UAttribute> keyAttributes) {
        super(name, true);
        this.uAttributes = keyAttributes;
    }

    public ArrayList<UAttribute> getUAttributes() {
        return uAttributes;
    }

}
