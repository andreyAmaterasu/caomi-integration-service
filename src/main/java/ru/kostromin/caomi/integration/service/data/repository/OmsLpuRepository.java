package ru.kostromin.caomi.integration.service.data.repository;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.kostromin.caomi.integration.service.data.entity.OmsLpu;

@Repository
public interface OmsLpuRepository extends CrudRepository<OmsLpu, Integer> {

  Optional<OmsLpu> findTopByLic(String lic);

}
