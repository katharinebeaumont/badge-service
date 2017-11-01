package badgeService.attendee;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Date;
import java.util.List;

/**
 * Created by katharinevoxxed on 03/02/2017.
 */
@RepositoryRestResource
public interface AttendeeRepository extends JpaRepository<Attendee, Integer> {


    @Query("select coalesce(max(a.changedSince), 0) from Attendee a where a.changedSince > :changedSince")
    Long findNextChangedSince(@Param("changedSince") long date);

    @Query("select a.id from Attendee a where a.changedSince > :changedSince")
    List<Integer> findIds(@Param("changedSince") long date);

    @Query("select a from Attendee a where a.id in (:ids)")
    List<Attendee> findAllWithIds(@Param("ids") List<Integer> ids);

    @Query("select a from Attendee a where a.uuid = :uuid")
    Attendee findOne(@Param("uuid") String uuid);
}
