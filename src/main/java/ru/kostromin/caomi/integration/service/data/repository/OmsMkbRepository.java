package ru.kostromin.caomi.integration.service.data.repository;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.kostromin.caomi.integration.service.data.entity.OmsMkb;

@Repository
public interface OmsMkbRepository extends CrudRepository<OmsMkb, Integer> {

  Optional<OmsMkb> findByDs(String ds);

}
