package ru.kostromin.caomi.integration.service.data.repository;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.kostromin.caomi.integration.service.data.entity.OmsKlProfitType;

@Repository
public interface OmsKlProfitTypeRepository extends CrudRepository<OmsKlProfitType, Integer> {

  Optional<OmsKlProfitType> findByCode(String code);
}
