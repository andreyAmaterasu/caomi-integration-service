package ru.kostromin.caomi.integration.service.data.repository;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.kostromin.caomi.integration.service.data.entity.LbrResearch;

@Repository
public interface LbrResearchRepository extends CrudRepository<LbrResearch, Integer> {

  boolean existsByResearchId(Integer researchId);

  List<LbrResearch> findByRfLaboratoryResearchGUID(String rfLaboratoryResearchGuid);

}
