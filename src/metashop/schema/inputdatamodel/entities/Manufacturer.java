package metashop.schema.inputdatamodel.entities;

/**
 * - Pensando en la migración a una base de datos relacional,
 *   no necesitaría definir una lista de productos ya que esa información la tendría incluída el mismo producto.
 *   Esto evitaría duplicar información.
 */
public class Manufacturer {

    private Integer __manufacturerId;
    private String name;

    public Manufacturer(Integer __manufacturerId, String name) {
        this.__manufacturerId = __manufacturerId;
        this.name = name;
    }

    public Integer getManufacturerId() {
        return __manufacturerId;
    }

    public String getName() {
        return name;
    }
}
