package ru.kostromin.caomi.integration.service.data.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.kostromin.caomi.integration.service.data.entity.OmsKlNowService;

@Repository
public interface OmsKlNowServiceRepository extends CrudRepository<OmsKlNowService, Long> {

  boolean existsByCode(String code);

}
