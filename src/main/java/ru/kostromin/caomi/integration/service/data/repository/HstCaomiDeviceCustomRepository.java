package ru.kostromin.caomi.integration.service.data.repository;

import java.util.List;
import ru.kostromin.caomi.integration.service.data.dto.DeviceDto;

public interface HstCaomiDeviceCustomRepository {

  List<DeviceDto> getDeviceDataByLimitAndOffset(String deviceQuery, int offset, int limit);

}
