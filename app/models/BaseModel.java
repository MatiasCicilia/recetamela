package models;

import com.avaje.ebean.Model;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

/**
 * Base model for database models. All models must extend this class.
 */
@MappedSuperclass
public class BaseModel extends Model {

    @Id
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
