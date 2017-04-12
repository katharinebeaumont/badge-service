package badgeService.util;

/**
 * Created by katharinevoxxed on 09/02/2017.
 */
public abstract class CSVEntityMapper<E extends Object> {

    public abstract E getEntity(String details);
}
