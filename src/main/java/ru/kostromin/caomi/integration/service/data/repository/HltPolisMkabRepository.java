package ru.kostromin.caomi.integration.service.data.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.kostromin.caomi.integration.service.data.entity.HltPolisMkab;

@Repository
public interface HltPolisMkabRepository extends CrudRepository<HltPolisMkab, Integer> {

}
