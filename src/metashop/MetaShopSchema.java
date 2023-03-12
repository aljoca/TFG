package metashop;

import java.lang.reflect.Field;
import java.util.*;

public abstract class MetaShopSchema {

    /**
     * Método para extraer los campos de cada clase del esquema
     */
     static Map<String, Map<String, String>> createSchema(List<Class> entities) throws Exception {
        Map<String, Map<String, String>> result = new HashMap<>();
        for (Class entity: entities) {
            Map<String, String> mappedClass = new HashMap<>();
            for (Field f: entity.getDeclaredFields()) {
                f.setAccessible(true);
                mappedClass.put(f.getName(), f.getType().getSimpleName());
            }
            result.put(entity.getSimpleName(), mappedClass);
        }
        return result;
    }
    

}
