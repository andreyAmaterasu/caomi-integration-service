package ru.kostromin.caomi.integration.service.data.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.kostromin.caomi.integration.service.data.entity.HstCaomiDevice;

@Repository
public interface HstCaomiDeviceRepository extends CrudRepository<HstCaomiDevice, Integer> {

}
