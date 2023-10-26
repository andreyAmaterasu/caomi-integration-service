package ru.kostromin.caomi.integration.service.data.repository;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.kostromin.caomi.integration.service.data.entity.OmsKlSex;

@Repository
public interface OmsKlSexRepository extends CrudRepository<OmsKlSex, Integer> {

  Optional<OmsKlSex> findByCode(String code);

}
