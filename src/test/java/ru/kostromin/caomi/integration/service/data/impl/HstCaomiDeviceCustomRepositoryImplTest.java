package ru.kostromin.caomi.integration.service.data.impl;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.kostromin.caomi.integration.service.data.dto.DeviceDto;

@ExtendWith(SpringExtension.class)
class HstCaomiDeviceCustomRepositoryImplTest {

  @Mock
  private NamedParameterJdbcTemplate jdbcTemplate;

  private HstCaomiDeviceCustomRepositoryImpl customRepositoryImpl;

  @BeforeEach
  void initDataHandler(){
    customRepositoryImpl = new HstCaomiDeviceCustomRepositoryImpl(jdbcTemplate);
  }

  @Test
  @DisplayName("Получение данных device."
      + "Данные получены без ошибок."
      + "Список данных device возвращен.")
  void getDeviceDataByLimitAndOffsetNoErrorsOccurred_listOfDataReturned(){
    // (1) prepare mocks:
    List<DeviceDto> expectedDevices = new ArrayList<>();
    DeviceDto expectedDevice = new DeviceDto();
    expectedDevice.setCaomiDeviceId(1);
    expectedDevice.setGuid("guid");
    expectedDevice.setDeviceMisId("dviceMisId");
    expectedDevice.setIsActiveDevice(true);
    expectedDevice.setDeviceName("deviceName");
    expectedDevice.setOwner("owner");
    expectedDevice.setServiceCode("serviceCode");
    expectedDevice.setServiceName("serviceName");
    expectedDevice.setIsActiveDevice(false);
    expectedDevices.add(expectedDevice);
    Mockito.when(jdbcTemplate.query(
        Mockito.anyString(),
        Mockito.any(MapSqlParameterSource.class),
        Mockito.any(BeanPropertyRowMapper.class)))
        .thenReturn(expectedDevices);

    // (2) start test:
    List<DeviceDto> deviceDtos = customRepositoryImpl.getDeviceDataByLimitAndOffset("someQuery",
        1,1);

    // (3) check:
    Assertions.assertEquals(1, deviceDtos.size());
    DeviceDto actualDevice = deviceDtos.get(0);
    Assertions.assertEquals(expectedDevice.getCaomiDeviceId(), actualDevice.getCaomiDeviceId());
    Assertions.assertEquals(expectedDevice.getGuid(), actualDevice.getGuid());
    Assertions.assertEquals(expectedDevice.getDeviceMisId(), actualDevice.getDeviceMisId());
    Assertions.assertEquals(expectedDevice.getIsActiveDevice(), actualDevice.getIsActiveDevice());
    Assertions.assertEquals(expectedDevice.getDeviceName(), actualDevice.getDeviceName());
    Assertions.assertEquals(expectedDevice.getOwner(), actualDevice.getOwner());
    Assertions.assertEquals(expectedDevice.getServiceCode(), actualDevice.getServiceCode());
    Assertions.assertEquals(expectedDevice.getServiceName(), actualDevice.getServiceName());
    Assertions.assertEquals(expectedDevice.getIsActiveService(), actualDevice.getIsActiveService());
  }
}