package ru.kostromin.caomi.integration.service.service.job;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.kostromin.caomi.integration.service.config.AddDeviceJobConfig;
import ru.kostromin.caomi.integration.service.config.AddDeviceJobConfig.DataQueries;
import ru.kostromin.caomi.integration.service.data.dto.DeviceDto;
import ru.kostromin.caomi.integration.service.data.entity.HstCaomiDevice;
import ru.kostromin.caomi.integration.service.data.repository.HstCaomiDeviceCustomRepository;
import ru.kostromin.caomi.integration.service.data.repository.HstCaomiDeviceRepository;
import ru.kostromin.caomi.integration.service.feign.CaomiFeignClient;
import ru.kostromin.caomi.integration.service.feign.request.device.CaomiDeviceRequest;
import ru.kostromin.caomi.integration.service.feign.response.CaomiBasicResponse;

@ExtendWith(SpringExtension.class)
class CaomiDeviceServiceTest {

  @Mock
  private AddDeviceJobConfig config;
  @Mock
  private HstCaomiDeviceRepository deviceRepository;
  @Mock
  private HstCaomiDeviceCustomRepository deviceCustomRepository;
  @Mock
  private CaomiFeignClient caomiFeignClient;

  private CaomiDeviceService caomiService;

  @BeforeEach
  void initCaomiService(){
    caomiService = new CaomiDeviceService(config, deviceRepository,
        deviceCustomRepository, caomiFeignClient);
  }

  @Test
  @DisplayName("Поиск и отправка данных в ЦАМИ."
      + "SQL найдена,"
      + "данные для отправки не найдены."
      + "Ничего не отправлено.")
  void findAndSendDataToCaomiSqlFoundDataNotFound_nothingDone(){
    // (1) prepare mocks:
    // device-SQL найдена
    DataQueries dataQueries = new DataQueries();
    dataQueries.setDeviceSql("someQuery");
    dataQueries.setLimit(1);
    dataQueries.setOffset(2);
    Mockito.when(config.getSql()).thenReturn(dataQueries);

    // данные для отправки не найдены
    Mockito.when(deviceCustomRepository.getDeviceDataByLimitAndOffset(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt()))
        .thenReturn(new ArrayList<>());

    // (2) start test:
    caomiService.findAndSendDataToCaomi();

    // (3) check:
    // sql, limit, offset
    Mockito.verify(config, Mockito.times(3)).getSql();

    Mockito.verify(deviceCustomRepository, Mockito.times(1))
        .getDeviceDataByLimitAndOffset(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt());

    Mockito.verify(caomiFeignClient, Mockito.times(0))
        .addDevice(Mockito.any(CaomiDeviceRequest.class));

    Mockito.verify(deviceRepository, Mockito.times(0))
        .save(Mockito.any(HstCaomiDevice.class));
  }

  @Test
  @DisplayName("Поиск и отправка данных в ЦАМИ."
      + "SQL найдена,"
      + "данные для отправки найдены,"
      + "отправлен запрос в ЦАМИ, но выброшено непредвиденное исключение."
      + "Данные об исключении сохранены в БД.")
  void findAndSendDataToCaomiSqlFoundDataFoundCaomiSendingThrewException_exceptionDataSaved(){
    // (1) prepare mocks:
    // device-SQL найдена
    DataQueries dataQueries = new DataQueries();
    dataQueries.setDeviceSql("someQuery");
    dataQueries.setLimit(1);
    dataQueries.setOffset(2);
    Mockito.when(config.getSql()).thenReturn(dataQueries);

    // данные для отправки найдены
    List<DeviceDto> expectedDevices = new ArrayList<>();
    DeviceDto expectedDevice = new DeviceDto();
    expectedDevice.setCaomiDeviceId(1);
    expectedDevice.setDeviceMisId("misId");
    expectedDevice.setIsActiveDevice(Boolean.TRUE);
    expectedDevice.setDeviceName("deviceName");
    expectedDevice.setOwner("owner");
    expectedDevice.setServiceCode("serviceCode");
    expectedDevice.setServiceName("serviceName");
    expectedDevice.setIsActiveService(Boolean.TRUE);
    expectedDevices.add(expectedDevice);
    Mockito.when(deviceCustomRepository.getDeviceDataByLimitAndOffset(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt()))
        .thenReturn(expectedDevices);

    // при отправке запроса в ЦАМИ выброшено исключение
    RuntimeException expectedException = new RuntimeException("someException");
    Mockito.doThrow(expectedException)
        .when(caomiFeignClient).addDevice(Mockito.any(CaomiDeviceRequest.class));

    Mockito.when(deviceRepository.findById(Mockito.anyInt()))
        .thenReturn(Optional.of(new HstCaomiDevice()));

    // (2) start test:
    caomiService.findAndSendDataToCaomi();

    // (3) check:
    // sql, limit, offset
    Mockito.verify(config, Mockito.times(3)).getSql();

    Mockito.verify(deviceCustomRepository, Mockito.times(1))
        .getDeviceDataByLimitAndOffset(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt());

    ArgumentCaptor<CaomiDeviceRequest> caomiRequestCaptor = ArgumentCaptor.forClass(
        CaomiDeviceRequest.class);
    Mockito.verify(caomiFeignClient, Mockito.times(1))
        .addDevice(caomiRequestCaptor.capture());
    CaomiDeviceRequest actualRequest = caomiRequestCaptor.getValue();
    Assertions.assertEquals(expectedDevice.getDeviceMisId(),
        actualRequest.getDeviceMisId());
    Assertions.assertEquals(expectedDevice.getIsActiveDevice(),
        actualRequest.getIsActive());
    Assertions.assertEquals(expectedDevice.getDeviceName(),
        actualRequest.getDeviceName());
    Assertions.assertEquals(expectedDevice.getOwner(),
        actualRequest.getOwner());
    CaomiDeviceRequest.Service actualService = actualRequest.getService().get(0);
    Assertions.assertEquals(expectedDevice.getServiceCode(), actualService.getServiceCode());
    Assertions.assertEquals(expectedDevice.getServiceName(), actualService.getServiceName());
    Assertions.assertEquals(expectedDevice.getIsActiveService(), actualService.getIsActive());

    ArgumentCaptor<HstCaomiDevice> deviceCaptor = ArgumentCaptor.forClass(HstCaomiDevice.class);
    Mockito.verify(deviceRepository, Mockito.times(1))
        .save(deviceCaptor.capture());
    HstCaomiDevice savedDevice = deviceCaptor.getValue();
    Assertions.assertEquals(expectedException.toString(), savedDevice.getErrorText());
    Assertions.assertNotNull(savedDevice.getDateStatus());
  }

  @Test
  @DisplayName("Поиск и отправка данных в ЦАМИ."
      + "SQL найдена,"
      + "данные для отправки найдены,"
      + "отправлен запрос в ЦАМИ, возвращен ответ."
      + "Данные об ответе сохранены в БД.")
  void findAndSendDataToCaomiSqlFoundDataFoundCaomiReturnedResponse_responseDataSaved(){
    // (1) prepare mocks:
    // device-SQL найдена
    DataQueries dataQueries = new DataQueries();
    dataQueries.setDeviceSql("someQuery");
    dataQueries.setLimit(1);
    dataQueries.setOffset(2);
    Mockito.when(config.getSql()).thenReturn(dataQueries);

    // данные для отправки найдены
    List<DeviceDto> expectedDevices = new ArrayList<>();
    DeviceDto expectedDevice = new DeviceDto();
    expectedDevice.setCaomiDeviceId(1);
    expectedDevice.setDeviceMisId("misId");
    expectedDevice.setIsActiveDevice(Boolean.TRUE);
    expectedDevice.setDeviceName("deviceName");
    expectedDevice.setOwner("owner");
    expectedDevice.setServiceCode("serviceCode");
    expectedDevice.setServiceName("serviceName");
    expectedDevice.setIsActiveService(Boolean.TRUE);
    expectedDevices.add(expectedDevice);
    Mockito.when(deviceCustomRepository.getDeviceDataByLimitAndOffset(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt()))
        .thenReturn(expectedDevices);

    // ЦАМИ вернуло ответ
    CaomiBasicResponse expCaomiResponse = new CaomiBasicResponse();
    expCaomiResponse.setId(1);
    expCaomiResponse.setErrorCode("code");
    expCaomiResponse.setErrorText("text");
    ResponseEntity<CaomiBasicResponse> expectedRespEnt = ResponseEntity.of(Optional.of(expCaomiResponse));
    Mockito.when(caomiFeignClient.addDevice(Mockito.any(CaomiDeviceRequest.class)))
        .thenReturn(expectedRespEnt);

    Mockito.when(deviceRepository.findById(Mockito.anyInt()))
        .thenReturn(Optional.of(new HstCaomiDevice()));

    // (2) start test:
    caomiService.findAndSendDataToCaomi();

    // (3) check:
    // sql, limit, offset
    Mockito.verify(config, Mockito.times(3)).getSql();

    Mockito.verify(deviceCustomRepository, Mockito.times(1))
        .getDeviceDataByLimitAndOffset(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt());

    ArgumentCaptor<CaomiDeviceRequest> caomiRequestCaptor = ArgumentCaptor.forClass(
        CaomiDeviceRequest.class);
    Mockito.verify(caomiFeignClient, Mockito.times(1))
        .addDevice(caomiRequestCaptor.capture());
    CaomiDeviceRequest actualRequest = caomiRequestCaptor.getValue();
    Assertions.assertEquals(expectedDevice.getDeviceMisId(),
        actualRequest.getDeviceMisId());
    Assertions.assertEquals(expectedDevice.getIsActiveDevice(),
        actualRequest.getIsActive());
    Assertions.assertEquals(expectedDevice.getDeviceName(),
        actualRequest.getDeviceName());
    Assertions.assertEquals(expectedDevice.getOwner(),
        actualRequest.getOwner());
    CaomiDeviceRequest.Service actualService = actualRequest.getService().get(0);
    Assertions.assertEquals(expectedDevice.getServiceCode(), actualService.getServiceCode());
    Assertions.assertEquals(expectedDevice.getServiceName(), actualService.getServiceName());
    Assertions.assertEquals(expectedDevice.getIsActiveService(), actualService.getIsActive());

    ArgumentCaptor<HstCaomiDevice> deviceCaptor = ArgumentCaptor.forClass(HstCaomiDevice.class);
    Mockito.verify(deviceRepository, Mockito.times(1))
        .save(deviceCaptor.capture());
    HstCaomiDevice savedDevice = deviceCaptor.getValue();
    Assertions.assertEquals(expCaomiResponse.getId(), savedDevice.getCaomiId());
    Assertions.assertEquals(expCaomiResponse.getErrorCode(), savedDevice.getErrorCode());
    Assertions.assertEquals(expCaomiResponse.getErrorText(), savedDevice.getErrorText());
    Assertions.assertNotNull(savedDevice.getDateStatus());
    Assertions.assertEquals(expectedRespEnt.getStatusCodeValue(), savedDevice.getHttpStatus());
    Assertions.assertEquals(1, savedDevice.getStatusId());
  }
}