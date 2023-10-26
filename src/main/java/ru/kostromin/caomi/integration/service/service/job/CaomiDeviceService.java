package ru.kostromin.caomi.integration.service.service.job;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.kostromin.caomi.integration.service.config.AddDeviceJobConfig;
import ru.kostromin.caomi.integration.service.data.dto.DeviceDto;
import ru.kostromin.caomi.integration.service.data.entity.HstCaomiDevice;
import ru.kostromin.caomi.integration.service.data.repository.HstCaomiDeviceCustomRepository;
import ru.kostromin.caomi.integration.service.data.repository.HstCaomiDeviceRepository;
import ru.kostromin.caomi.integration.service.feign.CaomiFeignClient;
import ru.kostromin.caomi.integration.service.feign.request.device.CaomiDeviceRequest;
import ru.kostromin.caomi.integration.service.feign.request.device.CaomiDeviceRequest.Service;
import ru.kostromin.caomi.integration.service.feign.response.CaomiBasicResponse;

/**
 * Сервис поиска данных оборудования для отправки в ЦАМИ (на метод POST AddDevice)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CaomiDeviceService {

  private final AddDeviceJobConfig config;
  private final HstCaomiDeviceRepository deviceRepository;
  private final HstCaomiDeviceCustomRepository deviceCustomRepository;
  private final CaomiFeignClient caomiFeignClient;

  /**
   * Отправки данных о создании/обновлении данных в ЦАМИ по оборудованию
   */
  public void findAndSendDataToCaomi(){
    // (1) собрать 'device данные'
    log.info("(1) - Обращение в БД за device...");
    List<DeviceDto> devices = getDeviceDataByLimitAndOffset(
        config.getSql().getDeviceSql(),
        config.getSql().getOffset(),
        config.getSql().getLimit());
    if(devices != null && !devices.isEmpty()){
      log.info("Извлечено {} devices в БД", devices.size());
      log.info("(2) - Обращение в БД за device...");
      devices.forEach(device -> {
        log.info("(3) - Запрос на регистрацию оборудования от hst_caomiDevice.hltEquipmentGUID = {}", device.getGuid());
        // (2) собрать Json для отправки в ЦАМИ
        CaomiDeviceRequest caomiRequest = createCaomiRequest(device);
        // (3) отправить POST запрос в ЦАМИ
        sendRequestAndProcessResponse(caomiRequest, device.getCaomiDeviceId());
      });
    } else {
      log.info("Для отправки данных в ЦАМИ не было найдено данных в БД.");
    }
  }

  /**
   * Получение device-данных по лимиту и оффсету
   * @param deviceQuery - sql запрос
   * @param offset - offset
   * @param limit - limit
   * @return - список DeviceDto
   */
  public List<DeviceDto> getDeviceDataByLimitAndOffset(String deviceQuery, int offset, int limit){
    try{
      return deviceCustomRepository.getDeviceDataByLimitAndOffset(deviceQuery, offset, limit);
    } catch (Exception e){
      log.error("Произошла ошибка при получении данных device: ", e);
      return null;
    }
  }

  /**
   * Создание запроса в ЦАМИ
   * @param deviceData - 'device данные'
   * @return - запрос в ЦАМИ
   */
  private CaomiDeviceRequest createCaomiRequest(DeviceDto deviceData){
    CaomiDeviceRequest caomiRequest = new CaomiDeviceRequest();
    caomiRequest.setDeviceMisId(deviceData.getDeviceMisId());
    caomiRequest.setIsActive(deviceData.getIsActiveDevice());
    caomiRequest.setDeviceName(deviceData.getDeviceName());
    caomiRequest.setOwner(deviceData.getOwner());

    CaomiDeviceRequest.Service service = new Service();
    service.setServiceCode(deviceData.getServiceCode());
    service.setServiceName(deviceData.getServiceName());
    service.setIsActive(deviceData.getIsActiveService());
    List<CaomiDeviceRequest.Service> serviceList = new ArrayList<>();
    serviceList.add(service);
    caomiRequest.setService(serviceList);

    return caomiRequest;
  }

  /**
   * Отправить запрос в ЦАМИ и обработать ответ
   * @param caomiRequest - запрос в ЦАМИ
   * @param caomiDeviceId - идентификатор оборудования (таблицы hst_caomiDevice)
   */
  private void sendRequestAndProcessResponse(CaomiDeviceRequest caomiRequest, Integer caomiDeviceId){
    try{
      ResponseEntity<CaomiBasicResponse> responseEntity = caomiFeignClient.addDevice(caomiRequest);
      if(responseEntity.hasBody()){
        // (4) сохранить ответ от запроса в hst_caomiDevice
        CaomiBasicResponse response = responseEntity.getBody();
        saveCaomiDeviceResponse(caomiDeviceId, response, responseEntity.getStatusCodeValue());
      } else {
        log.error("Отсутвует тело ответа ЦАМИ, необходимое для сохранения в БД!");
      }
    } catch (Exception e){
      log.error("Непредвиденная ошибка при отправке запроса/получении ответа от ЦАМИ: ", e);
      saveCaomiUnexpectedError(caomiDeviceId, e);
    }
  }

  /**
   * Сохранить ответ от ЦАМИ
   * @param caomiDeviceId - идентификатор hst_caomiDevice
   * @param response - ответ от ЦАМИ
   * @param httpCode - http код
   */
  public void saveCaomiDeviceResponse(Integer caomiDeviceId, CaomiBasicResponse response, Integer httpCode) {
    try {
      Optional<HstCaomiDevice> deviceOpt = deviceRepository.findById(caomiDeviceId);
      deviceOpt.ifPresentOrElse(
          device -> {
            device.setCaomiId(response.getId());
            device.setErrorCode(response.getErrorCode());
            device.setErrorText(response.getErrorText());
            device.setDateStatus(LocalDateTime.now());
            device.setHttpStatus(httpCode);
            device.setStatusId(1);
            deviceRepository.save(device);
          },
          () -> log.error("Не найдена запись hst_caomiDevice для обновления с caomiDeviceID ={}", caomiDeviceId));
    } catch (Exception e){
      log.error("Произошла непредвиденная ошибка при попытке сохранить ответ от ЦАМИ "
          + "(связанный с hst_caomiDevice.caomiDeviceID = {}): ", caomiDeviceId, e);
    }
  }

  /**
   * Сохранить информацию о непредвиденной ошибке
   * @param caomiDeviceId - идентификатор hst_caomiDevice
   * @param unexpectedException - исключение
   */
  public void saveCaomiUnexpectedError(Integer caomiDeviceId, Exception unexpectedException) {
    try {
      Optional<HstCaomiDevice> deviceOpt = deviceRepository.findById(caomiDeviceId);
      deviceOpt.ifPresentOrElse(device -> {
            String errorText = unexpectedException.toString()
                .substring(0, Math.min(unexpectedException.toString().length(), 100));
            device.setHttpStatus(0);
            device.setErrorCode("");
            device.setErrorText(errorText);
            device.setDateStatus(LocalDateTime.now());
            deviceRepository.save(device);
          },
          () -> log.error("Не найдена запись hst_caomiDevice для обновления с caomiDeviceID ={}", caomiDeviceId));
    } catch (Exception e){
      log.error("Произошла непредвиденная ошибка при попытке сохранить ответ от ЦАМИ "
          + "(связанный с hst_caomiDevice.caomiDeviceID = {}): ", caomiDeviceId, e);
    }
  }
}
