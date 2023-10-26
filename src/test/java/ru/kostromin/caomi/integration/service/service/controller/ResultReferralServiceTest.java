package ru.kostromin.caomi.integration.service.service.controller;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.kostromin.caomi.integration.service.config.ResultReferralEndpointConfig;
import ru.kostromin.caomi.integration.service.controller.request.common.FullName;
import ru.kostromin.caomi.integration.service.controller.request.refresult.ImagingStudyMis;
import ru.kostromin.caomi.integration.service.controller.request.common.Practitioner;
import ru.kostromin.caomi.integration.service.controller.request.refresult.ResourcePresentedForm;
import ru.kostromin.caomi.integration.service.controller.request.refresult.ResultReferralRequest;
import ru.kostromin.caomi.integration.service.controller.request.refresult.ServiceResult;
import ru.kostromin.caomi.integration.service.controller.response.Response;
import ru.kostromin.caomi.integration.service.controller.response.Response.ErrorCode;
import ru.kostromin.caomi.integration.service.data.entity.HstCaomiReferral;
import ru.kostromin.caomi.integration.service.data.entity.LbrLaboratoryResearch;
import ru.kostromin.caomi.integration.service.data.entity.LbrResearch;
import ru.kostromin.caomi.integration.service.data.entity.LbrResearchType;
import ru.kostromin.caomi.integration.service.data.entity.OmsKlNowService;
import ru.kostromin.caomi.integration.service.data.repository.HltMkabRepository;
import ru.kostromin.caomi.integration.service.data.repository.HstCaomiReferralRepository;
import ru.kostromin.caomi.integration.service.data.repository.LbrLaboratoryResearchRepository;
import ru.kostromin.caomi.integration.service.data.repository.LbrResearchRepository;
import ru.kostromin.caomi.integration.service.data.repository.LbrResearchTypeRepository;
import ru.kostromin.caomi.integration.service.data.repository.OmsKlNowServiceRepository;

@ExtendWith(SpringExtension.class)
class ResultReferralServiceTest {

  @Mock
  private HstCaomiReferralRepository caomiReferralRepository;
  @Mock
  private HltMkabRepository hltMkabRepository;
  @Mock
  private LbrResearchTypeRepository lbrResearchTypeRepository;
  @Mock
  private LbrResearchRepository lbrResearchRepository;
  @Mock
  private LbrLaboratoryResearchRepository lbrLaboratoryResearchRepository;
  @Mock
  private OmsKlNowServiceRepository omsKlNowServiceRepository;

  private ResultReferralEndpointConfig referralEndpointConfig;

  private ResultReferralService service;

  @BeforeEach
  void initService(){
    referralEndpointConfig = new ResultReferralEndpointConfig();
    referralEndpointConfig.setContentType("html");
    referralEndpointConfig.setConclusionRegexPattern("<Data>(.*?)</Data>");
    referralEndpointConfig.setGroup(1);
    service = new ResultReferralService(caomiReferralRepository,
        hltMkabRepository,
        lbrResearchTypeRepository,
        lbrResearchRepository,
        lbrLaboratoryResearchRepository,
        omsKlNowServiceRepository,
        referralEndpointConfig);
  }

  @Test
  @DisplayName("Обработка запроса направления ResultReferralRequest."
      + "Запись направления hst_caomiReferral не найдена."
      + "Ответ об не найденном направлении возвращен.")
  void whenCaomiReferralNotFound_absentReferralResponseReturned(){
    // (1) prepare mocks:
    Mockito.when(caomiReferralRepository.findByCaomiId(Mockito.anyString()))
        .thenReturn(new ArrayList<>());

    // (2) start test:
    ResultReferralRequest passedRequest = new ResultReferralRequest();
    passedRequest.setIdReferral(UUID.randomUUID());

    Response result = service.processResultReferralRequest(passedRequest);

    // (3) check:
    Mockito.verify(caomiReferralRepository, Mockito.times(1))
        .findByCaomiId(Mockito.anyString());

    Assertions.assertEquals(ErrorCode.E006.getCode(), result.getErrorCode());
    Assertions.assertEquals(String.format("Направление с идентификатором {%s} не найдено",
        passedRequest.getIdReferral().toString()), result.getErrorText());
  }

  @Test
  @DisplayName("Обработка запроса направления ResultReferralRequest."
      + "Надено больше 1 записи направления hst_caomiReferral."
      + "Ответ об нескольких найденных направлениях возвращен.")
  void whenFoundTooManyReferrals_tooManyReferralsResponseReturned(){
    // (1) prepare mocks:
    Mockito.when(caomiReferralRepository.findByCaomiId(Mockito.anyString()))
        .thenReturn(List.of(new HstCaomiReferral(), new HstCaomiReferral()));

    // (2) start test:
    ResultReferralRequest passedRequest = new ResultReferralRequest();
    passedRequest.setIdReferral(UUID.randomUUID());

    Response result = service.processResultReferralRequest(passedRequest);

    // (3) check:
    Mockito.verify(caomiReferralRepository, Mockito.times(1))
        .findByCaomiId(Mockito.anyString());

    Assertions.assertEquals(ErrorCode.E006.getCode(), result.getErrorCode());
    Assertions.assertEquals(String.format("Для идентификатора {%s} найдено больше 1 направления",
        passedRequest.getIdReferral().toString()), result.getErrorText());
  }

  @Test
  @DisplayName("Обработка запроса направления ResultReferralRequest."
      + "Запись направления hst_caomiReferral найдена, но lbrLaboratoryResearchID не заполнен"
      + "Ответ об незаполненном lbrLaboratoryResearchID возвращен.")
  void whenReferralFoundButLabResIdIsAbsent_labResIdIsAbsentResponseReturned(){
    // (1) prepare mocks:
    Mockito.when(caomiReferralRepository.findByCaomiId(Mockito.anyString()))
        .thenReturn(List.of(new HstCaomiReferral()));

    // (2) start test:
    ResultReferralRequest passedRequest = new ResultReferralRequest();
    passedRequest.setIdReferral(UUID.randomUUID());

    Response result = service.processResultReferralRequest(passedRequest);

    // (3) check:
    Mockito.verify(caomiReferralRepository, Mockito.times(1))
        .findByCaomiId(Mockito.anyString());

    Assertions.assertEquals(ErrorCode.E006.getCode(), result.getErrorCode());
    Assertions.assertEquals(String.format("Для идентификатора {%s} в таблице hst_caomiReferral не заполнен lbrLaboratoryResearchID",
        passedRequest.getIdReferral().toString()), result.getErrorText());
  }

  @Test
  @DisplayName("Обработка запроса направления ResultReferralRequest."
      + "Запись направления hst_caomiReferral найдена,"
      + "пациент oms_mn_Person не найден"
      + "Ответ об не найденном пациенте возвращен.")
  void whenOmsOnPersonNotFound_absentPatientResponseReturned() {
    // (1) prepare mocks:
    HstCaomiReferral expCaomiReferral = new HstCaomiReferral();
    expCaomiReferral.setLbrLaboratoryResearchId(1);
    Mockito.when(caomiReferralRepository.findByCaomiId(Mockito.anyString()))
        .thenReturn(List.of(expCaomiReferral));

    Mockito.when(hltMkabRepository.existsByUguid(Mockito.anyString())).thenReturn(false);

    // (2) start test:
    ResultReferralRequest passedRequest = new ResultReferralRequest();
    passedRequest.setIdReferral(UUID.randomUUID());
    List<ServiceResult> results = new ArrayList<>();
    ServiceResult expServiceResult = new ServiceResult();
    expServiceResult.setPatientId(UUID.randomUUID().toString());
    results.add(expServiceResult);
    passedRequest.setServiceResult(results);

    Response result = service.processResultReferralRequest(passedRequest);

    // (3) check:
    Mockito.verify(caomiReferralRepository, Mockito.times(1))
        .findByCaomiId(Mockito.anyString());

    Mockito.verify(hltMkabRepository, Mockito.times(1))
            .existsByUguid(Mockito.anyString());

    Assertions.assertEquals(ErrorCode.E006.getCode(), result.getErrorCode());
    Assertions.assertEquals(String.format("У направления с идентификатором {%s} не найден пациент с идентификатором {%s}",
        passedRequest.getIdReferral().toString(), expServiceResult.getPatientId()), result.getErrorText());
  }

  @Test
  @DisplayName("Обработка запроса направления ResultReferralRequest."
      + "Запись направления hst_caomiReferral найдена,"
      + "пациент oms_mn_Person найден,"
      + "услуга lbr_Research не найдена."
      + "Ответ об не найденной услуге возвращен.")
  void whenLbrResearchNotFound_absentServiceResponseReturned() {
    // (1) prepare mocks:
    HstCaomiReferral expCaomiReferral = new HstCaomiReferral();
    expCaomiReferral.setLbrLaboratoryResearchId(1);
    Mockito.when(caomiReferralRepository.findByCaomiId(Mockito.anyString()))
        .thenReturn(List.of(expCaomiReferral));

    Mockito.when(hltMkabRepository.existsByUguid(Mockito.anyString())).thenReturn(true);

    Mockito.when(lbrResearchRepository.existsByResearchId(Mockito.anyInt())).thenReturn(false);

    // (2) start test:
    ResultReferralRequest passedRequest = new ResultReferralRequest();
    passedRequest.setIdReferral(UUID.randomUUID());
    List<ServiceResult> results = new ArrayList<>();
    ServiceResult expServiceResult = new ServiceResult();
    expServiceResult.setPatientId(UUID.randomUUID().toString());
    expServiceResult.setServiceId("1");
    results.add(expServiceResult);
    passedRequest.setServiceResult(results);

    Response result = service.processResultReferralRequest(passedRequest);

    // (3) check:
    Mockito.verify(caomiReferralRepository, Mockito.times(1))
        .findByCaomiId(Mockito.anyString());

    Mockito.verify(hltMkabRepository, Mockito.times(1))
        .existsByUguid(Mockito.anyString());

    Assertions.assertEquals(ErrorCode.E006.getCode(), result.getErrorCode());
    Assertions.assertEquals(String.format("У направления с идентификатором {%s} не найдена услуга с идентификатором {%s}",
        passedRequest.getIdReferral().toString(), expServiceResult.getServiceId()), result.getErrorText());
  }

  @Test
  @DisplayName("Обработка запроса направления ResultReferralRequest."
          + "Запись направления hst_caomiReferral найдена,"
          + "пациент oms_mn_Person найден,"
          + "услуга lbr_Research не найдена, строка ServiceId не может быть приведена к Integer"
          + "Ответ об не найденной услуге возвращен.")
  void whenLbrResearchNotFoundAndServiceIdCouldNotBeCast_absentServiceResponseReturned() {
    // (1) prepare mocks:
    String serviceId = "serviceId";
    HstCaomiReferral expCaomiReferral = new HstCaomiReferral();
    expCaomiReferral.setLbrLaboratoryResearchId(1);
    Mockito.when(caomiReferralRepository.findByCaomiId(Mockito.anyString()))
            .thenReturn(List.of(expCaomiReferral));

    Mockito.when(hltMkabRepository.existsByUguid(Mockito.anyString())).thenReturn(true);

    // (2) start test:
    ResultReferralRequest passedRequest = new ResultReferralRequest();
    passedRequest.setIdReferral(UUID.randomUUID());
    List<ServiceResult> results = new ArrayList<>();
    ServiceResult expServiceResult = new ServiceResult();
    expServiceResult.setPatientId(UUID.randomUUID().toString());
    expServiceResult.setServiceId(serviceId);
    results.add(expServiceResult);
    passedRequest.setServiceResult(results);

    Response result = service.processResultReferralRequest(passedRequest);

    // (3) check:
    Mockito.verify(caomiReferralRepository, Mockito.times(1))
            .findByCaomiId(Mockito.anyString());

    Mockito.verify(hltMkabRepository, Mockito.times(1))
            .existsByUguid(Mockito.anyString());

    Assertions.assertThrows(NumberFormatException.class,
            () -> lbrResearchRepository.existsByResearchId(Integer.valueOf(serviceId)));
    Assertions.assertEquals(ErrorCode.E006.getCode(), result.getErrorCode());
    Assertions.assertEquals(String.format("У направления с идентификатором {%s} не найдена услуга с идентификатором {%s}",
            passedRequest.getIdReferral().toString(), expServiceResult.getServiceId()), result.getErrorText());
  }

  @Test
  @DisplayName("Обработка запроса направления ResultReferralRequest."
      + "Запись направления hst_caomiReferral найдена,"
      + "пациент oms_mn_Person найден,"
      + "услуга lbr_Research найдена,"
      + "lbr_LaboratoryResearch необходимый для обновления lbr_Research не найден."
      + "Ответ об не возможности обновить lbr_Research возвращен.")
  void whenLbrResearchRelatedToLbrResearchNotFound_lbrResearchSavingErrorResponseReturned() {
    // (1) prepare mocks:
    HstCaomiReferral expCaomiReferral = new HstCaomiReferral();
    expCaomiReferral.setLbrLaboratoryResearchId(1);
    Mockito.when(caomiReferralRepository.findByCaomiId(Mockito.anyString()))
        .thenReturn(List.of(expCaomiReferral));

    Mockito.when(hltMkabRepository.existsByUguid(Mockito.anyString())).thenReturn(true);

    Mockito.when(lbrResearchRepository.existsByResearchId(Mockito.anyInt())).thenReturn(true);

    Mockito.when(lbrLaboratoryResearchRepository.findById(Mockito.anyInt()))
        .thenReturn(Optional.empty());

    // (2) start test:
    ResultReferralRequest passedRequest = new ResultReferralRequest();
    passedRequest.setIdReferral(UUID.randomUUID());
    List<ServiceResult> results = new ArrayList<>();
    ServiceResult expServiceResult = new ServiceResult();
    expServiceResult.setPatientId(UUID.randomUUID().toString());
    expServiceResult.setServiceId("1");
    results.add(expServiceResult);
    passedRequest.setServiceResult(results);

    Response result = service.processResultReferralRequest(passedRequest);

    // (3) check:
    Mockito.verify(caomiReferralRepository, Mockito.times(1))
        .findByCaomiId(Mockito.anyString());
    Mockito.verify(hltMkabRepository, Mockito.times(1))
        .existsByUguid(Mockito.anyString());
    Mockito.verify(lbrResearchRepository, Mockito.times(1))
            .existsByResearchId(Mockito.anyInt());
    Mockito.verify(lbrLaboratoryResearchRepository, Mockito.times(1))
            .findById(Mockito.anyInt());

    Assertions.assertEquals(ErrorCode.E006.getCode(), result.getErrorCode());
    Assertions.assertTrue(result.getErrorText().contains("Не удалось найти запись lbr_LaboratoryResearch")
        && result.getErrorText().contains("необходимую для последующего поиска обновляемых записей lbr_Research"));
  }

  @Test
  @DisplayName("Обработка запроса направления ResultReferralRequest."
      + "Запись направления hst_caomiReferral найдена,"
      + "пациент oms_mn_Person найден,"
      + "услуга lbr_Research найден,"
      + "lbr_LaboratoryResearch необходимый для обновления lbr_Research найден,"
      + "запись для обновления lbr_Research - не была найдена."
      + "Ответ об не всех сохраненных lbr_Research возвращен.")
  void whenLbrResearchNotFound_lbrResearchSavingErrorResponseReturned() {
    // (1) prepare mocks:
    HstCaomiReferral expCaomiReferral = new HstCaomiReferral();
    expCaomiReferral.setLbrLaboratoryResearchId(1);
    Mockito.when(caomiReferralRepository.findByCaomiId(Mockito.anyString()))
        .thenReturn(List.of(expCaomiReferral));

    Mockito.when(hltMkabRepository.existsByUguid(Mockito.anyString())).thenReturn(true);

    Mockito.when(lbrResearchRepository.existsByResearchId(Mockito.anyInt())).thenReturn(true);

    Mockito.when(lbrLaboratoryResearchRepository.findById(Mockito.anyInt()))
        .thenReturn(Optional.of(new LbrLaboratoryResearch()));

    Mockito.when(lbrResearchRepository.findByRfLaboratoryResearchGUID(Mockito.any()))
        .thenReturn(new ArrayList<>());

    // (2) start test:
    ResultReferralRequest passedRequest = new ResultReferralRequest();
    passedRequest.setIdReferral(UUID.randomUUID());
    List<ServiceResult> results = new ArrayList<>();
    ServiceResult expServiceResult = new ServiceResult();
    expServiceResult.setPatientId(UUID.randomUUID().toString());
    expServiceResult.setServiceId("1");
    results.add(expServiceResult);
    passedRequest.setServiceResult(results);

    Response result = service.processResultReferralRequest(passedRequest);

    // (3) check:
    Mockito.verify(caomiReferralRepository, Mockito.times(1))
        .findByCaomiId(Mockito.anyString());
    Mockito.verify(hltMkabRepository, Mockito.times(1))
        .existsByUguid(Mockito.anyString());
    Mockito.verify(lbrResearchRepository, Mockito.times(1))
        .existsByResearchId(Mockito.anyInt());

    Mockito.verify(lbrLaboratoryResearchRepository, Mockito.times(1))
        .findById(Mockito.anyInt());
    Mockito.verify(lbrResearchRepository, Mockito.times(1))
            .findByRfLaboratoryResearchGUID(Mockito.any());

    Assertions.assertNull(result.getErrorCode());
    Assertions.assertTrue(result.getErrorText().contains("Не удалось найти запись lbr_Research для обновления"));
  }
  @Test
  @DisplayName("Обработка запроса направления ResultReferralRequest."
      + "Запись направления hst_caomiReferral найдена,"
      + "пациент oms_mn_Person найден,"
      + "услуга lbr_Research найден,"
      + "lbr_LaboratoryResearch необходимый для обновления lbr_Research найден,"
      + "было найдено больше 1 записи для обновления lbr_Research, "
      + "ServiceResult.serviceCode не заполнен."
      + "Ответ об не всех сохраненных lbr_Research возвращен.")
  void whenMoreThanOneUpdateEntityFoundButServiceCodeEmpty_lbrResearchSavingErrorResponseReturned() {
    // (1) prepare mocks:
    // processResultReferralRequest:
    HstCaomiReferral expCaomiReferral = new HstCaomiReferral();
    expCaomiReferral.setLbrLaboratoryResearchId(1);
    Mockito.when(caomiReferralRepository.findByCaomiId(Mockito.anyString()))
        .thenReturn(List.of(expCaomiReferral));

    Mockito.when(hltMkabRepository.existsByUguid(Mockito.anyString())).thenReturn(true);

    Mockito.when(lbrResearchRepository.existsByResearchId(Mockito.anyInt())).thenReturn(true);

    // findLbrResearchAndUpdate:
    Mockito.when(lbrLaboratoryResearchRepository.findById(Mockito.anyInt()))
        .thenReturn(Optional.of(new LbrLaboratoryResearch()));

    Mockito.when(lbrResearchRepository.findByRfLaboratoryResearchGUID(Mockito.any()))
        .thenReturn(List.of(new LbrResearch(), new LbrResearch()));

    // (2) start test:
    ResultReferralRequest passedRequest = new ResultReferralRequest();
    passedRequest.setIdReferral(UUID.randomUUID());
    List<ServiceResult> results = new ArrayList<>();
    ServiceResult expServiceResult = new ServiceResult();
    expServiceResult.setPatientId(UUID.randomUUID().toString());
    expServiceResult.setServiceId("1");
    expServiceResult.setServiceCode(""); // serviceCode не заполнен
    results.add(expServiceResult);
    passedRequest.setServiceResult(results);

    Response result = service.processResultReferralRequest(passedRequest);

    // (3) check:
    Mockito.verify(caomiReferralRepository, Mockito.times(1))
        .findByCaomiId(Mockito.anyString());
    Mockito.verify(hltMkabRepository, Mockito.times(1))
        .existsByUguid(Mockito.anyString());
    Mockito.verify(lbrResearchRepository, Mockito.times(1))
        .existsByResearchId(Mockito.anyInt());

    Mockito.verify(lbrLaboratoryResearchRepository, Mockito.times(1))
        .findById(Mockito.anyInt());
    Mockito.verify(lbrResearchRepository, Mockito.times(1))
        .findByRfLaboratoryResearchGUID(Mockito.any());

    Assertions.assertNull(result.getErrorCode());
    Assertions.assertTrue(result.getErrorText()
        .contains("найти подходящую запись невозможно т.к. ServiceResult.code - null или пустая строка"));
  }

  @Test
  @DisplayName("Обработка запроса направления ResultReferralRequest."
      + "Запись направления hst_caomiReferral найдена,"
      + "пациент oms_mn_Person существует по uguid,"
      + "услуга lbr_Research существует,"
      + "lbr_LaboratoryResearch необходимый для обновления lbr_Research найден,"
      + "было найдено больше 1 записи для обновления lbr_Research, "
      + "ServiceResult.serviceCode заполнен,"
      + "подходящая lbr_Research для обновления не найдена."
      + "Ответ об не всех сохраненных lbr_Research возвращен.")
  void whenUnableToFindLbrResearchForUpdate_lbrResearchSavingErrorResponseReturned() {
    // (1) prepare mocks:
    // processResultReferralRequest:
    HstCaomiReferral expCaomiReferral = new HstCaomiReferral();
    expCaomiReferral.setLbrLaboratoryResearchId(1);
    Mockito.when(caomiReferralRepository.findByCaomiId(Mockito.anyString()))
        .thenReturn(List.of(expCaomiReferral));

    Mockito.when(hltMkabRepository.existsByUguid(Mockito.anyString())).thenReturn(true);

    Mockito.when(lbrResearchRepository.existsByResearchId(Mockito.anyInt())).thenReturn(true);

    // findLbrResearchAndUpdate:
    Mockito.when(lbrLaboratoryResearchRepository.findById(Mockito.anyInt()))
        .thenReturn(Optional.of(new LbrLaboratoryResearch()));

    List<LbrResearch> foundLbrResearches = List.of(new LbrResearch(), new LbrResearch());
    Mockito.when(lbrResearchRepository.findByRfLaboratoryResearchGUID(Mockito.any()))
        .thenReturn(foundLbrResearches);

    // findOneLbrResearchToUpdateWhenMultipleFound:
    Mockito.when(lbrResearchTypeRepository.findByUguid(Mockito.any())).thenReturn(Optional.empty());

    // (2) start test:
    ResultReferralRequest passedRequest = new ResultReferralRequest();
    passedRequest.setIdReferral(UUID.randomUUID());
    List<ServiceResult> results = new ArrayList<>();
    ServiceResult expServiceResult = new ServiceResult();
    expServiceResult.setPatientId(UUID.randomUUID().toString());
    expServiceResult.setServiceId("1");
    expServiceResult.setServiceCode("serviceCode"); // serviceCode заполнен
    results.add(expServiceResult);
    passedRequest.setServiceResult(results);

    Response result = service.processResultReferralRequest(passedRequest);

    // (3) check:
    // processResultReferralRequest:
    Mockito.verify(caomiReferralRepository, Mockito.times(1))
        .findByCaomiId(Mockito.anyString());
    Mockito.verify(hltMkabRepository, Mockito.times(1))
        .existsByUguid(Mockito.anyString());
    Mockito.verify(lbrResearchRepository, Mockito.times(1))
        .existsByResearchId(Mockito.anyInt());
     // findLbrResearchAndUpdate:
    Mockito.verify(lbrLaboratoryResearchRepository, Mockito.times(1))
        .findById(Mockito.anyInt());
    Mockito.verify(lbrResearchRepository, Mockito.times(1))
        .findByRfLaboratoryResearchGUID(Mockito.any());
    // findOneLbrResearchToUpdateWhenMultipleFound:
    Mockito.verify(lbrResearchTypeRepository, Mockito.times(foundLbrResearches.size()))
        .findByUguid(Mockito.any());

    Assertions.assertNull(result.getErrorCode());
    Assertions.assertTrue(result.getErrorText()
        .contains("Было найдено несколько записей lbr_Research, "
            + "но среди них отсутсвует подходящая запись для обновления"));
  }

  @Test
  @DisplayName("Обработка запроса направления ResultReferralRequest."
      + "Запись направления hst_caomiReferral найдена,"
      + "пациент oms_mn_Person существует по uguid,"
      + "услуга lbr_Research существует,"
      + "lbr_LaboratoryResearch необходимый для обновления lbr_Research найден,"
      + "было найдено больше 1 записи для обновления lbr_Research, "
      + "ServiceResult.serviceCode заполнен,"
      + "подходящая lbr_Research для обновления найдена,"
      + "непредвиденное исключение при обновлении lbr_Research."
      + "Ответ об не всех сохраненных lbr_Research возвращен.")
  void whenUnexpectedExceptionOccurred_lbrResearchSavingErrorResponseReturned() {
    // (1) prepare mocks:
    // processResultReferralRequest:
    HstCaomiReferral expCaomiReferral = new HstCaomiReferral();
    expCaomiReferral.setLbrLaboratoryResearchId(1);
    Mockito.when(caomiReferralRepository.findByCaomiId(Mockito.anyString()))
        .thenReturn(List.of(expCaomiReferral));

    Mockito.when(hltMkabRepository.existsByUguid(Mockito.anyString())).thenReturn(true);

    Mockito.when(lbrResearchRepository.existsByResearchId(Mockito.anyInt())).thenReturn(true);

    // findLbrResearchAndUpdate:
    Mockito.when(lbrLaboratoryResearchRepository.findById(Mockito.anyInt()))
        .thenReturn(Optional.of(new LbrLaboratoryResearch()));

    List<LbrResearch> foundLbrResearches = List.of(new LbrResearch(), new LbrResearch());
    Mockito.when(lbrResearchRepository.findByRfLaboratoryResearchGUID(Mockito.any()))
        .thenReturn(foundLbrResearches);

    // findOneLbrResearchToUpdateWhenMultipleFound:
    Mockito.when(lbrResearchTypeRepository.findByUguid(Mockito.any()))
        .thenReturn(Optional.of(new LbrResearchType()));
    Mockito.when(omsKlNowServiceRepository.findById(Mockito.any()))
        .thenReturn(Optional.of(new OmsKlNowService()));
    Mockito.when(omsKlNowServiceRepository.existsByCode(Mockito.any()))
        .thenReturn(true);

    // updateLbrResearch:
    Mockito.doThrow(new RuntimeException("что то пошло не так")).when(lbrResearchRepository).save(Mockito.any());

    // (2) start test:
    ResultReferralRequest passedRequest = new ResultReferralRequest();
    passedRequest.setIdReferral(UUID.randomUUID());
    List<ServiceResult> results = new ArrayList<>();
    ServiceResult expServiceResult = new ServiceResult();
    expServiceResult.setPatientId(UUID.randomUUID().toString());
    expServiceResult.setServiceId("1");
    expServiceResult.setServiceCode("serviceCode"); // serviceCode заполнен
    results.add(expServiceResult);
    passedRequest.setServiceResult(results);

    Response result = service.processResultReferralRequest(passedRequest);

    // (3) check:
    // processResultReferralRequest:
    Mockito.verify(caomiReferralRepository, Mockito.times(1))
        .findByCaomiId(Mockito.anyString());
    Mockito.verify(hltMkabRepository, Mockito.times(1))
        .existsByUguid(Mockito.anyString());
    Mockito.verify(lbrResearchRepository, Mockito.times(1))
        .existsByResearchId(Mockito.anyInt());
    // findLbrResearchAndUpdate:
    Mockito.verify(lbrLaboratoryResearchRepository, Mockito.times(1))
        .findById(Mockito.anyInt());
    Mockito.verify(lbrResearchRepository, Mockito.times(1))
        .findByRfLaboratoryResearchGUID(Mockito.any());
    // findOneLbrResearchToUpdateWhenMultipleFound:
    Mockito.verify(lbrResearchTypeRepository, Mockito.times(1))
        .findByUguid(Mockito.any());
    Mockito.verify(omsKlNowServiceRepository, Mockito.times(1))
        .findById(Mockito.any());
    Mockito.verify(omsKlNowServiceRepository, Mockito.times(1))
        .existsByCode(Mockito.any());

    Assertions.assertNull(result.getErrorCode());
    Assertions.assertTrue(result.getErrorText()
        .contains("Непредвиденное исключение при попытке обновить запись в таблице lbr_Research"));
  }

  @Test
  @DisplayName("Обработка запроса направления ResultReferralRequest."
      + "Запись направления hst_caomiReferral найдена,"
      + "пациент oms_mn_Person существует по uguid,"
      + "услуга lbr_Research существует,"
      + "lbr_LaboratoryResearch необходимый для обновления lbr_Research найден,"
      + "было найдено больше 1 записи для обновления lbr_Research, "
      + "ServiceResult.serviceCode заполнен,"
      + "подходящая lbr_Research для обновления найдена,"
      + "lbr_Research успешно обновлена."
      + "Успешный ответ lbr_Research возвращен.")
  void whenLbrResearchUpdated_successResponseReturned() {
    // (1) prepare mocks:
    // processResultReferralRequest:
    HstCaomiReferral expCaomiReferral = new HstCaomiReferral();
    expCaomiReferral.setLbrLaboratoryResearchId(1);
    Mockito.when(caomiReferralRepository.findByCaomiId(Mockito.anyString()))
        .thenReturn(List.of(expCaomiReferral));

    Mockito.when(hltMkabRepository.existsByUguid(Mockito.anyString())).thenReturn(true);

    Mockito.when(lbrResearchRepository.existsByResearchId(Mockito.anyInt())).thenReturn(true);

    // findLbrResearchAndUpdate:
    Mockito.when(lbrLaboratoryResearchRepository.findById(Mockito.anyInt()))
        .thenReturn(Optional.of(new LbrLaboratoryResearch()));

    List<LbrResearch> foundLbrResearches = List.of(new LbrResearch(), new LbrResearch());
    Mockito.when(lbrResearchRepository.findByRfLaboratoryResearchGUID(Mockito.any()))
        .thenReturn(foundLbrResearches);

    // findOneLbrResearchToUpdateWhenMultipleFound:
    Mockito.when(lbrResearchTypeRepository.findByUguid(Mockito.any()))
        .thenReturn(Optional.of(new LbrResearchType()));
    Mockito.when(omsKlNowServiceRepository.findById(Mockito.any()))
        .thenReturn(Optional.of(new OmsKlNowService()));
    Mockito.when(omsKlNowServiceRepository.existsByCode(Mockito.any()))
        .thenReturn(true);

    // (2) start test:
    ResultReferralRequest passedRequest = new ResultReferralRequest();
    passedRequest.setIdReferral(UUID.randomUUID());
    List<ServiceResult> results = new ArrayList<>();
    ServiceResult expServiceResult =
        ServiceResult.builder()
            .patientId(UUID.randomUUID().toString())
            .serviceId("1")
            .serviceCode("serviceCode")
            .practitioner(
                Practitioner.builder()
                    .fullName(
                        FullName.builder()
                            .lastName("lastName")
                            .firstName("firstName")
                            .middleName("middleName")
                            .build())
                    .build())
            .effectiveDateTime(OffsetDateTime.now())
            .presentedForm(
                List.of(
                    ResourcePresentedForm.builder()
                        .contentType("text/html")
                        .data(
                            "<?xml version=\\\\\\\"1.0\\\\\\\" encoding=\\\\\\\"UTF-8\\\\\\\" standalone=\\\\\\\"yes\\\\\\\"?>"
                                + "<SignData><Data>"
                                + "0JTQvtCx0YDRi9C5INC00LXQvdGM"
                                + "</Data></SignData>")
                        .build()))
            .imagingStudy(List.of(ImagingStudyMis.builder().studyUid("studyUid").build()))
            .issued(OffsetDateTime.now())
            .build();
    results.add(expServiceResult);
    passedRequest.setServiceResult(results);

    Response result = service.processResultReferralRequest(passedRequest);

    // (3) check:
    // processResultReferralRequest:
    Mockito.verify(caomiReferralRepository, Mockito.times(1))
        .findByCaomiId(Mockito.anyString());
    Mockito.verify(hltMkabRepository, Mockito.times(1))
        .existsByUguid(Mockito.anyString());
    Mockito.verify(lbrResearchRepository, Mockito.times(1))
        .existsByResearchId(Mockito.anyInt());
    // findLbrResearchAndUpdate:
    Mockito.verify(lbrLaboratoryResearchRepository, Mockito.times(1))
        .findById(Mockito.anyInt());
    Mockito.verify(lbrResearchRepository, Mockito.times(1))
        .findByRfLaboratoryResearchGUID(Mockito.any());
    // findOneLbrResearchToUpdateWhenMultipleFound:
    Mockito.verify(lbrResearchTypeRepository, Mockito.times(1))
        .findByUguid(Mockito.any());
    Mockito.verify(omsKlNowServiceRepository, Mockito.times(1))
        .findById(Mockito.any());
    Mockito.verify(omsKlNowServiceRepository, Mockito.times(1))
        .existsByCode(Mockito.any());
    // updateLbrResearch
    ArgumentCaptor<LbrResearch> lbrResearchCaptor = ArgumentCaptor.forClass(LbrResearch.class);
    Mockito.verify(lbrResearchRepository, Mockito.times(1))
            .save(lbrResearchCaptor.capture());
    LbrResearch savedLbrRes = lbrResearchCaptor.getValue();
    Assertions.assertEquals(expServiceResult.getEffectiveDateTime().toLocalDateTime(),
        savedLbrRes.getDateComplete());
    Assertions.assertTrue(savedLbrRes.getIsComplete());
    Assertions.assertEquals(64, savedLbrRes.getFlag());
    Assertions.assertEquals(expServiceResult.getEffectiveDateTime().toLocalDateTime(),
        savedLbrRes.getDatePerformed());
    Assertions.assertEquals("Добрый день", // 0JTQvtCx0YDRi9C5INC00LXQvdGM
        savedLbrRes.getConclusion());
    Assertions.assertFalse(savedLbrRes.getIsCancelled());
    Assertions.assertEquals(expServiceResult.getImagingStudy().get(0).getStudyUid(),
        savedLbrRes.getStudyUid());
    Assertions.assertTrue(savedLbrRes.getIsIssued());
    Assertions.assertEquals(expServiceResult.getIssued().toLocalDateTime(),
        savedLbrRes.getDateIssued());
    Assertions.assertFalse(savedLbrRes.getIsRegistered());
    Assertions.assertFalse(savedLbrRes.getIsReceipt());
    Assertions.assertFalse(savedLbrRes.getIsIemkData());
    Assertions.assertFalse(savedLbrRes.getIsMainExpert());
    Assertions.assertFalse(savedLbrRes.getIsRejected());
    Assertions.assertFalse(savedLbrRes.getIsCompleteEarly());
    Assertions.assertEquals("", savedLbrRes.getPerformedLpuName());

    Assertions.assertEquals(ErrorCode.E000.getCode(), result.getErrorCode());
    Assertions.assertEquals(ErrorCode.E000.getMessage(),result.getErrorText());
    Assertions.assertNotNull(result.getDateTime());
    Assertions.assertEquals(passedRequest.getIdReferral(), result.getIdReferral());
  }

  @Test
  @DisplayName("Обработка запроса направления ResultReferralRequest."
      + "Запись направления hst_caomiReferral найдена,"
      + "пациент oms_mn_Person существует по uguid,"
      + "услуга lbr_Research существует,"
      + "lbr_LaboratoryResearch необходимый для обновления lbr_Research найден,"
      + "было найдено ровно 1 запись для обновления lbr_Research, "
      + "lbr_Research успешно обновлена."
      + "Успешный ответ lbr_Research возвращен.")
  void whenExactlyOneLbrResearchFoundAndUpdated_successResponseReturned() {
    // (1) prepare mocks:
    // processResultReferralRequest:
    HstCaomiReferral expCaomiReferral = new HstCaomiReferral();
    expCaomiReferral.setLbrLaboratoryResearchId(1);
    Mockito.when(caomiReferralRepository.findByCaomiId(Mockito.anyString()))
        .thenReturn(List.of(expCaomiReferral));

    Mockito.when(hltMkabRepository.existsByUguid(Mockito.anyString())).thenReturn(true);

    Mockito.when(lbrResearchRepository.existsByResearchId(Mockito.anyInt())).thenReturn(true);

    // findLbrResearchAndUpdate:
    Mockito.when(lbrLaboratoryResearchRepository.findById(Mockito.anyInt()))
        .thenReturn(Optional.of(new LbrLaboratoryResearch()));

    List<LbrResearch> foundLbrResearches = List.of(new LbrResearch()); // 1 Запись LbrResearch
    Mockito.when(lbrResearchRepository.findByRfLaboratoryResearchGUID(Mockito.any()))
        .thenReturn(foundLbrResearches);

    // (2) start test:
    ResultReferralRequest passedRequest = new ResultReferralRequest();
    passedRequest.setIdReferral(UUID.randomUUID());
    List<ServiceResult> results = new ArrayList<>();
    ServiceResult expServiceResult =
        ServiceResult.builder()
            .patientId(UUID.randomUUID().toString())
            .serviceId("1")
            .serviceCode("serviceCode")
            .practitioner(
                Practitioner.builder()
                    .fullName(
                        FullName.builder()
                            .lastName("lastName")
                            .firstName("firstName")
                            .middleName("middleName")
                            .build())
                    .build())
            .effectiveDateTime(OffsetDateTime.now())
            .presentedForm(
                List.of(
                    ResourcePresentedForm.builder()
                        .contentType("text/html")
                        .data(
                            "<?xml version=\\\\\\\"1.0\\\\\\\" encoding=\\\\\\\"UTF-8\\\\\\\" standalone=\\\\\\\"yes\\\\\\\"?>"
                                + "<SignData><Data>"
                                + "0JTQvtCx0YDRi9C5INC00LXQvdGM"
                                + "</Data></SignData>")
                        .build()))
            .imagingStudy(List.of(ImagingStudyMis.builder().studyUid("studyUid").build()))
            .issued(OffsetDateTime.now())
            .build();
    results.add(expServiceResult);
    passedRequest.setServiceResult(results);

    Response result = service.processResultReferralRequest(passedRequest);

    // (3) check:
    // processResultReferralRequest:
    Mockito.verify(caomiReferralRepository, Mockito.times(1))
        .findByCaomiId(Mockito.anyString());
    Mockito.verify(hltMkabRepository, Mockito.times(1))
        .existsByUguid(Mockito.anyString());
    Mockito.verify(lbrResearchRepository, Mockito.times(1))
        .existsByResearchId(Mockito.anyInt());
    // findLbrResearchAndUpdate:
    Mockito.verify(lbrLaboratoryResearchRepository, Mockito.times(1))
        .findById(Mockito.anyInt());
    Mockito.verify(lbrResearchRepository, Mockito.times(1))
        .findByRfLaboratoryResearchGUID(Mockito.any());
    // findOneLbrResearchToUpdateWhenMultipleFound:
    Mockito.verify(lbrResearchTypeRepository, Mockito.times(0))
        .findByUguid(Mockito.any());
    Mockito.verify(omsKlNowServiceRepository, Mockito.times(0))
        .findById(Mockito.any());
    Mockito.verify(omsKlNowServiceRepository, Mockito.times(0))
        .existsByCode(Mockito.any());
    // updateLbrResearch
    ArgumentCaptor<LbrResearch> lbrResearchCaptor = ArgumentCaptor.forClass(LbrResearch.class);
    Mockito.verify(lbrResearchRepository, Mockito.times(1))
        .save(lbrResearchCaptor.capture());
    LbrResearch savedLbrRes = lbrResearchCaptor.getValue();
    Assertions.assertEquals(expServiceResult.getEffectiveDateTime().toLocalDateTime(),
        savedLbrRes.getDateComplete());
    Assertions.assertTrue(savedLbrRes.getIsComplete());
    Assertions.assertEquals(64, savedLbrRes.getFlag());
    Assertions.assertEquals(expServiceResult.getEffectiveDateTime().toLocalDateTime(),
        savedLbrRes.getDatePerformed());
    Assertions.assertEquals("Добрый день", // 0JTQvtCx0YDRi9C5INC00LXQvdGM
        savedLbrRes.getConclusion());
    Assertions.assertFalse(savedLbrRes.getIsCancelled());
    Assertions.assertEquals(expServiceResult.getImagingStudy().get(0).getStudyUid(),
        savedLbrRes.getStudyUid());
    Assertions.assertTrue(savedLbrRes.getIsIssued());
    Assertions.assertEquals(expServiceResult.getIssued().toLocalDateTime(),
        savedLbrRes.getDateIssued());
    Assertions.assertFalse(savedLbrRes.getIsRegistered());
    Assertions.assertFalse(savedLbrRes.getIsReceipt());
    Assertions.assertFalse(savedLbrRes.getIsIemkData());
    Assertions.assertFalse(savedLbrRes.getIsMainExpert());
    Assertions.assertFalse(savedLbrRes.getIsRejected());
    Assertions.assertFalse(savedLbrRes.getIsCompleteEarly());
    Assertions.assertEquals("", savedLbrRes.getPerformedLpuName());

    Assertions.assertEquals(ErrorCode.E000.getCode(), result.getErrorCode());
    Assertions.assertEquals(ErrorCode.E000.getMessage(),result.getErrorText());
    Assertions.assertNotNull(result.getDateTime());
    Assertions.assertEquals(passedRequest.getIdReferral(), result.getIdReferral());
  }
}