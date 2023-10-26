package ru.kostromin.caomi.integration.service.data.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.kostromin.caomi.integration.service.data.entity.HltMkab;

@Repository
public interface HltMkabRepository extends CrudRepository<HltMkab, Integer> {

  Optional<HltMkab> findBySnilsAndLastNameAndNameAndPatronymic(String snils,
      String lastName, String name, String patronymic);

  Optional<HltMkab> findByLastNameAndNameAndPatronymicAndBirthDate(String lastName, String name,
      String patronymic, LocalDateTime birthDate);

  Optional<HltMkab> findByPolicyNumber(String policyNumber);

  boolean existsByUguid(String uguid);
}
