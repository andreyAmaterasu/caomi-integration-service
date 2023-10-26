package ru.kostromin.caomi.integration.service.service.controller;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import ru.kostromin.caomi.integration.service.controller.request.refreconciliation.OccurrencePeriod;
import ru.kostromin.caomi.integration.service.controller.request.refreconciliation.ReconciliationReferralRequest;
import ru.kostromin.caomi.integration.service.data.entity.HstCaomiReferral;
import ru.kostromin.caomi.integration.service.data.repository.HstCaomiReferralRepository;

class ReconciliationReferralServiceTest {

  private AutoCloseable mocks;
  @Mock
  private HstCaomiReferralRepository caomiReferralRepository;

  private ReconciliationReferralService service;

  @BeforeEach
  void beforeEach() {
    mocks = MockitoAnnotations.openMocks(this);
    service = new ReconciliationReferralService(caomiReferralRepository);
  }

  @Test
  @DisplayName("Обработать запрос ReconciliationReferralRequest."
      + "Записи hst_caomiReferral найдены и обновлены данными из запроса")
  void processReconciliationReferralRequestEntriesFound_entriesUpdated() {
    // (1) prepare mocks:
    final HstCaomiReferral entry = new HstCaomiReferral();
    final List<HstCaomiReferral> foundEntries = List.of(entry);
    Mockito.when(caomiReferralRepository.findByCaomiId(Mockito.anyString()))
        .thenReturn(foundEntries);

    // (2) start test:
    final ReconciliationReferralRequest request = ReconciliationReferralRequest.builder()
        .idReferral(UUID.randomUUID())
        .agreedReferral(false)
        .rejectionReason("durak")
        .ocurrencePeriod(
            OccurrencePeriod.builder()
                .start(OffsetDateTime.parse("2023-06-02T07:00:00+10:00"))
                .end(OffsetDateTime.parse("2023-06-02T07:15:00+10:00"))
                .build()
        )
        .build();
    service.processReconciliationReferralRequest(request);

    // (3) check:
    Mockito.verify(caomiReferralRepository, Mockito.times(1))
        .findByCaomiId(Mockito.anyString());
    final ArgumentCaptor<HstCaomiReferral> saveCaptor = ArgumentCaptor.forClass(HstCaomiReferral.class);
    Mockito.verify(caomiReferralRepository, Mockito.times(1))
        .save(saveCaptor.capture());
    Assertions.assertEquals(entry, saveCaptor.getValue());
    Assertions.assertEquals(request.getAgreedReferral(),
        saveCaptor.getValue().getAgreedReferral());
    Assertions.assertEquals(request.getRejectionReason(),
        saveCaptor.getValue().getRejectionReason());
    Assertions.assertEquals(request.getOcurrencePeriod().getStart().atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime(),
        saveCaptor.getValue().getDateStart());
    Assertions.assertEquals(request.getOcurrencePeriod().getEnd().atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime(),
        saveCaptor.getValue().getDateEnd());
  }

  @Test
  @DisplayName("Обработать запрос ReconciliationReferralRequest."
      + "Записи hst_caomiReferral НЕ найдены и НЕ обновлены данными из запроса")
  void processReconciliationReferralRequestEntriesNotFound_entriesNotUpdated() {
    // (1) prepare mocks:
    Mockito.when(caomiReferralRepository.findByCaomiId(Mockito.anyString()))
        .thenReturn(List.of());

    // (2) start test:
    final ReconciliationReferralRequest request = ReconciliationReferralRequest.builder()
        .idReferral(UUID.randomUUID())
        .agreedReferral(false)
        .rejectionReason("durak")
        .ocurrencePeriod(
            OccurrencePeriod.builder()
                .start(OffsetDateTime.MIN)
                .end(OffsetDateTime.MAX)
                .build()
        )
        .build();
    service.processReconciliationReferralRequest(request);

    // (3) check:
    Mockito.verify(caomiReferralRepository, Mockito.times(1))
        .findByCaomiId(Mockito.anyString());
    Mockito.verify(caomiReferralRepository, Mockito.times(0))
        .save(Mockito.any(HstCaomiReferral.class));
  }

}