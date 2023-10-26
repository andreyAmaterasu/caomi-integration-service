package ru.kostromin.caomi.integration.service.data.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.kostromin.caomi.integration.service.data.entity.LbrLaboratoryResearch;

@Repository
public interface LbrLaboratoryResearchRepository extends CrudRepository<LbrLaboratoryResearch, Integer> {

}
