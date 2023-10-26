package ru.kostromin.caomi.integration.service.service.controller;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.kostromin.caomi.integration.service.controller.request.refreconciliation.ReconciliationReferralRequest;
import ru.kostromin.caomi.integration.service.controller.response.Response;
import ru.kostromin.caomi.integration.service.controller.response.Response.ErrorCode;
import ru.kostromin.caomi.integration.service.data.entity.HstCaomiReferral;
import ru.kostromin.caomi.integration.service.data.repository.HstCaomiReferralRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReconciliationReferralService {

  private final static Integer RECONCILIATION_STATUS_ID = 2;

  private final HstCaomiReferralRepository caomiReferralRepository;

  /**
   * Обработать запрос ReconciliationReferralRequest (обновление таблицы hst_caomiReferral)
   * @param request - запрос
   * @return - ответ
   */
  public Response processReconciliationReferralRequest(ReconciliationReferralRequest request) {
    findEntriesToUpdateByCaomiId(request.getIdReferral().toString())
        .forEach(hstCaomiReferral -> {
          hstCaomiReferral.setStatusId(RECONCILIATION_STATUS_ID);
          hstCaomiReferral.setAgreedReferral(request.getAgreedReferral());
          hstCaomiReferral.setRejectionReason(request.getRejectionReason()
              .substring(0, Math.min(request.getRejectionReason().length(), 200))
          );
          hstCaomiReferral.setDateStart(request.getOcurrencePeriod().getStart().atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime());
          hstCaomiReferral.setDateEnd(request.getOcurrencePeriod().getEnd().atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime());
          caomiReferralRepository.save(hstCaomiReferral);
        });
    return Response.builder()
        .errorCode(ErrorCode.E000.getCode())
        .errorText(ErrorCode.E000.getMessage())
        .dateTime(LocalDateTime.now())
        .idReferral(request.getIdReferral())
        .build();
  }

  private List<HstCaomiReferral> findEntriesToUpdateByCaomiId(String caomiId) {
    final List<HstCaomiReferral> foundEntries = caomiReferralRepository.findByCaomiId(caomiId);
    log.info("Найдено {} записей hst_caomiReferral", foundEntries.size());
    return foundEntries;
  }
}
