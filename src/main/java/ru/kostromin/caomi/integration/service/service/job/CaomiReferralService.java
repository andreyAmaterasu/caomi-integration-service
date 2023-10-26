package ru.kostromin.caomi.integration.service.service.job;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.kostromin.caomi.integration.service.config.AcceptReferralJobConfig;
import ru.kostromin.caomi.integration.service.data.dto.PatientDto;
import ru.kostromin.caomi.integration.service.data.dto.ReferralDto;
import ru.kostromin.caomi.integration.service.data.dto.ServiceRequestDto;
import ru.kostromin.caomi.integration.service.data.entity.HstCaomiReferral;
import ru.kostromin.caomi.integration.service.data.repository.HstCaomiReferralCustomRepository;
import ru.kostromin.caomi.integration.service.data.repository.HstCaomiReferralRepository;
import ru.kostromin.caomi.integration.service.feign.CaomiFeignClient;
import ru.kostromin.caomi.integration.service.feign.request.referral.CaomiReferralRequest;
import ru.kostromin.caomi.integration.service.feign.request.referral.Coverage;
import ru.kostromin.caomi.integration.service.feign.request.referral.CoverageValidityPeriod;
import ru.kostromin.caomi.integration.service.feign.request.referral.Patient;
import ru.kostromin.caomi.integration.service.feign.request.referral.ServiceRequest;
import ru.kostromin.caomi.integration.service.feign.request.referral.ServiceRequestDesiredPeriod;
import ru.kostromin.caomi.integration.service.feign.response.CaomiBasicResponse;

/**
 * Сервис поиска данных направления для отправки в ЦАМИ (на метод POST acceptReferral)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CaomiReferralService {

  private final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  private final AcceptReferralJobConfig config;
  private final HstCaomiReferralRepository referralRepository;
  private final HstCaomiReferralCustomRepository referralCustomRepository;
  private final CaomiFeignClient feignClient;

  /**
   *  поиск и отправка данных о создании направления на инструментальное исследование
   */
  public void findAndSendReferrals(){
    // (1) Найти все hst_caomiReferral с statusId = 0
    log.info("Обращение за hst_caomiReferral в БД...");
    List<HstCaomiReferral> referrals = getReferralsToProcess(config.getSql().getOffset(), config.getSql().getLimit());
    if(referrals != null && !referrals.isEmpty()){
      log.info("Извлечено {} hst_caomiReferral из БД", referrals.size());
      referrals.forEach(referral -> {
        Integer labResId = referral.getLbrLaboratoryResearchId();
        Integer caomiRefId = referral.getCaomiReferralId();
        // (2) собрать referral данные
        ReferralDto referralDto = getReferralDtoData(config.getSql().getReferralSql(), labResId, caomiRefId);
        if(referralDto != null){
          // (3) собрать patient данные
          PatientDto patientDto = getPatientDtoData(config.getSql().getPatientSql(), labResId, caomiRefId);
          if(patientDto != null){
            // (4) собрать serviceRequest данные
            ServiceRequestDto serviceRequestDto = getServiceRequestDto(config.getSql().getServiceRequestSql(), labResId, caomiRefId);
            if (serviceRequestDto != null){
              // (5) собрать Json для отправки в ЦАМИ
              CaomiReferralRequest request = createCaomiReferralRequest(referralDto, patientDto,
                  serviceRequestDto, caomiRefId);
              if (request != null){
                // (6) отправить запрос в ЦАМИ
                sendRequestAndProcessResponse(request, caomiRefId);
              }
            }
          }
        }
      });
    } else {
      log.info("Для отправки данных в ЦАМИ не было найдено данных в БД.");
    }
  }

  private List<HstCaomiReferral> getReferralsToProcess(int offset, int limit) {
    try {
      limit = limit != 0 ? limit : Integer.MAX_VALUE;
      return referralRepository.getReferralsToProcessByLimitAndOffset(offset, limit);
    } catch (Exception e){
      log.error("Произошла ошибка при получении hst_caomiReferral с statusID = 0: ", e);
      return null;
    }
  }

  /**
   * Получение referral-данных по LaboratoryResearchId
   * @param referralSql - sql запрос
   * @param labResId - lbr_LaboratoryResearch.LaboratoryResearchId
   * @param caomiRefId - идентификатор реферала hst_caomiReferral
   * @return referral-данные
   */
  private ReferralDto getReferralDtoData(String referralSql, Integer labResId, Integer caomiRefId) {
    try{
      return referralCustomRepository.getReferralDataByLaboratoryResearchId(referralSql, labResId);
    } catch (Exception e){
      log.error("Непредвиденное исключение при попытке получения refferal данных из БД "
              + "(связанным с hst_caomiReferral.caomiReferralID = {}): ",
          caomiRefId,
          e);
      return null;
    }
  }

  /**
   * Получение patient-данных по LaboratoryResearchId
   * @param patientSql - sql запрос
   * @param labResId - lbr_LaboratoryResearch.LaboratoryResearchId
   * @param caomiRefId - идентификатор реферала hst_caomiReferral
   * @return patient-данные
   */
  private PatientDto getPatientDtoData(String patientSql, Integer labResId, Integer caomiRefId) {
    try{
      return referralCustomRepository.getPatientDataByLaboratoryResearchId(patientSql, labResId);
    } catch (Exception e){
      log.error("Непредвиденное исключение при попытке получения patient данных из БД "
              + "(связанным с hst_caomiReferral.caomiReferralID = {}): ",
          caomiRefId,
          e);
      return null;
    }
  }

  /**
   * Получение serviceRequest-данных по LaboratoryResearchId
   * @param serviceRequestSql - sql запрос
   * @param labResId - lbr_LaboratoryResearch.LaboratoryResearchId
   * @param caomiRefId - идентификатор реферала hst_caomiReferral
   * @return - serviceRequest-данные
   */
  private ServiceRequestDto getServiceRequestDto(String serviceRequestSql, Integer labResId, Integer caomiRefId) {
    try{
      return referralCustomRepository.getServiceRequestDataByLaboratoryResearchId(serviceRequestSql, labResId);
    } catch (Exception e){
      log.error("Непредвиденное исключение при попытке получения serviceRequest данных из БД "
              + "(связанным с hst_caomiReferral.caomiReferralID = {}): ",
          caomiRefId,
          e);
      return null;
    }
  }

  /**
   * Создание запроса в ЦАМИ
   * @param referralDto - 'referral данные'
   * @param patientDto - 'patient данные'
   * @param serviceRequestDto - 'serviceRequest данные'
   * @param caomiRefId - идентификатор реферала (таблицы hst_caomiReferral)
   * @return - запрос в ЦАМИ
   */
  private CaomiReferralRequest createCaomiReferralRequest(ReferralDto referralDto, PatientDto patientDto,
      ServiceRequestDto serviceRequestDto, Integer caomiRefId) {
    try{
      return CaomiReferralRequest.builder()
          // referral
          .moOid(referralDto.getMoOid())
          .referralNumber(referralDto.getReferralNumber())
          .practitioner(
              StringUtils.hasText(referralDto.getPractitioner())
                  ? Long.valueOf(referralDto.getPractitioner())
                  : null)
          .practitionerRole(referralDto.getPractitionerRole())
          // patient
          .patient(
              Patient.builder()
                  .patientId(patientDto.getId())
                  .gender(
                      StringUtils.hasText(patientDto.getGender())
                          ? Long.valueOf(patientDto.getGender())
                          : null)
                  .birthDate(
                      StringUtils.hasText(patientDto.getBirthDate())
                          ? LocalDate.parse(patientDto.getBirthDate(), DATE_FORMAT)
                          : null)
                  .generalPractitionerMoOid(patientDto.getGeneralPractitionerMoOid())
                  .build())
          .coverage(
              Coverage.builder()
                  .policyTypeCode(patientDto.getPolicyTypeCode())
                  .policyNumber(patientDto.getPolicyNumber())
                  .policyStatus(patientDto.getPolicyStatus() != null ? (patientDto.getPolicyStatus() ? 1L : 0L) : null)
                  .validityPeriod(
                      CoverageValidityPeriod.builder()
                          .start(
                              patientDto.getValidityPeriodStart() != null
                                  ? patientDto.getValidityPeriodStart().toLocalDate()
                                  : null)
                          .end(
                              patientDto.getValidityPeriodEnd() != null
                                  ? patientDto.getValidityPeriodEnd().toLocalDate()
                                  : null)
                          .build())
                  .medicalInsuranceOrganizationCode(patientDto.getMedInsuranceOrgCode())
                  .build())
          // serviceRequest
          .serviceRequest(
              List.of(
                  ServiceRequest.builder()
                      .serviceId(serviceRequestDto.getId())
                      .serviceStatusCode(serviceRequestDto.getStatusCode())
                      .serviceIntentCode(
                          StringUtils.hasText(serviceRequestDto.getIntentCode())
                              ? Long.valueOf(serviceRequestDto.getIntentCode())
                              : null
                          )
                      .servicePriorityCode(serviceRequestDto.getPriorityCode())
                      .serviceCode(serviceRequestDto.getCode())
                      .authoredOn(
                          serviceRequestDto.getAuthOnDate() != null
                              ? serviceRequestDto
                                  .getAuthOnDate()
                                  .atZone(ZoneId.systemDefault())
                                  .toOffsetDateTime()
                              : null)
                      .desiredPeriod(
                          ServiceRequestDesiredPeriod.builder()
                              .start(
                                  serviceRequestDto.getDesiredPeriodStart() != null
                                      ? serviceRequestDto
                                          .getDesiredPeriodStart()
                                          .atZone(ZoneId.systemDefault())
                                          .toOffsetDateTime()
                                      : null)
                              .end(
                                  serviceRequestDto.getDesiredPeriodEnd() != null
                                      ? serviceRequestDto
                                          .getDesiredPeriodEnd()
                                          .atZone(ZoneId.systemDefault())
                                          .toOffsetDateTime()
                                      : null)
                              .build())
                      .performerTypeCode(
                          StringUtils.hasText(serviceRequestDto.getPerformerTypeCode())
                              ? Long.valueOf(serviceRequestDto.getPerformerTypeCode())
                              : null)
                      .performerDeviceId(serviceRequestDto.getPerformerDeviceId())
                      .performerMoOid(serviceRequestDto.getPerformerMoOid())
                      .reasonCode(serviceRequestDto.getReasonCode())
                      .build()))
          .build();
    }
    catch (Exception e){
      log.error("Ошибка при формировании запроса в ЦАМИ (связанного с hst_caomiReferral.caomiReferralID = {}): ", caomiRefId, e);
      return null;
    }
  }

  /**
   * Отправить запрос в ЦАМИ и обработать ответ
   * @param request - запрос в ЦАМИ
   * @param caomiRefId - идентификатор реферала (таблицы hst_caomiReferral)
   */
  private void sendRequestAndProcessResponse(CaomiReferralRequest request, Integer caomiRefId) {
    try{
      ResponseEntity<CaomiBasicResponse> responseEntity = feignClient.acceptReferral(request);
      if(responseEntity.hasBody()){
        // (7) сохранить ответ от запроса в hst_caomiReferral
        CaomiBasicResponse response = responseEntity.getBody();
        saveCaomiRefferalResponse(caomiRefId, response, responseEntity.getStatusCodeValue());
      } else {
        log.error("Отсутвует тело ответа ЦАМИ, необходимое для сохранения в БД!");
      }
    } catch (Exception e){
      log.error("Непредвиденная ошибка при отправке запроса/получении ответа от ЦАМИ: ", e);
      saveCaomiUnexpectedError(caomiRefId, e);
    }
  }

  /**
   * Сохранить ответ от ЦАМИ
   * @param caomiRefId - идентификатор hst_caomiReferral
   * @param response - ответ от ЦАМИ
   * @param httpCode - http код
   */
  private void saveCaomiRefferalResponse(Integer caomiRefId, CaomiBasicResponse response, Integer httpCode) {
    try {
      Optional<HstCaomiReferral> referralOpt =  referralRepository.findById(caomiRefId);
      referralOpt.ifPresentOrElse(
          referral -> {
            String errorText = response.getErrorText()
                .substring(0, Math.min(response.getErrorText().length(), 200));
            referral.setCaomiId(response.getIdReferral());
            referral.setErrorCode(response.getErrorCode());
            referral.setErrorText(errorText);
            referral.setDateStatus(LocalDateTime.now());
            referral.setHttpStatus(httpCode);
            referral.setStatusId(1);
            referralRepository.save(referral);
          },
          () -> log.error("Не найдена запись hst_caomiReferral для обновления с caomiReferralID ={}", caomiRefId));
    } catch (Exception e){
      log.error("Произошла непредвиденная ошибка при попытке сохранить ответ от ЦАМИ "
          + "(связанный с hst_caomiReferral.caomiReferralID = {}): ", caomiRefId, e);
    }
  }

  /**
   * Сохранить информацию о непредвиденной ошибке
   * @param caomiRefId - идентификатор hst_caomiReferral
   * @param unexpectedException - исключение
   */
  private void saveCaomiUnexpectedError(Integer caomiRefId, Exception unexpectedException) {
    try {
      Optional<HstCaomiReferral> referralOpt = referralRepository.findById(caomiRefId);
      referralOpt.ifPresentOrElse(device -> {
            String errorText = unexpectedException.toString()
                .substring(0, Math.min(unexpectedException.toString().length(), 200));
            device.setHttpStatus(0);
            device.setErrorCode("");
            device.setErrorText(errorText);
            device.setDateStatus(LocalDateTime.now());
            referralRepository.save(device);
          },
          () -> log.error("Не найдена запись hst_caomiReferral для обновления с caomiReferralID ={}", caomiRefId));
    } catch (Exception e){
      log.error("Произошла непредвиденная ошибка при попытке сохранить ответ от ЦАМИ "
          + "(связанный с hst_caomiReferral.caomiReferralID = {}): ", caomiRefId, e);
    }
  }
}
