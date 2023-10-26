package ru.kostromin.caomi.integration.service.service.job;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import ru.kostromin.caomi.integration.service.config.ReconciliationReferralJobConfig;
import ru.kostromin.caomi.integration.service.config.ReconciliationReferralJobConfig.DataQueries;
import ru.kostromin.caomi.integration.service.data.dto.ReconciliationDto;
import ru.kostromin.caomi.integration.service.data.entity.HstCaomiAcceptReferral;
import ru.kostromin.caomi.integration.service.data.repository.HstCaomiAcceptReferralRepository;
import ru.kostromin.caomi.integration.service.data.repository.ReconciliationReferralRepository;
import ru.kostromin.caomi.integration.service.feign.CaomiFeignClient;
import ru.kostromin.caomi.integration.service.feign.request.reconciliation.CaomiReconciliationReferralRequest;
import ru.kostromin.caomi.integration.service.feign.response.CaomiBasicResponse;

class CaomiReconciliationReferralServiceTest {

  private static final Integer SCHEDULED_REFERRAL_SENT_STATUS_ID = 5;
  private static final Integer NO_FREE_SLOTS_REFERRAL_SENT_STATUS_ID = 6;

  private ReconciliationReferralJobConfig config;
  @Mock
  private HstCaomiAcceptReferralRepository caomiAcceptReferralRepository;
  @Mock
  private ReconciliationReferralRepository reconciliationRepository;
  @Mock
  private CaomiFeignClient feignClient;

  private AutoCloseable mocks;

  private CaomiReconciliationReferralService service;

  @BeforeEach
  void beforeEach() {
    config = new ReconciliationReferralJobConfig();
    final DataQueries dataQueries = new DataQueries();
    dataQueries.setLimit(1);
    dataQueries.setOffset(0);
    dataQueries.setReconcilationReferralSql("some sql");
    config.setSql(dataQueries);
    mocks = MockitoAnnotations.openMocks(this);
    service = new CaomiReconciliationReferralService(config, caomiAcceptReferralRepository,
        reconciliationRepository, feignClient);
  }

  @Test
  @DisplayName("\"Поставлено в раписание\" рефералы не найдены."
      + "Запрос не отправлен."
      + "hst_caomiAcceptReferral не обновлена.")
  void scheduledReferralsNotFound_requestNotSentEntryNotUpdated() {
    // (1) prepare mocks:
    Mockito.when(caomiAcceptReferralRepository.findEntriesWithStatusIdOffsetAndLimit(
        Mockito.anyInt(),
        Mockito.anyInt(),
        Mockito.anyInt()
    )).thenReturn(List.of());

    // (2) start:
    service.findReconciliationReferralsScheduledAndSendToCaomi();

    // (3) check:
    Mockito.verify(caomiAcceptReferralRepository, Mockito.times(1))
        .findEntriesWithStatusIdOffsetAndLimit(
            Mockito.anyInt(),
            Mockito.anyInt(),
            Mockito.anyInt());
    Mockito.verify(reconciliationRepository, Mockito.times(0))
            .getReconciliationReferralByLabResId(
                Mockito.anyString(),
                Mockito.anyInt());
    Mockito.verify(feignClient, Mockito.times(0))
            .postReconciliationReferral(Mockito.any(CaomiReconciliationReferralRequest.class));
    Mockito.verify(caomiAcceptReferralRepository, Mockito.times(0))
        .save(Mockito.any(HstCaomiAcceptReferral.class));
  }


  @Test
  @DisplayName("\"Поставлено в раписание\" рефералы найдены."
      + "Данные для создания запроса не найдены."
      + "Запрос не отправлен."
      + "hst_caomiAcceptReferral не обновлена.")
  void scheduledReferralsFoundDatabaseDataForRequestNotFound_requestNotSentEntryNotUpdated() {
    // (1) prepare mocks:
    final HstCaomiAcceptReferral hstCaomiAcceptReferral = new HstCaomiAcceptReferral();
    hstCaomiAcceptReferral.setLbrLaboratoryResearchId(1);
    Mockito.when(caomiAcceptReferralRepository.findEntriesWithStatusIdOffsetAndLimit(
        Mockito.anyInt(),
        Mockito.anyInt(),
        Mockito.anyInt()
    )).thenReturn(List.of(hstCaomiAcceptReferral));

    Mockito.when(reconciliationRepository.getReconciliationReferralByLabResId(
        Mockito.anyString(),
        Mockito.anyInt()
    )).thenReturn(null);

    // (2) start:
    service.findReconciliationReferralsScheduledAndSendToCaomi();

    // (3) check:
    Mockito.verify(caomiAcceptReferralRepository, Mockito.times(1))
        .findEntriesWithStatusIdOffsetAndLimit(
            Mockito.anyInt(),
            Mockito.anyInt(),
            Mockito.anyInt());
    Mockito.verify(reconciliationRepository, Mockito.times(1))
        .getReconciliationReferralByLabResId(
            Mockito.anyString(),
            Mockito.anyInt());
    Mockito.verify(feignClient, Mockito.times(0))
        .postReconciliationReferral(Mockito.any(CaomiReconciliationReferralRequest.class));
    Mockito.verify(caomiAcceptReferralRepository, Mockito.times(0))
        .save(Mockito.any(HstCaomiAcceptReferral.class));
  }

  @Test
  @DisplayName("\"Поставлено в раписание\" рефералы найдены."
      + "При поиске данных для создания запроса в БД было выброшено непредвиденное исключение."
      + "Запрос не отправлен."
      + "hst_caomiAcceptReferral не обновлена.")
  void scheduledReferralsFoundDatabaseDataForRequestThrewException_requestNotSentEntryNotUpdated() {
    // (1) prepare mocks:
    final HstCaomiAcceptReferral hstCaomiAcceptReferral = new HstCaomiAcceptReferral();
    hstCaomiAcceptReferral.setLbrLaboratoryResearchId(1);
    Mockito.when(caomiAcceptReferralRepository.findEntriesWithStatusIdOffsetAndLimit(
        Mockito.anyInt(),
        Mockito.anyInt(),
        Mockito.anyInt()
    )).thenReturn(List.of(hstCaomiAcceptReferral));

    Mockito.doThrow(new RuntimeException(""))
        .when(reconciliationRepository)
        .getReconciliationReferralByLabResId(
            Mockito.anyString(),
            Mockito.anyInt()
        );

    // (2) start:
    service.findReconciliationReferralsScheduledAndSendToCaomi();

    // (3) check:
    Mockito.verify(caomiAcceptReferralRepository, Mockito.times(1))
        .findEntriesWithStatusIdOffsetAndLimit(
            Mockito.anyInt(),
            Mockito.anyInt(),
            Mockito.anyInt());
    Mockito.verify(reconciliationRepository, Mockito.times(1))
        .getReconciliationReferralByLabResId(
            Mockito.anyString(),
            Mockito.anyInt());
    Mockito.verify(feignClient, Mockito.times(0))
        .postReconciliationReferral(Mockito.any(CaomiReconciliationReferralRequest.class));
    Mockito.verify(caomiAcceptReferralRepository, Mockito.times(0))
        .save(Mockito.any(HstCaomiAcceptReferral.class));
  }

  @Test
  @DisplayName("\"Поставлено в раписание\" рефералы найдены."
      + "Данные для создания запроса найдены."
      + "При создании запроса произошла непредвиденная ошибка."
      + "Запрос не отправлен."
      + "hst_caomiAcceptReferral не обновлена.")
  void scheduledReferralsFoundDataFoundRequestCreationThrewException_requestNotSentEntryNotUpdated() {
    // (1) prepare mocks:
    final HstCaomiAcceptReferral hstCaomiAcceptReferral = new HstCaomiAcceptReferral();
    hstCaomiAcceptReferral.setLbrLaboratoryResearchId(1);
    Mockito.when(caomiAcceptReferralRepository.findEntriesWithStatusIdOffsetAndLimit(
        Mockito.anyInt(),
        Mockito.anyInt(),
        Mockito.anyInt()
    )).thenReturn(List.of(hstCaomiAcceptReferral));

    final ReconciliationDto flawedDto = new ReconciliationDto();
    flawedDto.setOccurrencePeriodEnd("123");
    Mockito.when(reconciliationRepository.getReconciliationReferralByLabResId(
        Mockito.anyString(),
        Mockito.anyInt()
    )).thenReturn(flawedDto);

    // (2) start:
    service.findReconciliationReferralsScheduledAndSendToCaomi();

    // (3) check:
    Mockito.verify(caomiAcceptReferralRepository, Mockito.times(1))
        .findEntriesWithStatusIdOffsetAndLimit(
            Mockito.anyInt(),
            Mockito.anyInt(),
            Mockito.anyInt());
    Mockito.verify(reconciliationRepository, Mockito.times(1))
        .getReconciliationReferralByLabResId(
            Mockito.anyString(),
            Mockito.anyInt());
    Mockito.verify(feignClient, Mockito.times(0))
        .postReconciliationReferral(Mockito.any(CaomiReconciliationReferralRequest.class));
    Mockito.verify(caomiAcceptReferralRepository, Mockito.times(0))
        .save(Mockito.any(HstCaomiAcceptReferral.class));
  }

  @Test
  @DisplayName("\"Поставлено в раписание\" рефералы найдены."
      + "Данные для создания запроса найдены."
      + "Запрос успешно создан."
      + "При отправке запроса в ЦАМИ было выброшено исключение."
      + "hst_caomiAcceptReferral обновлена.")
  void scheduledReferralsFoundDataFoundRequestCreatedCaomiThrewException_entryUpdated() {
    // (1) prepare mocks:
    final HstCaomiAcceptReferral hstCaomiAcceptReferral = new HstCaomiAcceptReferral();
    hstCaomiAcceptReferral.setLbrLaboratoryResearchId(1);
    Mockito.when(caomiAcceptReferralRepository.findEntriesWithStatusIdOffsetAndLimit(
        Mockito.anyInt(),
        Mockito.anyInt(),
        Mockito.anyInt()
    )).thenReturn(List.of(hstCaomiAcceptReferral));

    final ReconciliationDto dto = ReconciliationDto.builder()
        .idReferral("idReferral")
        .agreedReferral(false)
        .rejectionReason("very dumb")
        .occurrencePeriodStart("2021-08-13 12:00:00")
        .occurrencePeriodEnd("2021-08-13 12:10:00")
        .deviceMisId("deviceMisId")
        .owner("me")
        .deviceName("tractor")
        .build();
    Mockito.when(reconciliationRepository.getReconciliationReferralByLabResId(
        Mockito.anyString(),
        Mockito.anyInt()
    )).thenReturn(dto);

    final RuntimeException thrownException = new RuntimeException("oops");
    Mockito.doThrow(thrownException).when(feignClient).postReconciliationReferral(
        Mockito.any(CaomiReconciliationReferralRequest.class)
    );

    // (2) start:
    service.findReconciliationReferralsScheduledAndSendToCaomi();

    // (3) check:
    Mockito.verify(caomiAcceptReferralRepository, Mockito.times(1))
        .findEntriesWithStatusIdOffsetAndLimit(
            Mockito.anyInt(),
            Mockito.anyInt(),
            Mockito.anyInt());
    Mockito.verify(reconciliationRepository, Mockito.times(1))
        .getReconciliationReferralByLabResId(
            Mockito.anyString(),
            Mockito.anyInt());

    final ArgumentCaptor<CaomiReconciliationReferralRequest> requestCaptor = ArgumentCaptor.forClass(
        CaomiReconciliationReferralRequest.class);
    Mockito.verify(feignClient, Mockito.times(1))
        .postReconciliationReferral(requestCaptor.capture());
    final CaomiReconciliationReferralRequest captReq = requestCaptor.getValue();
    Assertions.assertEquals(dto.getIdReferral(), captReq.getIdReferral());
    Assertions.assertEquals(dto.getAgreedReferral(), captReq.getAgreedReferral());
    Assertions.assertEquals(dto.getRejectionReason(), captReq.getRejectionReason());
    Assertions.assertEquals(LocalDateTime.parse(dto.getOccurrencePeriodStart(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        .atZone(ZoneId.systemDefault()).toOffsetDateTime(),
        captReq.getOcurrencePeriod().getStart());
    Assertions.assertEquals(LocalDateTime.parse(dto.getOccurrencePeriodEnd(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            .atZone(ZoneId.systemDefault()).toOffsetDateTime(),
        captReq.getOcurrencePeriod().getEnd());
    Assertions.assertEquals(dto.getDeviceMisId(), captReq.getDevice().getDeviceMisId());
    Assertions.assertEquals(dto.getOwner(), captReq.getDevice().getOwner());
    Assertions.assertEquals(dto.getDeviceName(), captReq.getDevice().getDeviceName());

    final ArgumentCaptor<HstCaomiAcceptReferral> savedCaptor = ArgumentCaptor.forClass(HstCaomiAcceptReferral.class);
    Mockito.verify(caomiAcceptReferralRepository, Mockito.times(1))
        .save(savedCaptor.capture());
    Assertions.assertEquals("", savedCaptor.getValue().getErrorCode());
    Assertions.assertEquals(thrownException.getMessage(), savedCaptor.getValue().getErrorText());
  }

  @Test
  @DisplayName("\"Поставлено в раписание\" рефералы найдены."
      + "Данные для создания запроса найдены."
      + "Запрос успешно создан."
      + "При запрос отправлен успешно, но ЦАМИ вернуло ответ."
      + "hst_caomiAcceptReferral не обновлена.")
  void scheduledReferralsFoundDataFoundRequestCreatedCaomiReturnedEmptyBody_entryNotUpdated() {
    // (1) prepare mocks:
    final HstCaomiAcceptReferral hstCaomiAcceptReferral = new HstCaomiAcceptReferral();
    hstCaomiAcceptReferral.setLbrLaboratoryResearchId(1);
    Mockito.when(caomiAcceptReferralRepository.findEntriesWithStatusIdOffsetAndLimit(
        Mockito.anyInt(),
        Mockito.anyInt(),
        Mockito.anyInt()
    )).thenReturn(List.of(hstCaomiAcceptReferral));

    final ReconciliationDto dto = ReconciliationDto.builder()
        .idReferral("idReferral")
        .agreedReferral(false)
        .rejectionReason("very dumb")
        .occurrencePeriodStart("2021-08-13 12:00:00")
        .occurrencePeriodEnd("2021-08-13 12:10:00")
        .deviceMisId("deviceMisId")
        .owner("me")
        .deviceName("tractor")
        .build();
    Mockito.when(reconciliationRepository.getReconciliationReferralByLabResId(
        Mockito.anyString(),
        Mockito.anyInt()
    )).thenReturn(dto);


    Mockito.when(feignClient.postReconciliationReferral(Mockito.any(
            CaomiReconciliationReferralRequest.class)))
        .thenReturn(ResponseEntity.of(Optional.empty()));

    // (2) start:
    service.findReconciliationReferralsScheduledAndSendToCaomi();

    // (3) check:
    Mockito.verify(caomiAcceptReferralRepository, Mockito.times(1))
        .findEntriesWithStatusIdOffsetAndLimit(
            Mockito.anyInt(),
            Mockito.anyInt(),
            Mockito.anyInt());
    Mockito.verify(reconciliationRepository, Mockito.times(1))
        .getReconciliationReferralByLabResId(
            Mockito.anyString(),
            Mockito.anyInt());

    final ArgumentCaptor<CaomiReconciliationReferralRequest> requestCaptor = ArgumentCaptor.forClass(
        CaomiReconciliationReferralRequest.class);
    Mockito.verify(feignClient, Mockito.times(1))
        .postReconciliationReferral(requestCaptor.capture());
    final CaomiReconciliationReferralRequest captReq = requestCaptor.getValue();
    Assertions.assertEquals(dto.getIdReferral(), captReq.getIdReferral());
    Assertions.assertEquals(dto.getAgreedReferral(), captReq.getAgreedReferral());
    Assertions.assertEquals(dto.getRejectionReason(), captReq.getRejectionReason());
    Assertions.assertEquals(LocalDateTime.parse(dto.getOccurrencePeriodStart(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            .atZone(ZoneId.systemDefault()).toOffsetDateTime(),
        captReq.getOcurrencePeriod().getStart());
    Assertions.assertEquals(LocalDateTime.parse(dto.getOccurrencePeriodEnd(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            .atZone(ZoneId.systemDefault()).toOffsetDateTime(),
        captReq.getOcurrencePeriod().getEnd());
    Assertions.assertEquals(dto.getDeviceMisId(), captReq.getDevice().getDeviceMisId());
    Assertions.assertEquals(dto.getOwner(), captReq.getDevice().getOwner());
    Assertions.assertEquals(dto.getDeviceName(), captReq.getDevice().getDeviceName());

    Mockito.verify(caomiAcceptReferralRepository, Mockito.times(0))
        .save(Mockito.any(HstCaomiAcceptReferral.class));
  }

  @Test
  @DisplayName("\"Поставлено в раписание\" рефералы найдены."
      + "Данные для создания запроса найдены."
      + "Запрос успешно создан."
      + "При запрос отправлен успешно, ЦАМИ вернуло ответ."
      + "hst_caomiAcceptReferral обновлена.")
  void scheduledReferralsFoundDataFoundRequestCreatedCaomiReturnedResponse_entryUpdated() {
    // (1) prepare mocks:
    final HstCaomiAcceptReferral hstCaomiAcceptReferral = new HstCaomiAcceptReferral();
    hstCaomiAcceptReferral.setLbrLaboratoryResearchId(1);
    Mockito.when(caomiAcceptReferralRepository.findEntriesWithStatusIdOffsetAndLimit(
        Mockito.anyInt(),
        Mockito.anyInt(),
        Mockito.anyInt()
    )).thenReturn(List.of(hstCaomiAcceptReferral));

    final ReconciliationDto dto = ReconciliationDto.builder()
        .idReferral("idReferral")
        .agreedReferral(false)
        .rejectionReason("very dumb")
        .occurrencePeriodStart("2021-08-13 12:00:00")
        .occurrencePeriodEnd("2021-08-13 12:10:00")
        .deviceMisId("deviceMisId")
        .owner("me")
        .deviceName("tractor")
        .build();
    Mockito.when(reconciliationRepository.getReconciliationReferralByLabResId(
        Mockito.anyString(),
        Mockito.anyInt()
    )).thenReturn(dto);


    final CaomiBasicResponse response = new CaomiBasicResponse();
    response.setErrorText("success");
    response.setErrorCode("1337");
    Mockito.when(feignClient.postReconciliationReferral(Mockito.any(
            CaomiReconciliationReferralRequest.class)))
        .thenReturn(ResponseEntity.of(Optional.of(response)));

    // (2) start:
    service.findReconciliationReferralsScheduledAndSendToCaomi();

    // (3) check:
    Mockito.verify(caomiAcceptReferralRepository, Mockito.times(1))
        .findEntriesWithStatusIdOffsetAndLimit(
            Mockito.anyInt(),
            Mockito.anyInt(),
            Mockito.anyInt());
    Mockito.verify(reconciliationRepository, Mockito.times(1))
        .getReconciliationReferralByLabResId(
            Mockito.anyString(),
            Mockito.anyInt());

    final ArgumentCaptor<CaomiReconciliationReferralRequest> requestCaptor = ArgumentCaptor.forClass(
        CaomiReconciliationReferralRequest.class);
    Mockito.verify(feignClient, Mockito.times(1))
        .postReconciliationReferral(requestCaptor.capture());
    final CaomiReconciliationReferralRequest captReq = requestCaptor.getValue();
    Assertions.assertEquals(dto.getIdReferral(), captReq.getIdReferral());
    Assertions.assertEquals(dto.getAgreedReferral(), captReq.getAgreedReferral());
    Assertions.assertEquals(dto.getRejectionReason(), captReq.getRejectionReason());
    Assertions.assertEquals(LocalDateTime.parse(dto.getOccurrencePeriodStart(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            .atZone(ZoneId.systemDefault()).toOffsetDateTime(),
        captReq.getOcurrencePeriod().getStart());
    Assertions.assertEquals(LocalDateTime.parse(dto.getOccurrencePeriodEnd(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            .atZone(ZoneId.systemDefault()).toOffsetDateTime(),
        captReq.getOcurrencePeriod().getEnd());
    Assertions.assertEquals(dto.getDeviceMisId(), captReq.getDevice().getDeviceMisId());
    Assertions.assertEquals(dto.getOwner(), captReq.getDevice().getOwner());
    Assertions.assertEquals(dto.getDeviceName(), captReq.getDevice().getDeviceName());

    final ArgumentCaptor<HstCaomiAcceptReferral> savedCaptor = ArgumentCaptor.forClass(HstCaomiAcceptReferral.class);
    Mockito.verify(caomiAcceptReferralRepository, Mockito.times(1))
        .save(savedCaptor.capture());
    Assertions.assertEquals(response.getErrorCode(), savedCaptor.getValue().getErrorCode());
    Assertions.assertEquals(response.getErrorText(), savedCaptor.getValue().getErrorText());
    Assertions.assertEquals(SCHEDULED_REFERRAL_SENT_STATUS_ID, savedCaptor.getValue().getStatusId());
  }


  @Test
  @DisplayName("\"Нет свободных слотов в расписании\" рефералы найдены."
      + "Данные для создания запроса найдены."
      + "Запрос успешно создан."
      + "При запрос отправлен успешно, ЦАМИ вернуло ответ."
      + "hst_caomiAcceptReferral обновлена.")
  void noSlotsAvailableReferralsFoundDataFoundRequestCreatedCaomiReturnedResponse_entryUpdated() {
    // (1) prepare mocks:
    final HstCaomiAcceptReferral hstCaomiAcceptReferral = new HstCaomiAcceptReferral();
    hstCaomiAcceptReferral.setLbrLaboratoryResearchId(1);
    Mockito.when(caomiAcceptReferralRepository.findEntriesWithStatusIdOffsetAndLimit(
        Mockito.anyInt(),
        Mockito.anyInt(),
        Mockito.anyInt()
    )).thenReturn(List.of(hstCaomiAcceptReferral));

    final ReconciliationDto dto = ReconciliationDto.builder()
        .idReferral("idReferral")
        .agreedReferral(false)
        .rejectionReason("very dumb")
        .occurrencePeriodStart("2021-08-13 12:00:00")
        .occurrencePeriodEnd("2021-08-13 12:10:00")
        .deviceMisId("deviceMisId")
        .owner("me")
        .deviceName("tractor")
        .build();
    Mockito.when(reconciliationRepository.getReconciliationReferralByLabResId(
        Mockito.anyString(),
        Mockito.anyInt()
    )).thenReturn(dto);


    final CaomiBasicResponse response = new CaomiBasicResponse();
    response.setErrorText("success");
    response.setErrorCode("1337");
    Mockito.when(feignClient.postReconciliationReferral(Mockito.any(
            CaomiReconciliationReferralRequest.class)))
        .thenReturn(ResponseEntity.of(Optional.of(response)));

    // (2) start:
    service.findReconciliationReferralsNoSlotsAvailableAndSendToCaomi();

    // (3) check:
    Mockito.verify(caomiAcceptReferralRepository, Mockito.times(1))
        .findEntriesWithStatusIdOffsetAndLimit(
            Mockito.anyInt(),
            Mockito.anyInt(),
            Mockito.anyInt());
    Mockito.verify(reconciliationRepository, Mockito.times(1))
        .getReconciliationReferralByLabResId(
            Mockito.anyString(),
            Mockito.anyInt());

    final ArgumentCaptor<CaomiReconciliationReferralRequest> requestCaptor = ArgumentCaptor.forClass(
        CaomiReconciliationReferralRequest.class);
    Mockito.verify(feignClient, Mockito.times(1))
        .postReconciliationReferral(requestCaptor.capture());
    final CaomiReconciliationReferralRequest captReq = requestCaptor.getValue();
    Assertions.assertEquals(dto.getIdReferral(), captReq.getIdReferral());
    Assertions.assertEquals(dto.getAgreedReferral(), captReq.getAgreedReferral());
    Assertions.assertEquals(dto.getRejectionReason(), captReq.getRejectionReason());
    Assertions.assertNull(captReq.getOcurrencePeriod());
    Assertions.assertNull(captReq.getDevice());

    final ArgumentCaptor<HstCaomiAcceptReferral> savedCaptor = ArgumentCaptor.forClass(HstCaomiAcceptReferral.class);
    Mockito.verify(caomiAcceptReferralRepository, Mockito.times(1))
        .save(savedCaptor.capture());
    Assertions.assertEquals(response.getErrorCode(), savedCaptor.getValue().getErrorCode());
    Assertions.assertEquals(response.getErrorText(), savedCaptor.getValue().getErrorText());
    Assertions.assertEquals(NO_FREE_SLOTS_REFERRAL_SENT_STATUS_ID, savedCaptor.getValue().getStatusId());
  }

}