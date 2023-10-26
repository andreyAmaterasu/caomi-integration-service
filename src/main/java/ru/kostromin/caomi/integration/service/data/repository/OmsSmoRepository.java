package ru.kostromin.caomi.integration.service.data.repository;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.kostromin.caomi.integration.service.data.entity.OmsSmo;

@Repository
public interface OmsSmoRepository extends CrudRepository<OmsSmo, Integer> {

  Optional<OmsSmo> findByCod(String cod);

}
