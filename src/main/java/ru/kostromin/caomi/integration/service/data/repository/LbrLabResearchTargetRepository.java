package ru.kostromin.caomi.integration.service.data.repository;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.kostromin.caomi.integration.service.data.entity.LbrLabResearchTarget;

@Repository
public interface LbrLabResearchTargetRepository extends CrudRepository<LbrLabResearchTarget, Integer> {

  Optional<LbrLabResearchTarget> findByCode(String code);
}
