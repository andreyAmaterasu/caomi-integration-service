package ru.kostromin.caomi.integration.service.service.job;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.kostromin.caomi.integration.service.config.ReconciliationReferralJobConfig;
import ru.kostromin.caomi.integration.service.data.dto.ReconciliationDto;
import ru.kostromin.caomi.integration.service.data.entity.HstCaomiAcceptReferral;
import ru.kostromin.caomi.integration.service.data.repository.HstCaomiAcceptReferralRepository;
import ru.kostromin.caomi.integration.service.data.repository.ReconciliationReferralRepository;
import ru.kostromin.caomi.integration.service.feign.CaomiFeignClient;
import ru.kostromin.caomi.integration.service.feign.request.reconciliation.Device;
import ru.kostromin.caomi.integration.service.feign.request.reconciliation.OccurrencePeriod;
import ru.kostromin.caomi.integration.service.feign.request.reconciliation.CaomiReconciliationReferralRequest;
import ru.kostromin.caomi.integration.service.feign.response.CaomiBasicResponse;

/**
 * Сервис поиска данных расписания и отправки в ЦАМИ (на метод - POST reconciliationReferral)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CaomiReconciliationReferralService {

  private static final Integer SCHEDULED_REFERRAL_ID = 1;
  private static final Integer NO_FREE_SLOTS_REFERRAL = 2;
  private static final Integer SCHEDULED_REFERRAL_SENT_STATUS_ID = 5;
  private static final Integer NO_FREE_SLOTS_REFERRAL_SENT_STATUS_ID = 6;

  private final ReconciliationReferralJobConfig config;
  private final HstCaomiAcceptReferralRepository caomiAcceptReferralRepository;
  private final ReconciliationReferralRepository reconciliationRepository;
  private final CaomiFeignClient feignClient;

  /**
   * Найти информацию о "времени приема на инструментальное исследование поставленную в раписание"
   * и отправить в ЦАМИ
   */
  public void findReconciliationReferralsScheduledAndSendToCaomi() {
    // (1) найти все hst_caomiAcceptReferral для которых statusId = 1 ("Поставлено в расписание")
    final List<HstCaomiAcceptReferral> scheduledReferals = caomiAcceptReferralRepository.findEntriesWithStatusIdOffsetAndLimit(
        SCHEDULED_REFERRAL_ID,
        config.getSql().getOffset(),
        config.getSql().getLimit()
    );
    log.info("Найдено {} \"Поставлено в расписание\" рефералов", scheduledReferals.size());
    scheduledReferals.forEach(hstCaomi -> processReferral(hstCaomi, SCHEDULED_REFERRAL_SENT_STATUS_ID));
    log.info("\"Поставлено в расписание\" рефералы обработаны");
  }

  /**
   * Найти информацию о "времени приема на инструментальное исследование где нет свободных слотов в расписании"
   * и отправить в ЦАМИ
   */
  public void findReconciliationReferralsNoSlotsAvailableAndSendToCaomi() {
    // (1) найти все hst_caomiAcceptReferral для которых statusId = 2 ("Нет свободных слотов в расписании")
    final Integer limit = config.getSql().getLimit() == 0 ? Integer.MAX_VALUE : config.getSql().getLimit();
    final List<HstCaomiAcceptReferral> noFreeSlotsReferals = caomiAcceptReferralRepository
        .findEntriesWithStatusIdOffsetAndLimit(
            NO_FREE_SLOTS_REFERRAL,
            config.getSql().getOffset(),
            limit
        );
    log.info("Найдено {} \"Нет свободных слотов в расписании\" рефералов", noFreeSlotsReferals.size());
    noFreeSlotsReferals.forEach(hstCaomi -> processReferral(hstCaomi, NO_FREE_SLOTS_REFERRAL_SENT_STATUS_ID));
    log.info("\"Нет свободных слотов в расписании\" рефералы обработаны");
  }

  private void processReferral(HstCaomiAcceptReferral entryToProcess, Integer statusId) {
    // (2) обратиться за данными для конкретного hst_caomiAcceptReferral.lbrLaboratoryResearchID
    Optional.ofNullable(getReconciliationDto(entryToProcess.getLbrLaboratoryResearchId()))
        .ifPresentOrElse(reconciliationDto -> {
          // (3) собрать запрос
          final CaomiReconciliationReferralRequest request = createRequest(reconciliationDto, statusId);
          if (request != null) {
            // (4) отправить запрос в ЦАМИ
            sendRequestToCaomiAndSaveResponse(request, entryToProcess, statusId);
          }
        }, () -> {
          log.warn("Не найдено данных 'расписания' (для lbrLaboratoryResearchID = {}): ",
              entryToProcess.getLbrLaboratoryResearchId());
        });
  }

  /**
   * Получить данные 'раписания' из БД
   * @param lbrResearchId - hst_caomiAcceptReferral.lbrLaboratoryResearchID
   * @return - данные 'расписания'
   */
  private ReconciliationDto getReconciliationDto(Integer lbrResearchId) {
    try {
      return reconciliationRepository.getReconciliationReferralByLabResId(
          config.getSql().getReconcilationReferralSql(),
          lbrResearchId);
    } catch (Exception e) {
      log.error("Непредвиденное исключение при попытке обратиться за данными 'рапсисания' "
          + "(по lbrLaboratoryResearchID = {}): ", lbrResearchId, e);
      return null;
    }
  }

  /**
   * Собрать запрос в ЦАМИ
   * @param dto - данные БД
   * @return - запрос в ЦАМИ
   */
  private CaomiReconciliationReferralRequest createRequest(ReconciliationDto dto, Integer statusId) {
    try{
      final CaomiReconciliationReferralRequest request = CaomiReconciliationReferralRequest.builder()
          .idReferral(dto.getIdReferral())
          .agreedReferral(dto.getAgreedReferral())
          .rejectionReason(dto.getRejectionReason())
          .build();
      if (Objects.equals(statusId, SCHEDULED_REFERRAL_SENT_STATUS_ID)) {
        request.setOcurrencePeriod(
            OccurrencePeriod.builder()
                .start(
                    StringUtils.hasText(dto.getOccurrencePeriodStart()) ?
                        LocalDateTime.parse(dto.getOccurrencePeriodStart(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                            .atZone(ZoneId.systemDefault()).toOffsetDateTime()
                        : null)
                .end(
                    StringUtils.hasText(dto.getOccurrencePeriodEnd()) ?
                        LocalDateTime.parse(dto.getOccurrencePeriodEnd(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                            .atZone(ZoneId.systemDefault()).toOffsetDateTime()
                        : null)
                .build()
        );
        request.setDevice(
            Device.builder()
            .deviceMisId(dto.getDeviceMisId())
            .owner(dto.getOwner())
            .deviceName(dto.getDeviceName())
            .build()
        );
      }
      return request;
    } catch (Exception e) {
      log.error("Непредвиденное исключение при формировании запроса в ЦАМИ для idReferral = {}: ",
          dto.getIdReferral(), e);
      return null;
    }
  }

  /**
   * Отправить запрос в ЦАМИ и обработать ответ
   * @param request - запрос в ЦАМИ
   * @param entryToUpdate - запись для обновления
   */
  private void sendRequestToCaomiAndSaveResponse(CaomiReconciliationReferralRequest request, HstCaomiAcceptReferral entryToUpdate,
      Integer statusId) {
    try {
      final ResponseEntity<CaomiBasicResponse> responseEntity = feignClient.postReconciliationReferral(request);
      if(responseEntity.hasBody()){
        // (5) сохранить ответ от запроса в hst_caomiAcceptReferral
        updateHstCaomiEntry(responseEntity.getBody(), entryToUpdate, statusId);
      } else {
        log.error("Отсутвует тело ответа ЦАМИ, необходимое для сохранения в БД! (caomiAcceptReferralID = {})",
            entryToUpdate.getId());
      }
    } catch (Exception e){
      log.error("Непредвиденная ошибка при отправке запроса/получении ответа от ЦАМИ (caomiAcceptReferralID = {}): ",
          entryToUpdate.getId(), e);
      updateCaomiUnexpectedError(e.getMessage(), entryToUpdate);
    }
  }

  /**
   * Обновить запись hst_caomiAcceptReferral
   * @param response - ЦАМИ ответ
   * @param entryToUpdate - запись для обновления
   */
  private void updateHstCaomiEntry(CaomiBasicResponse response, HstCaomiAcceptReferral entryToUpdate,
      Integer statusId) {
    try {
      final String errorText = response.getErrorText() != null
          ? response.getErrorText().substring(0, Math.min(response.getErrorText().length(), 200))
          : "";
      final String errorCode = response.getErrorCode() != null
          ? response.getErrorCode().substring(0, Math.min(response.getErrorCode().length(), 100))
          : "";
      entryToUpdate.setStatusId(statusId);
      entryToUpdate.setErrorCode(errorCode);
      entryToUpdate.setErrorText(errorText);
      caomiAcceptReferralRepository.save(entryToUpdate);
    } catch (Exception e){
      log.error("Непредвиденная ошибка при попытке обновления hst_caomiAcceptReferral (caomiAcceptReferralID = {}): ",
          entryToUpdate.getId(), e);
    }
  }

  /**
   * Обновить запись hst_caomiAcceptReferral при непредвиденное ошибке
   * @param unexpectedException - непредвиденное исключение
   * @param entryToUpdate - запись для обновления
   */
  private void updateCaomiUnexpectedError(String unexpectedException, HstCaomiAcceptReferral entryToUpdate) {
    try {
      final String errorText = unexpectedException != null
          ? unexpectedException.substring(0, Math.min(unexpectedException.length(), 200))
          : "";
      entryToUpdate.setErrorCode("");
      entryToUpdate.setErrorText(errorText);
      caomiAcceptReferralRepository.save(entryToUpdate);
    } catch (Exception e){
      log.error("Непредвиденная ошибка при попытке обновления hst_caomiAcceptReferral (caomiAcceptReferralID = {}): ",
          entryToUpdate.getId(), e);
    }
  }
}
