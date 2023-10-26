package ru.kostromin.caomi.integration.service.data.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.kostromin.caomi.integration.service.data.entity.OmsMnPerson;

@Repository
public interface OmsMnPersonRepository extends CrudRepository<OmsMnPerson, Integer> {

  boolean existsByPersonGuid(String personGuid);

}
