package ru.kostromin.caomi.integration.service.service.job;


import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.kostromin.caomi.integration.service.config.AcceptAppointmentReferralJobConfig;
import ru.kostromin.caomi.integration.service.data.dto.EquipmentSlotsDto;
import ru.kostromin.caomi.integration.service.data.entity.HltMkab;
import ru.kostromin.caomi.integration.service.data.entity.HstCaomiAcceptReferral;
import ru.kostromin.caomi.integration.service.data.repository.AcceptAppointmentReferralRepository;
import ru.kostromin.caomi.integration.service.data.repository.HltMkabRepository;
import ru.kostromin.caomi.integration.service.data.repository.HstCaomiAcceptReferralRepository;
import ru.kostromin.caomi.integration.service.feign.AppointmentFeignClient;
import ru.kostromin.caomi.integration.service.feign.request.appointment.AppointmentRequest;
import ru.kostromin.caomi.integration.service.feign.request.appointment.AppointmentRequest.Patient;
import ru.kostromin.caomi.integration.service.feign.response.AppointmentResponse;

/**
 * Сервис поиска свободных слотов оборудования и отправки в 'Сервис записи' (на метод - POST /appointment)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentAcceptReferralService {

  private static final Integer STATUS_ID_EQUIPMENT_SLOTS_EMPTY = 2;
  private static final Integer STATUS_ID_SENT_SUCCESS = 1;
  private static final Integer MALE_GENDER_CODE = 1;
  private static final Integer FEMALE_GENDER_CODE = 2;
  private static final Map<Integer, String> GENDERS =
      Map.of(MALE_GENDER_CODE, "male", FEMALE_GENDER_CODE, "female");

  private final AcceptAppointmentReferralJobConfig config;
  private final HstCaomiAcceptReferralRepository hstCaomiAcceptReferralRepository;
  private final AcceptAppointmentReferralRepository appointmentReferralRepository;
  private final HltMkabRepository hltMkabRepository;
  private final AppointmentFeignClient appointmentFeignClient;

  /**
   * Найти рефералов и отправить в Внешнюю Систему/Шину (сервис 'appointment')
   */
  public void findAcceptReferralsAndSendToAppointment() {
    final Integer statusId = 0;
    final Integer limit = config.getSql().getLimit() == 0 ? Integer.MAX_VALUE : config.getSql().getLimit();
    final List<HstCaomiAcceptReferral> caomiAcceptReferrals = hstCaomiAcceptReferralRepository.findEntriesWithStatusIdOffsetAndLimit(
        statusId, config.getSql().getOffset(), limit
    );
    log.info("Найдено {} записей hst_caomiAcceptReferral с statusId = 0", caomiAcceptReferrals.size());
    caomiAcceptReferrals.forEach(hstCaomiAcceptReferral -> {
      // (A) Отобрать все записи с performerDeviceID = null
      if (hstCaomiAcceptReferral.getPerformerDeviceId() == null || hstCaomiAcceptReferral.getPerformerDeviceId().isEmpty()) {
        // Найти список доступных слотов оборудования
        final EquipmentSlotsDto equipmentSlotsDto = appointmentReferralRepository.findEquipmentDataByLaboratoryResearchId(
            config.getSql().getEquipmentSlotsSql(),
            hstCaomiAcceptReferral.getLbrLaboratoryResearchId()
        );
        // Слот отсутствует
        if (equipmentSlotsDto == null) {
          log.info("Для hst_caomiAcceptReferral с caomiAcceptReferralID = {} отсутсвуют доступные слоты", hstCaomiAcceptReferral.getId());
          hstCaomiAcceptReferral.setStatusId(STATUS_ID_EQUIPMENT_SLOTS_EMPTY);
          hstCaomiAcceptReferralRepository.save(hstCaomiAcceptReferral);
        }
        // Слоты присутствуют
        else {
          log.info("Для hst_caomiAcceptReferral с caomiAcceptReferralID = {} найден  доступный слот",
              hstCaomiAcceptReferral.getId());
          final AppointmentRequest request = createRequest(
              equipmentSlotsDto,
              hltMkabRepository.findById(equipmentSlotsDto.getRfMkabID()).orElse(new HltMkab()),
              retrieveIdPlace(equipmentSlotsDto.getDoctorTimeTableId()),
              hstCaomiAcceptReferral
          );
          final AppointmentResponse response = sendRequestAndReceiveResponse(request, hstCaomiAcceptReferral.getId());
          if (response != null) {
            hstCaomiAcceptReferral.setStatusId(STATUS_ID_SENT_SUCCESS);
            hstCaomiAcceptReferral.setPerformerDeviceId(equipmentSlotsDto.getEquipmentGuid());
            hstCaomiAcceptReferral.setErrorCode(response.getStatusCode());
            hstCaomiAcceptReferral.setErrorText(response.getComment());
            hstCaomiAcceptReferralRepository.save(hstCaomiAcceptReferral);
          }
        }
      }
      // (B) Отобрать все записи с performerDeviceID = ...
      // TODO - алгоритм для других performerDeviceID будет реализован в последующих задачах ...
    });
  }

  /**
   * Создать запрос на основе - доступного слота
   * @param equipmentSlotsDto - доступный слот
   * @param hltMkab - связанный МКАБ
   * @param idPlace - идентификатор места
   * @param caomiAcceptReferral - саязанный hst_caomiAcceptReferral
   * @return - запрос
   */
  private AppointmentRequest createRequest(
      EquipmentSlotsDto equipmentSlotsDto,
      HltMkab hltMkab,
      String idPlace,
      HstCaomiAcceptReferral caomiAcceptReferral) {

    final LocalDate dateSlot = equipmentSlotsDto.getDate() != null
        ? equipmentSlotsDto.getDate().toLocalDate() : null;
    final String idSchedule = equipmentSlotsDto.getExternalScheduleId() != null
        ? equipmentSlotsDto.getExternalScheduleId().toString() : null;
    final LocalDate birthDate = hltMkab.getBirthDate() != null
        ? hltMkab.getBirthDate().toLocalDate() : null;
    final String gender = Optional.ofNullable(hltMkab.getRfKlSexId()).map(GENDERS::get).orElse(null);

    return AppointmentRequest.builder()
        .id(caomiAcceptReferral.getCaomiId())
        .medicalServiceId(caomiAcceptReferral.getServiceCode())
        .referralNum(equipmentSlotsDto.getNumber())
        .patient(
            Patient.builder()
                .oms(hltMkab.getPolicyNumber())
                .birthDate(birthDate)
                .gender(gender)
                .name(hltMkab.getName())
                .surname(hltMkab.getLastName())
                .patronymic(hltMkab.getPatronymic())
                .build()
        )
        .dateSlot(dateSlot)
        .timeBegin(equipmentSlotsDto.getBeginTime())
        .idSchedule(idSchedule)
        .idSlot(equipmentSlotsDto.getUguid())
        .idPlace(idPlace)
        .build();
  }

  /**
   * Получить идентификатор места на основе идентификатора расписания
   * @param doctorTimeTableId - идентификатор расписания (hlt_DoctorTimeTable.DoctorTimeTableID)
   * @return идентификатор места/null
   */
  private String retrieveIdPlace(Integer doctorTimeTableId) {
    try{
      return appointmentReferralRepository.findIdPlaceByDoctorTimeTableId(
              config.getSql().getIdPlaceSql(),
              doctorTimeTableId)
          .getIdPlace();
    } catch (Exception e) {
      log.error("Непредвиденное исключение при попытке поиска idPlace (связанного с hlt_DoctorTimeTable.DoctorTimeTableID = {}): ",
          doctorTimeTableId, e);
      return null;
    }
  }

  /**
   * Отправить запрос в внешнюю систему
   * @param request - запрос
   * @param caomiAcceptReferralId - идентификатор hst_caomiAcceptReferral.caomiAcceptReferralID (для логирования)
   * @return ответ/null
   */
  private AppointmentResponse sendRequestAndReceiveResponse(AppointmentRequest request, Integer caomiAcceptReferralId) {
    try {
      final ResponseEntity<AppointmentResponse> responseEntity = appointmentFeignClient.postAppointment(request);
      if (!responseEntity.hasBody()) {
        log.error("Ошибка при получении ответа от внешнего сервиса - отсутсвует тело ответа");
        return null;
      }
      return responseEntity.getBody();
    } catch (Exception e){
      log.error("Непредвиденная ошибка при попытке отправить запрос/получить ответ"
          + "(связанный с hst_caomiAcceptReferral.id = {}):", caomiAcceptReferralId, e);
      return null;
    }
  }
}
