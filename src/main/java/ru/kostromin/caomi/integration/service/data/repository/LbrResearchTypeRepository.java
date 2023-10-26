package ru.kostromin.caomi.integration.service.data.repository;

import java.util.Optional;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.kostromin.caomi.integration.service.data.entity.LbrResearchType;

@Repository
public interface LbrResearchTypeRepository extends CrudRepository<LbrResearchType, Integer> {

  boolean existsByCode(String code);

  Optional<LbrResearchType> findByUguid(String uguid);

  @Query(value = "select top(1) rt.UGUID  from lbr_ResearchType rt "
      + "join oms_kl_NomService okns on okns.kl_NomServiceID = rt.rf_kl_NomServiceID "
      + "where okns.code = :code")
  Optional<String> findUGUIDByServiceRequestCode(String code);

}
