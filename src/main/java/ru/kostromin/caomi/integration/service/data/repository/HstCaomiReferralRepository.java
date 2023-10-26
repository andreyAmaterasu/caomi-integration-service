package ru.kostromin.caomi.integration.service.data.repository;

import java.util.List;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.kostromin.caomi.integration.service.data.entity.HstCaomiReferral;

@Repository
public interface HstCaomiReferralRepository extends CrudRepository<HstCaomiReferral, Integer> {

  @Query(
      value =
          "select * from dbo.hst_caomiReferral where statusID = 0 "
              + "order by caomiReferralID asc "
              + "OFFSET :offset ROWS "
              + "FETCH NEXT :limit ROWS ONLY")
  List<HstCaomiReferral> getReferralsToProcessByLimitAndOffset(@Param("offset") Integer offset, @Param("limit") Integer limit);

  List<HstCaomiReferral> findByCaomiId(String caomiId);
}
