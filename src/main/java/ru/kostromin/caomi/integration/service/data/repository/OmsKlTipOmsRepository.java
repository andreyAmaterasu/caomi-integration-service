package ru.kostromin.caomi.integration.service.data.repository;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.kostromin.caomi.integration.service.data.entity.OmsKlTipOms;

@Repository
public interface OmsKlTipOmsRepository extends CrudRepository<OmsKlTipOms, Integer> {

  Optional<OmsKlTipOms> findByIdDoc(Integer idDoc);

}
