package ru.kostromin.caomi.integration.service.data.repository;

import java.util.List;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.kostromin.caomi.integration.service.data.entity.HstCaomiAcceptReferral;

@Repository
public interface HstCaomiAcceptReferralRepository extends CrudRepository<HstCaomiAcceptReferral, Integer> {

  @Query(
      value =
          "select * from dbo.hst_caomiAcceptReferral where statusID = :statusId "
              + "order by caomiAcceptReferralID asc "
              + "OFFSET :offset ROWS "
              + "FETCH NEXT :limit ROWS ONLY")
  List<HstCaomiAcceptReferral> findEntriesWithStatusIdOffsetAndLimit(
      @Param("statusId") Integer statusId,
      @Param("offset") Integer offset,
      @Param("limit") Integer limit);

}
