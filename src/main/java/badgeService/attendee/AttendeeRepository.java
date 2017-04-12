package badgeService.attendee;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 * Created by katharinevoxxed on 03/02/2017.
 */
@RepositoryRestResource
public interface AttendeeRepository extends JpaRepository<Attendee, String> {

}
