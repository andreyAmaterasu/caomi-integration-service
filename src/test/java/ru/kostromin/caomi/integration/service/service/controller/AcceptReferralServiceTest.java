package ru.kostromin.caomi.integration.service.service.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
import ru.kostromin.caomi.integration.service.controller.request.common.FullName;
import ru.kostromin.caomi.integration.service.controller.request.common.Practitioner;
import ru.kostromin.caomi.integration.service.controller.request.refaccept.AcceptReferralRequest;
import ru.kostromin.caomi.integration.service.controller.request.refaccept.Coverage;
import ru.kostromin.caomi.integration.service.controller.request.refaccept.CoverageValidityPeriod;
import ru.kostromin.caomi.integration.service.controller.request.refaccept.Patient;
import ru.kostromin.caomi.integration.service.controller.request.refaccept.ServiceRequest;
import ru.kostromin.caomi.integration.service.controller.response.Response;
import ru.kostromin.caomi.integration.service.controller.response.Response.ErrorCode;
import ru.kostromin.caomi.integration.service.data.entity.HltMkab;
import ru.kostromin.caomi.integration.service.data.entity.HltPolisMkab;
import ru.kostromin.caomi.integration.service.data.entity.HstCaomiAcceptReferral;
import ru.kostromin.caomi.integration.service.data.entity.HstCaomiReferral;
import ru.kostromin.caomi.integration.service.data.entity.LbrLabResearchTarget;
import ru.kostromin.caomi.integration.service.data.entity.LbrLaboratoryResearch;
import ru.kostromin.caomi.integration.service.data.entity.LbrResearch;
import ru.kostromin.caomi.integration.service.data.entity.OmsKlProfitType;
import ru.kostromin.caomi.integration.service.data.entity.OmsKlSex;
import ru.kostromin.caomi.integration.service.data.entity.OmsKlTipOms;
import ru.kostromin.caomi.integration.service.data.entity.OmsLpu;
import ru.kostromin.caomi.integration.service.data.entity.OmsMkb;
import ru.kostromin.caomi.integration.service.data.entity.OmsSmo;
import ru.kostromin.caomi.integration.service.data.repository.HltMkabRepository;
import ru.kostromin.caomi.integration.service.data.repository.HltPolisMkabRepository;
import ru.kostromin.caomi.integration.service.data.repository.HstCaomiAcceptReferralRepository;
import ru.kostromin.caomi.integration.service.data.repository.HstCaomiReferralRepository;
import ru.kostromin.caomi.integration.service.data.repository.LbrLabResearchTargetRepository;
import ru.kostromin.caomi.integration.service.data.repository.LbrLaboratoryResearchRepository;
import ru.kostromin.caomi.integration.service.data.repository.LbrResearchRepository;
import ru.kostromin.caomi.integration.service.data.repository.LbrResearchTypeRepository;
import ru.kostromin.caomi.integration.service.data.repository.OmsKlProfitTypeRepository;
import ru.kostromin.caomi.integration.service.data.repository.OmsKlSexRepository;
import ru.kostromin.caomi.integration.service.data.repository.OmsKlTipOmsRepository;
import ru.kostromin.caomi.integration.service.data.repository.OmsLpuRepository;
import ru.kostromin.caomi.integration.service.data.repository.OmsMkbRepository;
import ru.kostromin.caomi.integration.service.data.repository.OmsSmoRepository;

@ExtendWith(SpringExtension.class)
class AcceptReferralServiceTest {

  @Mock
  private HstCaomiReferralRepository caomiReferralRepository;
  @Mock
  private HstCaomiAcceptReferralRepository caomiAcceptReferralRepository;
  @Mock
  private LbrLaboratoryResearchRepository lbrLaboratoryResearchRepository;
  @Mock
  private HltMkabRepository mkabRepository;
  @Mock
  private OmsSmoRepository omsSmoRepository;
  @Mock
  private OmsLpuRepository omsLpuRepository;
  @Mock
  private OmsKlSexRepository omsKlSexRepository;
  @Mock
  private OmsKlTipOmsRepository omsKlTipOmsRepository;
  @Mock
  private OmsKlProfitTypeRepository omsKlProfitTypeRepository;
  @Mock
  private HltPolisMkabRepository polisMkabRepository;
  @Mock
  private LbrLabResearchTargetRepository labResearchTargetRepository;
  @Mock
  private OmsMkbRepository omsMkbRepository;
  @Mock
  private LbrResearchRepository lbrResearchRepository;
  @Mock
  private LbrResearchTypeRepository lbrResearchTypeRepository;

  private AcceptReferralService service;

  @BeforeEach
  void initAcceptReferralService(){
    service = new AcceptReferralService(
        caomiReferralRepository, caomiAcceptReferralRepository, lbrLaboratoryResearchRepository,
        mkabRepository, omsSmoRepository, omsLpuRepository,
        omsKlSexRepository, omsKlTipOmsRepository, omsKlProfitTypeRepository,
        polisMkabRepository, labResearchTargetRepository, omsMkbRepository,
        lbrResearchRepository, lbrResearchTypeRepository
    );
  }

  @Test
  @DisplayName("Обработать запрос AcceptReferralRequest."
      + "Найдено больше одного направления."
      + "Response с ошибкой возвращен.")
  void processAcceptReferralRequestMoreThanOneReferralFound_errorResponseReturned(){
    // (1) prepare mocks:
    List<HstCaomiReferral> foundReferrals = new ArrayList<>();
    HstCaomiReferral foundReferral = new HstCaomiReferral();
    foundReferrals.add(foundReferral);
    foundReferrals.add(new HstCaomiReferral());
    Mockito.when(caomiReferralRepository.findByCaomiId(Mockito.anyString()))
        .thenReturn(foundReferrals);

    // (2) start test:
    AcceptReferralRequest request = getFullyFilledRequest();
    Response actualResponse = service.processAcceptReferralRequest(request);

    // (3) check:
    Mockito.verify(caomiReferralRepository, Mockito.times(1))
        .findByCaomiId(Mockito.anyString());
    Mockito.verify(caomiAcceptReferralRepository, Mockito.times(0))
        .save(Mockito.any(HstCaomiAcceptReferral.class));

    Assertions.assertEquals(ErrorCode.E006.getCode(), actualResponse.getErrorCode());
    Assertions.assertEquals(String.format("Для идентификатора {%s} найдено больше 1 направления",
        request.getIdReferral().toString()), actualResponse.getErrorText());
  }

  @Test
  @DisplayName("Обработать запрос AcceptReferralRequest."
      + "Найдено направление,"
      + "не найдена lbr_LaboratoryResearch."
      + "Response с ошибкой возвращен.")
  void processAcceptReferralRequestReferralFoundLbrLaboratoryNotFound_errorResponseReturned(){
    // (1) prepare mocks:
    List<HstCaomiReferral> foundReferrals = new ArrayList<>();
    HstCaomiReferral foundReferral = new HstCaomiReferral();
    foundReferral.setLbrLaboratoryResearchId(1);
    foundReferrals.add(foundReferral);
    Mockito.when(caomiReferralRepository.findByCaomiId(Mockito.anyString()))
        .thenReturn(foundReferrals);

    Mockito.when(lbrLaboratoryResearchRepository.findById(Mockito.anyInt()))
        .thenReturn(Optional.empty());

    // (2) start test:
    AcceptReferralRequest request = getFullyFilledRequest();
    Response actualResponse = service.processAcceptReferralRequest(request);

    // (3) check:
    Mockito.verify(caomiReferralRepository, Mockito.times(1))
        .findByCaomiId(Mockito.anyString());
    Mockito.verify(lbrLaboratoryResearchRepository, Mockito.times(1))
            .findById(Mockito.anyInt());
    Mockito.verify(caomiAcceptReferralRepository, Mockito.times(0))
        .save(Mockito.any(HstCaomiAcceptReferral.class));

    Assertions.assertEquals(ErrorCode.E006.getCode(), actualResponse.getErrorCode());
    Assertions.assertEquals(String.format("Для направления с идентификатором {%s} не найдено lbr_LaboratoryResearch с id = {%s}",
        request.getIdReferral().toString(), foundReferral.getLbrLaboratoryResearchId()),
        actualResponse.getErrorText());
  }

  @Test
  @DisplayName("Обработать запрос AcceptReferralRequest."
      + "Найдено направление,"
      + "найдена lbr_LaboratoryResearch,"
      + "при сохранении данных в hst_caomiAcceptReferral было выброшено исключение."
      + "Response с ошибкой возвращен.")
  void processAcceptReferralRequestReferralFoundLbrLaboratoryFoundSavingAcceptReferralThrewException_errorResponseReturned(){
    // (1) prepare mocks:
    List<HstCaomiReferral> foundReferrals = new ArrayList<>();
    HstCaomiReferral foundReferral = new HstCaomiReferral();
    foundReferral.setLbrLaboratoryResearchId(1);
    foundReferrals.add(foundReferral);
    Mockito.when(caomiReferralRepository.findByCaomiId(Mockito.anyString()))
        .thenReturn(foundReferrals);

    LbrLaboratoryResearch foundLab = new LbrLaboratoryResearch();
    foundLab.setRfMkabId(2);
    Mockito.when(lbrLaboratoryResearchRepository.findById(Mockito.anyInt()))
        .thenReturn(Optional.of(foundLab));

    Mockito.doThrow(new RuntimeException())
        .when(caomiAcceptReferralRepository)
        .save(Mockito.any(HstCaomiAcceptReferral.class));

    // (2) start test:
    AcceptReferralRequest request = getFullyFilledRequest();
    Response actualResponse = service.processAcceptReferralRequest(request);

    // (3) check:
    Mockito.verify(caomiReferralRepository, Mockito.times(1))
        .findByCaomiId(Mockito.anyString());
    Mockito.verify(lbrLaboratoryResearchRepository, Mockito.times(1))
        .findById(Mockito.anyInt());
    final ArgumentCaptor<LbrLaboratoryResearch> saveCaptor = ArgumentCaptor.forClass(LbrLaboratoryResearch.class);
    Mockito.verify(lbrLaboratoryResearchRepository, Mockito.times(1))
        .save(saveCaptor.capture());
    Assertions.assertEquals(request.getIdReferral().toString(), saveCaptor.getValue().getAccessionNumber());

    ArgumentCaptor<HstCaomiAcceptReferral> acceptReferralCaptor = ArgumentCaptor.forClass(HstCaomiAcceptReferral.class);
    Mockito.verify(caomiAcceptReferralRepository, Mockito.times(1))
        .save(acceptReferralCaptor.capture());
    HstCaomiAcceptReferral actualAcceptReferral = acceptReferralCaptor.getValue();
    Assertions.assertEquals(request.getIdReferral().toString(), actualAcceptReferral.getCaomiId());
    Assertions.assertNotNull(actualAcceptReferral.getDateCreate());
    Assertions.assertEquals(request.getMoOid(), actualAcceptReferral.getRecipientMoOid());
    Assertions.assertEquals(request.getServiceRequest().get(0).getPerformerMoOid(),
        actualAcceptReferral.getPerformerMoOid());
    Assertions.assertEquals(request.getServiceRequest().get(0).getPerformerDeviceId(),
        actualAcceptReferral.getPerformerDeviceId());
    Assertions.assertEquals(0, actualAcceptReferral.getStatusId());
    Assertions.assertEquals(foundReferral.getLbrLaboratoryResearchId(), actualAcceptReferral.getLbrLaboratoryResearchId());
    Assertions.assertEquals(foundLab.getRfMkabId(), actualAcceptReferral.getMkabId());

    Assertions.assertEquals("Не удалось создать новую запись в таблице hst_caomiAcceptReferral для "
        + "ServiceRequest [idReferral, moOid, performerMoOid, performerDeviceId]: " + String.format("[%s, %s, %s, %s],",
        request.getIdReferral(),
        request.getMoOid(),
        request.getServiceRequest().get(0).getPerformerMoOid(),
        request.getServiceRequest().get(0).getPerformerDeviceId()),
        actualResponse.getErrorText());
  }

  @Test
  @DisplayName("Обработать запрос AcceptReferralRequest."
      + "Найдено направление,"
      + "найдена lbr_LaboratoryResearch,"
      + "данные сохранены в hst_caomiAcceptReferral."
      + "Успешный Response возвращен.")
  void processAcceptReferralRequestReferralFoundLbrLaboratoryFoundAcceptReferralSaved_successResponseReturned(){
    // (1) prepare mocks:
    List<HstCaomiReferral> foundReferrals = new ArrayList<>();
    HstCaomiReferral foundReferral = new HstCaomiReferral();
    foundReferral.setLbrLaboratoryResearchId(1);
    foundReferrals.add(foundReferral);
    Mockito.when(caomiReferralRepository.findByCaomiId(Mockito.anyString()))
        .thenReturn(foundReferrals);

    LbrLaboratoryResearch foundLab = new LbrLaboratoryResearch();
    foundLab.setRfMkabId(2);
    Mockito.when(lbrLaboratoryResearchRepository.findById(Mockito.anyInt()))
        .thenReturn(Optional.of(foundLab));

    // (2) start test:
    AcceptReferralRequest request = getFullyFilledRequest();
    Response actualResponse = service.processAcceptReferralRequest(request);

    // (3) check:
    Mockito.verify(caomiReferralRepository, Mockito.times(1))
        .findByCaomiId(Mockito.anyString());
    Mockito.verify(lbrLaboratoryResearchRepository, Mockito.times(1))
        .findById(Mockito.anyInt());
    final ArgumentCaptor<LbrLaboratoryResearch> saveCaptor = ArgumentCaptor.forClass(LbrLaboratoryResearch.class);
    Mockito.verify(lbrLaboratoryResearchRepository, Mockito.times(1))
        .save(saveCaptor.capture());
    Assertions.assertEquals(request.getIdReferral().toString(), saveCaptor.getValue().getAccessionNumber());

    assertHstCaomiAcceptReferral(request, foundReferral.getLbrLaboratoryResearchId(),
        foundLab.getRfMkabId());

    Assertions.assertEquals(ErrorCode.E000.getCode(), actualResponse.getErrorCode());
    Assertions.assertEquals(ErrorCode.E000.getMessage(), actualResponse.getErrorText());
    Assertions.assertNotNull(actualResponse.getDateTime());
    Assertions.assertEquals(request.getIdReferral(), actualResponse.getIdReferral());
  }

  @Test
  @DisplayName("Обработать запрос AcceptReferralRequest."
      + "Найдено 0 направлений, производится поиск МКАБ,"
      + "найден МКАБ по СНИЛС,"
      + "произошла ошибка при сохранении новой lbr_LaboratoryResearch."
      + "Response с ошибкой возвращен.")
  void processAcceptReferralRequestReferralNotFoundMkabBySnilsFoundSavingLabResearchThrewException_errorResponseReturned(){
    // (1) prepare mocks:
    Mockito.when(caomiReferralRepository.findByCaomiId(Mockito.anyString()))
        .thenReturn(new ArrayList<>());

    HltMkab foundMkab = new HltMkab();
    foundMkab.setMkabId(1);
    Mockito.when(mkabRepository.findByPolicyNumber(Mockito.anyString()))
        .thenReturn(Optional.of(foundMkab));

    Mockito.doThrow(new RuntimeException())
        .when(lbrLaboratoryResearchRepository)
        .save(Mockito.any(LbrLaboratoryResearch.class));

    // (2) start test:
    AcceptReferralRequest request = getFullyFilledRequest();
    Response actualResponse = service.processAcceptReferralRequest(request);

    // (3) check:
    Mockito.verify(caomiReferralRepository, Mockito.times(1))
        .findByCaomiId(Mockito.anyString());
    Mockito.verify(lbrLaboratoryResearchRepository, Mockito.times(1))
            .save(Mockito.any(LbrLaboratoryResearch.class));

    Mockito.verify(caomiAcceptReferralRepository, Mockito.times(0))
        .save(Mockito.any(HstCaomiAcceptReferral.class));

    Assertions.assertEquals("Непредвиденное исключение при попытке сохранить новую запись lbr_LaboratoryResearch"
        ,actualResponse.getErrorText());
  }

  @Test
  @DisplayName("Обработать запрос AcceptReferralRequest."
      + "Найдено 0 направлений, производится поиск МКАБ,"
      + "найден МКАБ по Полису,"
      + "новая запись lbr_LaboratoryResearch сохранена,"
      + "произошла ошибка при сохранении новой lbr_Laboratory,"
      + "данные НЕ сохранены в hst_caomiAcceptReferral."
      + "Response с ошибкой возвращен.")
  void processAcceptReferralRequestReferralNotFoundMkabByPolicyFoundLabResearchSavedLbrLabException_successResponseReturned(){
    // (1) prepare mocks:
    Mockito.when(caomiReferralRepository.findByCaomiId(Mockito.anyString()))
        .thenReturn(new ArrayList<>());

    HltMkab foundMkab = new HltMkab();
    foundMkab.setMkabId(1);
    Mockito.when(mkabRepository.findByPolicyNumber(Mockito.anyString()))
        .thenReturn(Optional.of(foundMkab));

    // *для части заполнения lbr_LaboratoryResearch из БД*
    Integer expRfSmoId = 100;
    Mockito.when(omsSmoRepository.findByCod(Mockito.anyString()))
        .thenReturn(Optional.of(OmsSmo.builder().smoId(expRfSmoId).build()));
    Integer expKlTipOmsId = 200;
    Mockito.when(omsKlTipOmsRepository.findByIdDoc(Mockito.anyInt()))
        .thenReturn(Optional.of(OmsKlTipOms.builder().klTipOmsId(expKlTipOmsId).build()));
    Integer expLpuIdFromMoOid = 300;
    Integer expLpuIdFromPerfmMoOid = 400;
    Mockito.when(omsLpuRepository.findTopByLic(Mockito.anyString()))
        .thenReturn(Optional.of(OmsLpu.builder().lpuId(expLpuIdFromMoOid).build()))
        .thenReturn(Optional.of(OmsLpu.builder().lpuId(expLpuIdFromPerfmMoOid).build()));
    Integer expLabResearchTargetId = 500;
    Mockito.when(labResearchTargetRepository.findByCode(Mockito.anyString()))
        .thenReturn(Optional.of(LbrLabResearchTarget.builder().labResearchTargetId(expLabResearchTargetId).build()));
    Integer expMkbId = 600;
    Mockito.when(omsMkbRepository.findByDs(Mockito.anyString()))
        .thenReturn(Optional.of(OmsMkb.builder().mkbId(expMkbId).build()));
    Integer expKlProfitTypeId = 700;
    Mockito.when(omsKlProfitTypeRepository.findByCode(Mockito.anyString()))
        .thenReturn(Optional.of(OmsKlProfitType.builder().klProfitTypeId(expKlProfitTypeId).build()));

    LbrLaboratoryResearch savedLabRes = new LbrLaboratoryResearch();
    savedLabRes.setLaboratoryResearchId(2000);
    Mockito.when(lbrLaboratoryResearchRepository.save(Mockito.any(LbrLaboratoryResearch.class)))
        .thenReturn(savedLabRes);

    Mockito.doThrow(new RuntimeException())
        .when(lbrResearchRepository).findByRfLaboratoryResearchGUID(Mockito.anyString());


    // (2) start test:
    AcceptReferralRequest request = getFullyFilledRequest();
    Response actualResponse = service.processAcceptReferralRequest(request);

    // (3) check:
    Mockito.verify(caomiReferralRepository, Mockito.times(1))
        .findByCaomiId(Mockito.anyString());
    // сохранение LbrLaboratoryResearch
    assertLbrLaboratoryResearch(request,
        foundMkab.getMkabId(),
        expRfSmoId,
        expKlTipOmsId,
        expLpuIdFromPerfmMoOid,
        expLabResearchTargetId,
        expMkbId,
        expLpuIdFromMoOid,
        expKlProfitTypeId);
    // сохранение HstCaomiAcceptReferral
    Mockito.verify(caomiAcceptReferralRepository, Mockito.times(0))
        .save(Mockito.any(HstCaomiAcceptReferral.class));

    Assertions.assertEquals("Непредвиденное исключение при попытке сохранить запись lbr_Research", actualResponse.getErrorText());
  }

  @Test
  @DisplayName("Обработать запрос AcceptReferralRequest."
      + "Найдено 0 направлений, производится поиск МКАБ,"
      + "найден МКАБ по Полису,"
      + "новая запись lbr_LaboratoryResearch сохранена,"
      + "новая запись lbr_Laboratory сохранена,"
      + "данные сохранены в hst_caomiAcceptReferral."
      + "Успешный Response возвращен.")
  void processAcceptReferralRequestReferralNotFoundMkabByPolicyFoundLabResearchSavedLabResSavedAcceptReferralSaved_successResponseReturned(){
    // (1) prepare mocks:
    Mockito.when(caomiReferralRepository.findByCaomiId(Mockito.anyString()))
        .thenReturn(new ArrayList<>());

    HltMkab foundMkab = new HltMkab();
    foundMkab.setMkabId(1);
    Mockito.when(mkabRepository.findByPolicyNumber(Mockito.anyString()))
        .thenReturn(Optional.of(foundMkab));

    // *для части заполнения lbr_LaboratoryResearch из БД*
    Integer expRfSmoId = 100;
    Mockito.when(omsSmoRepository.findByCod(Mockito.anyString()))
        .thenReturn(Optional.of(OmsSmo.builder().smoId(expRfSmoId).build()));
    Integer expKlTipOmsId = 200;
    Mockito.when(omsKlTipOmsRepository.findByIdDoc(Mockito.anyInt()))
        .thenReturn(Optional.of(OmsKlTipOms.builder().klTipOmsId(expKlTipOmsId).build()));
    Integer expLpuIdFromMoOid = 300;
    Integer expLpuIdFromPerfmMoOid = 400;
    Mockito.when(omsLpuRepository.findTopByLic(Mockito.anyString()))
        .thenReturn(Optional.of(OmsLpu.builder().lpuId(expLpuIdFromMoOid).build()))
        .thenReturn(Optional.of(OmsLpu.builder().lpuId(expLpuIdFromPerfmMoOid).build()));
    Integer expLabResearchTargetId = 500;
    Mockito.when(labResearchTargetRepository.findByCode(Mockito.anyString()))
        .thenReturn(Optional.of(LbrLabResearchTarget.builder().labResearchTargetId(expLabResearchTargetId).build()));
    Integer expMkbId = 600;
    Mockito.when(omsMkbRepository.findByDs(Mockito.anyString()))
        .thenReturn(Optional.of(OmsMkb.builder().mkbId(expMkbId).build()));
    Integer expKlProfitTypeId = 700;
    Mockito.when(omsKlProfitTypeRepository.findByCode(Mockito.anyString()))
        .thenReturn(Optional.of(OmsKlProfitType.builder().klProfitTypeId(expKlProfitTypeId).build()));

    LbrLaboratoryResearch savedLabRes = new LbrLaboratoryResearch();
    savedLabRes.setLaboratoryResearchId(2000);
    savedLabRes.setGuid(UUID.randomUUID().toString());
    Mockito.when(lbrLaboratoryResearchRepository.save(Mockito.any(LbrLaboratoryResearch.class)))
        .thenReturn(savedLabRes);

    Mockito.when(lbrResearchRepository.save(Mockito.any(LbrResearch.class)))
        .thenReturn(new LbrResearch());
    final String returnedRfResTypeUguid = UUID.randomUUID().toString();
    Mockito.when(lbrResearchTypeRepository.findUGUIDByServiceRequestCode(Mockito.anyString()))
        .thenReturn(Optional.of(returnedRfResTypeUguid));

    // (2) start test:
    AcceptReferralRequest request = getFullyFilledRequest();
    Response actualResponse = service.processAcceptReferralRequest(request);

    // (3) check:
    Mockito.verify(caomiReferralRepository, Mockito.times(1))
        .findByCaomiId(Mockito.anyString());
    // сохранение LbrLaboratoryResearch
    assertLbrLaboratoryResearch(request,
        foundMkab.getMkabId(),
        expRfSmoId,
        expKlTipOmsId,
        expLpuIdFromPerfmMoOid,
        expLabResearchTargetId,
        expMkbId,
        expLpuIdFromMoOid,
        expKlProfitTypeId);
    // сохранение lbr_Research
    assertLbrResearch(request.getReferralNumber(), savedLabRes.getGuid(), returnedRfResTypeUguid);
    // сохранение HstCaomiAcceptReferral
    assertHstCaomiAcceptReferral(
        request,
        savedLabRes.getLaboratoryResearchId(),
        foundMkab.getMkabId());

    Assertions.assertEquals(ErrorCode.E000.getCode(), actualResponse.getErrorCode());
    Assertions.assertEquals(ErrorCode.E000.getMessage(), actualResponse.getErrorText());
    Assertions.assertNotNull(actualResponse.getDateTime());
    Assertions.assertEquals(request.getIdReferral(), actualResponse.getIdReferral());
  }

  @Test
  @DisplayName("Обработать запрос AcceptReferralRequest."
      + "Найдено 0 направлений, производится поиск МКАБ,"
      + "НЕ найден МКАБ по Полису,"
      + "найден МКАБ по Полному Имени и СНИЛС"
      + "новая запись lbr_LaboratoryResearch сохранена,"
      + "новая запись lbr_Laboratory сохранена,"
      + "данные сохранены в hst_caomiAcceptReferral."
      + "Успешный Response возвращен.")
  void processAcceptReferralRequestReferralNotFoundMkabByNameAndSnilsFoundLabResearchSavedLbResSavedAcceptReferralSaved_successResponseReturned(){
    // (1) prepare mocks:
    Mockito.when(caomiReferralRepository.findByCaomiId(Mockito.anyString()))
        .thenReturn(new ArrayList<>());

    HltMkab foundMkab = new HltMkab();
    foundMkab.setMkabId(1);
    Mockito.when(mkabRepository.findByPolicyNumber(Mockito.anyString()))
        .thenReturn(Optional.empty());
    Mockito.when(mkabRepository.findBySnilsAndLastNameAndNameAndPatronymic(
        Mockito.anyString(),
        Mockito.anyString(),
        Mockito.anyString(),
        Mockito.anyString()))
        .thenReturn(Optional.of(foundMkab));

    LbrLaboratoryResearch savedLabRes = new LbrLaboratoryResearch();
    savedLabRes.setLaboratoryResearchId(2000);
    Mockito.when(lbrLaboratoryResearchRepository.save(Mockito.any(LbrLaboratoryResearch.class)))
        .thenReturn(savedLabRes);

    Mockito.when(lbrResearchRepository.save(Mockito.any(LbrResearch.class)))
        .thenReturn(new LbrResearch());
    final String returnedRfResTypeUguid = UUID.randomUUID().toString();
    Mockito.when(lbrResearchTypeRepository.findUGUIDByServiceRequestCode(Mockito.anyString()))
        .thenReturn(Optional.of(returnedRfResTypeUguid));

    // (2) start test:
    AcceptReferralRequest request = getFullyFilledRequest();
    Response actualResponse = service.processAcceptReferralRequest(request);

    // (3) check:
    Mockito.verify(caomiReferralRepository, Mockito.times(1))
        .findByCaomiId(Mockito.anyString());
    // сохранение LbrLaboratoryResearch
    Mockito.verify(lbrLaboratoryResearchRepository, Mockito.times(1))
        .save(Mockito.any(LbrLaboratoryResearch.class));
    // сохранение lbr_Research
    assertLbrResearch(request.getReferralNumber(), savedLabRes.getGuid(), returnedRfResTypeUguid);
    // сохранение HstCaomiAcceptReferral
    assertHstCaomiAcceptReferral(request,
        savedLabRes.getLaboratoryResearchId(),
        foundMkab.getMkabId());

    Assertions.assertEquals(ErrorCode.E000.getCode(), actualResponse.getErrorCode());
    Assertions.assertEquals(ErrorCode.E000.getMessage(), actualResponse.getErrorText());
    Assertions.assertNotNull(actualResponse.getDateTime());
    Assertions.assertEquals(request.getIdReferral(), actualResponse.getIdReferral());
  }

  @Test
  @DisplayName("Обработать запрос AcceptReferralRequest."
      + "Найдено 0 направлений, производится поиск МКАБ,"
      + "НЕ найден МКАБ по Полису,"
      + "НЕ найден МКАБ по Полному Имени и СНИЛС,"
      + "найден МКАБ по Полному Имени и Дате Рождения"
      + "новая запись lbr_LaboratoryResearch сохранена,"
      + "новая запись lbr_Laboratory сохранена,"
      + "данные сохранены в hst_caomiAcceptReferral."
      + "Успешный Response возвращен.")
  void processAcceptReferralRequestReferralNotFoundMkabByNameAndBirthDateFoundLabResearchSavedLbResSavedAcceptReferralSaved_successResponseReturned(){
    // (1) prepare mocks:
    Mockito.when(caomiReferralRepository.findByCaomiId(Mockito.anyString()))
        .thenReturn(new ArrayList<>());

    HltMkab foundMkab = new HltMkab();
    foundMkab.setMkabId(1);
    Mockito.when(mkabRepository.findByPolicyNumber(Mockito.anyString()))
        .thenReturn(Optional.empty());
    Mockito.when(mkabRepository.findBySnilsAndLastNameAndNameAndPatronymic(
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.anyString())).thenReturn(Optional.empty());
    Mockito.when(mkabRepository.findByLastNameAndNameAndPatronymicAndBirthDate(
        Mockito.anyString(),
        Mockito.anyString(),
        Mockito.anyString(),
        Mockito.any(LocalDateTime.class)
    )).thenReturn(Optional.of(foundMkab));

    LbrLaboratoryResearch savedLabRes = new LbrLaboratoryResearch();
    savedLabRes.setLaboratoryResearchId(2000);
    Mockito.when(lbrLaboratoryResearchRepository.save(Mockito.any(LbrLaboratoryResearch.class)))
        .thenReturn(savedLabRes);

    Mockito.when(lbrResearchRepository.save(Mockito.any(LbrResearch.class)))
        .thenReturn(new LbrResearch());
    final String returnedRfResTypeUguid = UUID.randomUUID().toString();
    Mockito.when(lbrResearchTypeRepository.findUGUIDByServiceRequestCode(Mockito.anyString()))
        .thenReturn(Optional.of(returnedRfResTypeUguid));

    // (2) start test:
    AcceptReferralRequest request = getFullyFilledRequest();
    Response actualResponse = service.processAcceptReferralRequest(request);

    // (3) check:
    Mockito.verify(caomiReferralRepository, Mockito.times(1))
        .findByCaomiId(Mockito.anyString());
    // сохранение LbrLaboratoryResearch
    Mockito.verify(lbrLaboratoryResearchRepository, Mockito.times(1))
        .save(Mockito.any(LbrLaboratoryResearch.class));
    // сохранение lbr_Research
    assertLbrResearch(request.getReferralNumber(), savedLabRes.getGuid(), returnedRfResTypeUguid);
    // сохранение HstCaomiAcceptReferral
    assertHstCaomiAcceptReferral(request,
        savedLabRes.getLaboratoryResearchId(),
        foundMkab.getMkabId());

    Assertions.assertEquals(ErrorCode.E000.getCode(), actualResponse.getErrorCode());
    Assertions.assertEquals(ErrorCode.E000.getMessage(), actualResponse.getErrorText());
    Assertions.assertNotNull(actualResponse.getDateTime());
    Assertions.assertEquals(request.getIdReferral(), actualResponse.getIdReferral());
  }

  @Test
  @DisplayName("Обработать запрос AcceptReferralRequest."
      + "Найдено 0 направлений, производится поиск МКАБ,"
      + "НЕ найден МКАБ по Полису,"
      + "НЕ найден МКАБ по Полному Имени и СНИЛС,"
      + "НЕ найден МКАБ по Полному Имени и Дате Рождения, сохраняем записи в нужные таблицы самостоятельно,"
      + "МКАБ НЕ сохранен успешно,"
      + "Response с ошибкой возвращен.")
  void processAcceptReferralRequestReferralNotFoundNoMkabsFoundSavingMkabThrewException_errorResponseReturned(){
    // (1) prepare mocks:
    Mockito.when(caomiReferralRepository.findByCaomiId(Mockito.anyString()))
        .thenReturn(new ArrayList<>());

    Mockito.when(mkabRepository.findByPolicyNumber(Mockito.anyString()))
        .thenReturn(Optional.empty());
    Mockito.when(mkabRepository.findBySnilsAndLastNameAndNameAndPatronymic(
        Mockito.anyString(),
        Mockito.anyString(),
        Mockito.anyString(),
        Mockito.anyString())).thenReturn(Optional.empty());
    Mockito.when(mkabRepository.findByLastNameAndNameAndPatronymicAndBirthDate(
        Mockito.anyString(),
        Mockito.anyString(),
        Mockito.anyString(),
        Mockito.any(LocalDateTime.class)
    )).thenReturn(Optional.empty());

    Mockito.doThrow(new RuntimeException()).when(mkabRepository).save(Mockito.any(HltMkab.class));

    // (2) start test:
    AcceptReferralRequest request = getFullyFilledRequest();
    Response actualResponse = service.processAcceptReferralRequest(request);

    // (3) check:
    Mockito.verify(caomiReferralRepository, Mockito.times(1))
        .findByCaomiId(Mockito.anyString());

    Mockito.verify(mkabRepository, Mockito.times(1)).findByPolicyNumber(Mockito.anyString());
    Mockito.verify(mkabRepository, Mockito.times(1)).findBySnilsAndLastNameAndNameAndPatronymic(
        Mockito.anyString(),
        Mockito.anyString(),
        Mockito.anyString(),
        Mockito.anyString());
    Mockito.verify(mkabRepository, Mockito.times(1)).findByLastNameAndNameAndPatronymicAndBirthDate(
        Mockito.anyString(),
        Mockito.anyString(),
        Mockito.anyString(),
        Mockito.any(LocalDateTime.class));

    Mockito.verify(mkabRepository, Mockito.times(1)).save(Mockito.any(HltMkab.class));

    Mockito.verify(caomiAcceptReferralRepository, Mockito.times(0))
        .save(Mockito.any(HstCaomiAcceptReferral.class));

    Assertions.assertEquals("Непредвиденное исключение при попытке сохранить новую запись hlt_MKAB",
        actualResponse.getErrorText());
  }

  @Test
  @DisplayName("Обработать запрос AcceptReferralRequest."
      + "Найдено 0 направлений, производится поиск МКАБ,"
      + "НЕ найден МКАБ по Полису,"
      + "НЕ найден МКАБ по Полному Имени и СНИЛС,"
      + "НЕ найден МКАБ по Полному Имени и Дате Рождения, сохраняем записи в нужные таблицы самостоятельно,"
      + "МКАБ сохранен успешно,"
      + "ПолисМКАБ НЕ сохранен успешно,"
      + "Response с ошибкой возвращен.")
  void processAcceptReferralRequestReferralNotFoundNoMkabsFoundMkabSvaedPolicyMkabThrewError_errorResponseReturned(){
    // (1) prepare mocks:
    Mockito.when(caomiReferralRepository.findByCaomiId(Mockito.anyString()))
        .thenReturn(new ArrayList<>());

    Mockito.when(mkabRepository.findByPolicyNumber(Mockito.anyString()))
        .thenReturn(Optional.empty());
    Mockito.when(mkabRepository.findBySnilsAndLastNameAndNameAndPatronymic(
        Mockito.anyString(),
        Mockito.anyString(),
        Mockito.anyString(),
        Mockito.anyString())).thenReturn(Optional.empty());
    Mockito.when(mkabRepository.findByLastNameAndNameAndPatronymicAndBirthDate(
        Mockito.anyString(),
        Mockito.anyString(),
        Mockito.anyString(),
        Mockito.any(LocalDateTime.class)
    )).thenReturn(Optional.empty());

    Mockito.when(mkabRepository.save(Mockito.any(HltMkab.class)))
        .thenReturn(new HltMkab());
    Mockito.doThrow(new RuntimeException()).when(polisMkabRepository).save(Mockito.any(HltPolisMkab.class));

    // (2) start test:
    AcceptReferralRequest request = getFullyFilledRequest();
    Response actualResponse = service.processAcceptReferralRequest(request);

    // (3) check:
    Mockito.verify(caomiReferralRepository, Mockito.times(1))
        .findByCaomiId(Mockito.anyString());

    Mockito.verify(mkabRepository, Mockito.times(1)).findByPolicyNumber(Mockito.anyString());
    Mockito.verify(mkabRepository, Mockito.times(1)).findBySnilsAndLastNameAndNameAndPatronymic(
        Mockito.anyString(),
        Mockito.anyString(),
        Mockito.anyString(),
        Mockito.anyString());
    Mockito.verify(mkabRepository, Mockito.times(1)).findByLastNameAndNameAndPatronymicAndBirthDate(
        Mockito.anyString(),
        Mockito.anyString(),
        Mockito.anyString(),
        Mockito.any(LocalDateTime.class));

    Mockito.verify(mkabRepository, Mockito.times(1)).save(Mockito.any(HltMkab.class));
    Mockito.verify(polisMkabRepository, Mockito.times(1)).save(Mockito.any(HltPolisMkab.class));

    Mockito.verify(caomiAcceptReferralRepository, Mockito.times(0))
        .save(Mockito.any(HstCaomiAcceptReferral.class));

    Assertions.assertEquals("Непредвиденное исключение при попытке сохранить новую запись hlt_PolisMKAB",
        actualResponse.getErrorText());
  }

  @Test
  @DisplayName("Обработать запрос AcceptReferralRequest."
      + "Найдено 0 направлений, производится поиск МКАБ,"
      + "НЕ найден МКАБ по Полису,"
      + "НЕ найден МКАБ по Полному Имени и СНИЛС,"
      + "НЕ найден МКАБ по Полному Имени и Дате Рождения, сохраняем записи в нужные таблицы самостоятельно,"
      + "МКАБ сохранен успешно,"
      + "ПолисМКАБ сохранен успешно,"
      + "lbr_LaboratoryResearch НЕ сохранен успешно."
      + "Response с ошибкой возвращен.")
  void processAcceptReferralRequestReferralNotFoundNoMkabsFoundMkabSavedPolicyMkabSavedLabResThrewException_errorResponseReturned(){
    // (1) prepare mocks:
    Mockito.when(caomiReferralRepository.findByCaomiId(Mockito.anyString()))
        .thenReturn(new ArrayList<>());

    Mockito.when(mkabRepository.findByPolicyNumber(Mockito.anyString()))
        .thenReturn(Optional.empty());
    Mockito.when(mkabRepository.findBySnilsAndLastNameAndNameAndPatronymic(
        Mockito.anyString(),
        Mockito.anyString(),
        Mockito.anyString(),
        Mockito.anyString())).thenReturn(Optional.empty());
    Mockito.when(mkabRepository.findByLastNameAndNameAndPatronymicAndBirthDate(
        Mockito.anyString(),
        Mockito.anyString(),
        Mockito.anyString(),
        Mockito.any(LocalDateTime.class)
    )).thenReturn(Optional.empty());

    Mockito.when(mkabRepository.save(Mockito.any(HltMkab.class)))
        .thenReturn(new HltMkab());
    Mockito.when(polisMkabRepository.save(Mockito.any(HltPolisMkab.class)))
        .thenReturn(new HltPolisMkab());
    Mockito.doThrow(new RuntimeException()).when(lbrLaboratoryResearchRepository)
        .save(Mockito.any(LbrLaboratoryResearch.class));

    // (2) start test:
    AcceptReferralRequest request = getFullyFilledRequest();
    Response actualResponse = service.processAcceptReferralRequest(request);

    // (3) check:
    Mockito.verify(caomiReferralRepository, Mockito.times(1))
        .findByCaomiId(Mockito.anyString());

    Mockito.verify(mkabRepository, Mockito.times(1)).findByPolicyNumber(Mockito.anyString());
    Mockito.verify(mkabRepository, Mockito.times(1)).findBySnilsAndLastNameAndNameAndPatronymic(
        Mockito.anyString(),
        Mockito.anyString(),
        Mockito.anyString(),
        Mockito.anyString());
    Mockito.verify(mkabRepository, Mockito.times(1)).findByLastNameAndNameAndPatronymicAndBirthDate(
        Mockito.anyString(),
        Mockito.anyString(),
        Mockito.anyString(),
        Mockito.any(LocalDateTime.class));

    Mockito.verify(mkabRepository, Mockito.times(1)).save(Mockito.any(HltMkab.class));
    Mockito.verify(polisMkabRepository, Mockito.times(1)).save(Mockito.any(HltPolisMkab.class));
    Mockito.verify(lbrLaboratoryResearchRepository, Mockito.times(1))
            .save(Mockito.any(LbrLaboratoryResearch.class));

    Mockito.verify(caomiAcceptReferralRepository, Mockito.times(0))
            .save(Mockito.any(HstCaomiAcceptReferral.class));

    Assertions.assertEquals("Непредвиденное исключение при попытке сохранить новую запись lbr_LaboratoryResearch",
        actualResponse.getErrorText());
  }

  @Test
  @DisplayName("Обработать запрос AcceptReferralRequest."
      + "Найдено 0 направлений, производится поиск МКАБ,"
      + "НЕ найден МКАБ по Полису,"
      + "НЕ найден МКАБ по Полному Имени и СНИЛС,"
      + "НЕ найден МКАБ по Полному Имени и Дате Рождения, сохраняем записи в нужные таблицы самостоятельно,"
      + "МКАБ сохранен успешно,"
      + "ПолисМКАБ сохранен успешно,"
      + "lbr_LaboratoryResearch сохранен успешно,"
      + "lbr_Laboratory НЕ сохранен успешно."
      + "Response с ошибкой возвращен.")
  void processAcceptReferralRequestReferralNotFoundNoMkabsFoundMkabSavedPolicyMkabSavedLabResSavedLbrResException_errorResponseReturned(){
    // (1) prepare mocks:
    Mockito.when(caomiReferralRepository.findByCaomiId(Mockito.anyString()))
        .thenReturn(new ArrayList<>());

    Mockito.when(mkabRepository.findByPolicyNumber(Mockito.anyString()))
        .thenReturn(Optional.empty());
    Mockito.when(mkabRepository.findBySnilsAndLastNameAndNameAndPatronymic(
        Mockito.anyString(),
        Mockito.anyString(),
        Mockito.anyString(),
        Mockito.anyString())).thenReturn(Optional.empty());
    Mockito.when(mkabRepository.findByLastNameAndNameAndPatronymicAndBirthDate(
        Mockito.anyString(),
        Mockito.anyString(),
        Mockito.anyString(),
        Mockito.any(LocalDateTime.class)
    )).thenReturn(Optional.empty());

    Mockito.when(mkabRepository.save(Mockito.any(HltMkab.class)))
        .thenReturn(new HltMkab());
    Mockito.when(polisMkabRepository.save(Mockito.any(HltPolisMkab.class)))
        .thenReturn(new HltPolisMkab());
    // сохранение новой записи в lbr_LaboratoryResearch
    Integer expRfSmoId = 100;
    Mockito.when(omsSmoRepository.findByCod(Mockito.anyString()))
        .thenReturn(Optional.of(OmsSmo.builder().smoId(expRfSmoId).build()));
    Integer expKlTipOmsId = 200;
    Mockito.when(omsKlTipOmsRepository.findByIdDoc(Mockito.anyInt()))
        .thenReturn(Optional.of(OmsKlTipOms.builder().klTipOmsId(expKlTipOmsId).build()));
    Integer expLpuIdFromMoOid = 300;
    Integer expLpuIdFromPerfmMoOid = 400;
    Mockito.when(omsLpuRepository.findTopByLic(Mockito.anyString()))
        .thenReturn(Optional.of(OmsLpu.builder().lpuId(expLpuIdFromPerfmMoOid).build()))
        .thenReturn(Optional.of(OmsLpu.builder().lpuId(expLpuIdFromMoOid).build()))
        .thenReturn(Optional.of(OmsLpu.builder().lpuId(expLpuIdFromPerfmMoOid).build()));
    Integer expLabResearchTargetId = 500;
    Mockito.when(labResearchTargetRepository.findByCode(Mockito.anyString()))
        .thenReturn(Optional.of(LbrLabResearchTarget.builder().labResearchTargetId(expLabResearchTargetId).build()));
    Integer expMkbId = 600;
    Mockito.when(omsMkbRepository.findByDs(Mockito.anyString()))
        .thenReturn(Optional.of(OmsMkb.builder().mkbId(expMkbId).build()));
    Integer expKlProfitTypeId = 700;
    Mockito.when(omsKlProfitTypeRepository.findByCode(Mockito.anyString()))
        .thenReturn(Optional.of(OmsKlProfitType.builder().klProfitTypeId(expKlProfitTypeId).build()));
    LbrLaboratoryResearch savedLabRes = new LbrLaboratoryResearch();
    savedLabRes.setLaboratoryResearchId(2);
    savedLabRes.setGuid(UUID.randomUUID().toString());
    Mockito.when(lbrLaboratoryResearchRepository.save(Mockito.any(LbrLaboratoryResearch.class)))
        .thenReturn(savedLabRes);

    Mockito.doThrow(new RuntimeException()).when(lbrResearchRepository)
        .save(Mockito.any(LbrResearch.class));

    // (2) start test:
    AcceptReferralRequest request = getFullyFilledRequest();
    Response actualResponse = service.processAcceptReferralRequest(request);

    // (3) check:
    Mockito.verify(caomiReferralRepository, Mockito.times(1))
        .findByCaomiId(Mockito.anyString());

    Mockito.verify(mkabRepository, Mockito.times(1)).findByPolicyNumber(Mockito.anyString());
    Mockito.verify(mkabRepository, Mockito.times(1)).findBySnilsAndLastNameAndNameAndPatronymic(
        Mockito.anyString(),
        Mockito.anyString(),
        Mockito.anyString(),
        Mockito.anyString());
    Mockito.verify(mkabRepository, Mockito.times(1)).findByLastNameAndNameAndPatronymicAndBirthDate(
        Mockito.anyString(),
        Mockito.anyString(),
        Mockito.anyString(),
        Mockito.any(LocalDateTime.class));

    Mockito.verify(mkabRepository, Mockito.times(1)).save(Mockito.any(HltMkab.class));
    Mockito.verify(polisMkabRepository, Mockito.times(1)).save(Mockito.any(HltPolisMkab.class));
    Mockito.verify(lbrLaboratoryResearchRepository, Mockito.times(1))
        .save(Mockito.any(LbrLaboratoryResearch.class));

    Mockito.verify(caomiAcceptReferralRepository, Mockito.times(0))
        .save(Mockito.any(HstCaomiAcceptReferral.class));

    Assertions.assertEquals("Непредвиденное исключение при попытке сохранить запись lbr_Research",
        actualResponse.getErrorText());
  }

  @Test
  @DisplayName("Обработать запрос AcceptReferralRequest."
      + "Найдено 0 направлений, производится поиск МКАБ,"
      + "НЕ найден МКАБ по Полису,"
      + "НЕ найден МКАБ по Полному Имени и СНИЛС,"
      + "НЕ найден МКАБ по Полному Имени и Дате Рождения, сохраняем записи в нужные таблицы самостоятельно,"
      + "МКАБ сохранен успешно,"
      + "ПолисМКАБ сохранен успешно,"
      + "lbr_LaboratoryResearch сохранен успешно,"
      + "lbr_Laboratory сохранен успешно,"
      + "данные сохранены в hst_caomiAcceptReferral."
      + "Успешный Response возвращен.")
  void processAcceptReferralRequestReferralNotFoundNoMkabsFoundMkabSavedPolicyMkabSavedLabResSavedLbResSavedAccepReferralSaved_successResponseReturned(){
    // (1) prepare mocks:
    Mockito.when(caomiReferralRepository.findByCaomiId(Mockito.anyString()))
        .thenReturn(new ArrayList<>());

    Mockito.when(mkabRepository.findByPolicyNumber(Mockito.anyString()))
        .thenReturn(Optional.empty());
    Mockito.when(mkabRepository.findBySnilsAndLastNameAndNameAndPatronymic(
        Mockito.anyString(),
        Mockito.anyString(),
        Mockito.anyString(),
        Mockito.anyString())).thenReturn(Optional.empty());
    Mockito.when(mkabRepository.findByLastNameAndNameAndPatronymicAndBirthDate(
        Mockito.anyString(),
        Mockito.anyString(),
        Mockito.anyString(),
        Mockito.any(LocalDateTime.class)
    )).thenReturn(Optional.empty());

    // сохранение новой записи hlt_MKAB
    Integer expSexId = 10;
    Mockito.when(omsKlSexRepository.findByCode(Mockito.anyString()))
        .thenReturn(Optional.of(OmsKlSex.builder().klSexId(expSexId).build()));
    Integer exRfKlTipOmsId = 30;
    Mockito.when(omsKlTipOmsRepository.findByIdDoc(Mockito.anyInt()))
        .thenReturn(Optional.of(OmsKlTipOms.builder().klTipOmsId(exRfKlTipOmsId).build()));
    HltMkab savedMkab = new HltMkab();
    savedMkab.setMkabId(1);
    savedMkab.setRfSmoId(2);
    savedMkab.setRfKlTipOmsId(3);
    savedMkab.setDatePolBegin(LocalDateTime.MIN);
    savedMkab.setDatePolEnd(LocalDateTime.MAX);
    savedMkab.setPolicySeries("policySeries");
    savedMkab.setPolicyNumber("policyNumber");
    Mockito.when(mkabRepository.save(Mockito.any(HltMkab.class)))
        .thenReturn(savedMkab);
    // сохранение новой записи hlt_PolisMKAB
    Mockito.when(omsKlProfitTypeRepository.findByCode("1"))
            .thenReturn(Optional.of(OmsKlProfitType.builder().klProfitTypeId(4).build()));
    Mockito.when(polisMkabRepository.save(Mockito.any(HltPolisMkab.class)))
        .thenReturn(new HltPolisMkab());
    // сохранение новой записи в lbr_LaboratoryResearch
    Integer expRfSmoId = 100;
    Mockito.when(omsSmoRepository.findByCod(Mockito.anyString()))
        .thenReturn(Optional.of(OmsSmo.builder().smoId(expRfSmoId).build()));
    Integer expKlTipOmsId = 200;
    Mockito.when(omsKlTipOmsRepository.findByIdDoc(Mockito.anyInt()))
        .thenReturn(Optional.of(OmsKlTipOms.builder().klTipOmsId(expKlTipOmsId).build()));
    Integer expLpuIdFromMoOid = 300;
    Integer expLpuIdFromPerfmMoOid = 400;
    Mockito.when(omsLpuRepository.findTopByLic(Mockito.anyString()))
        .thenReturn(Optional.of(OmsLpu.builder().lpuId(expLpuIdFromPerfmMoOid).build()))
        .thenReturn(Optional.of(OmsLpu.builder().lpuId(expLpuIdFromMoOid).build()))
        .thenReturn(Optional.of(OmsLpu.builder().lpuId(expLpuIdFromPerfmMoOid).build()));
    Integer expLabResearchTargetId = 500;
    Mockito.when(labResearchTargetRepository.findByCode(Mockito.anyString()))
        .thenReturn(Optional.of(LbrLabResearchTarget.builder().labResearchTargetId(expLabResearchTargetId).build()));
    Integer expMkbId = 600;
    Mockito.when(omsMkbRepository.findByDs(Mockito.anyString()))
        .thenReturn(Optional.of(OmsMkb.builder().mkbId(expMkbId).build()));
    Integer expKlProfitTypeId = 700;
    Mockito.when(omsKlProfitTypeRepository.findByCode(Mockito.anyString()))
        .thenReturn(Optional.of(OmsKlProfitType.builder().klProfitTypeId(expKlProfitTypeId).build()));
    LbrLaboratoryResearch savedLabRes = new LbrLaboratoryResearch();
    savedLabRes.setLaboratoryResearchId(2);
    savedLabRes.setGuid(UUID.randomUUID().toString());
    Mockito.when(lbrLaboratoryResearchRepository.save(Mockito.any(LbrLaboratoryResearch.class)))
        .thenReturn(savedLabRes);

    Mockito.when(lbrResearchRepository.save(Mockito.any(LbrResearch.class)))
        .thenReturn(new LbrResearch());
    final String returnedRfResTypeUguid = UUID.randomUUID().toString();
    Mockito.when(lbrResearchTypeRepository.findUGUIDByServiceRequestCode(Mockito.anyString()))
        .thenReturn(Optional.of(returnedRfResTypeUguid));

    // (2) start test:
    AcceptReferralRequest request = getFullyFilledRequest();
    request.getServiceRequest().get(0).setPerformerMoOid("123.12345.0.1");
    Response actualResponse = service.processAcceptReferralRequest(request);

    // (3) check:
    Mockito.verify(caomiReferralRepository, Mockito.times(1))
        .findByCaomiId(Mockito.anyString());

    Mockito.verify(mkabRepository, Mockito.times(1)).findByPolicyNumber(Mockito.anyString());
    Mockito.verify(mkabRepository, Mockito.times(1)).findBySnilsAndLastNameAndNameAndPatronymic(
        Mockito.anyString(),
        Mockito.anyString(),
        Mockito.anyString(),
        Mockito.anyString());
    Mockito.verify(mkabRepository, Mockito.times(1)).findByLastNameAndNameAndPatronymicAndBirthDate(
        Mockito.anyString(),
        Mockito.anyString(),
        Mockito.anyString(),
        Mockito.any(LocalDateTime.class));

    ArgumentCaptor<HltMkab> mkabCaptor = ArgumentCaptor.forClass(HltMkab.class);
    Mockito.verify(mkabRepository, Mockito.times(1)).save(mkabCaptor.capture());
    HltMkab actualMkab = mkabCaptor.getValue();
    Assertions.assertEquals(expSexId, actualMkab.getRfKlSexId());
    Assertions.assertEquals(request.getPatient().getFullName().getLastName(),
        actualMkab.getLastName());
    Assertions.assertEquals(request.getPatient().getFullName().getFirstName(),
        actualMkab.getName());
    Assertions.assertEquals(request.getPatient().getFullName().getMiddleName(),
        actualMkab.getPatronymic());
    Assertions.assertEquals(request.getPatient().getSnils(),
        actualMkab.getSnils());
    Assertions.assertEquals(request.getPatient().getBirthDate().atStartOfDay(),
        actualMkab.getBirthDate());
    Assertions.assertEquals(expRfSmoId, actualMkab.getRfSmoId());
    Assertions.assertEquals(expKlTipOmsId, actualMkab.getRfKlTipOmsId());
    Assertions.assertEquals(request.getCoverage().getValidityPeriod().getStart().atStartOfDay(),
        actualMkab.getDatePolBegin());
    Assertions.assertEquals(request.getCoverage().getValidityPeriod().getEnd().atStartOfDay(),
        actualMkab.getDatePolEnd());
    Assertions.assertEquals("policySeries", actualMkab.getPolicySeries());
    Assertions.assertEquals("policyNumber", actualMkab.getPolicyNumber());
    Assertions.assertEquals(expLpuIdFromPerfmMoOid, actualMkab.getRfLpuId());

    ArgumentCaptor<HltPolisMkab> polisMkabCaptor = ArgumentCaptor.forClass(HltPolisMkab.class);
    Mockito.verify(polisMkabRepository, Mockito.times(1)).save(polisMkabCaptor.capture());
    HltPolisMkab actualPolisMkab = polisMkabCaptor.getValue();
    Assertions.assertEquals(savedMkab.getRfSmoId(), actualPolisMkab.getRfSmoId());
    Assertions.assertEquals(savedMkab.getRfKlTipOmsId(), actualPolisMkab.getRfKlTipOmsId());
    Assertions.assertEquals(savedMkab.getDatePolBegin(), actualPolisMkab.getDatePolBegin());
    Assertions.assertEquals(savedMkab.getDatePolEnd(), actualPolisMkab.getDatePolEnd());
    Assertions.assertEquals(savedMkab.getPolicySeries(), actualPolisMkab.getPolicySeries());
    Assertions.assertEquals(savedMkab.getPolicyNumber(), actualPolisMkab.getPolicyNumber());
    Assertions.assertTrue(actualPolisMkab.getIsActive());
    Assertions.assertEquals(0, actualPolisMkab.getFlags());
    Assertions.assertEquals(0, actualPolisMkab.getRfDogovorId());
    Assertions.assertNotNull(actualPolisMkab.getGuid());

    assertLbrLaboratoryResearch(request,
        savedMkab.getMkabId(),
        expRfSmoId,
        expKlTipOmsId,
        expLpuIdFromPerfmMoOid,
        expLabResearchTargetId,
        expMkbId,
        expLpuIdFromMoOid,
        expKlProfitTypeId);

    assertLbrResearch(request.getReferralNumber(), savedLabRes.getGuid(), returnedRfResTypeUguid);

    assertHstCaomiAcceptReferral(request,
        savedLabRes.getLaboratoryResearchId(),
        savedMkab.getMkabId());

    Assertions.assertEquals(ErrorCode.E000.getCode(), actualResponse.getErrorCode());
    Assertions.assertEquals(ErrorCode.E000.getMessage(), actualResponse.getErrorText());
    Assertions.assertNotNull(actualResponse.getDateTime());
    Assertions.assertEquals(request.getIdReferral(), actualResponse.getIdReferral());
  }

  private AcceptReferralRequest getFullyFilledRequest() {
    return AcceptReferralRequest.builder()
        .idReferral(UUID.randomUUID())
        .referralNumber("refNum")
        .moOid("moOid")
        .patient(
            Patient.builder()
                .fullName(
                    FullName.builder()
                        .lastName("lastName")
                        .firstName("fisrtName")
                        .middleName("middleName")
                        .build())
                .snils("snils")
                .birthDate(LocalDate.MIN)
                .gender(1L)
                .build())
        .coverage(
            Coverage.builder()
                .medicalInsuranceOrganizationCode("medInsOrgCode")
                .policyTypeCode(1L)
                .policyNumber("policySeries:policyNumber")
                .validityPeriod(CoverageValidityPeriod.builder()
                    .start(LocalDate.MIN)
                    .end(LocalDate.MAX)
                    .build())
                .build())
        .practitioner(
            Practitioner.builder()
                .fullName(FullName.builder()
                    .lastName("last  NameP")
                    .firstName("name P")
                    .middleName("miDdleNameP")
                    .build())
                .medStaffId("medStaffId")
                .snils("practSnils")
                .build()
        )
        .serviceRequest(List.of(
            ServiceRequest.builder()
                .performerMoOid("perfMoOid")
                .authoredOn(OffsetDateTime.MIN)
                .serviceIntentCode(3L)
                .reasonCode("reasonCode")
                .performerDeviceId("perDevId")
                .serviceCode("serviceCode")
                .build()))
        .build();
  }

  private void assertHstCaomiAcceptReferral(AcceptReferralRequest request, Integer expLabResId, Integer expMkabId){
    ArgumentCaptor<HstCaomiAcceptReferral> acceptReferralCaptor = ArgumentCaptor.forClass(HstCaomiAcceptReferral.class);
    Mockito.verify(caomiAcceptReferralRepository, Mockito.times(1))
        .save(acceptReferralCaptor.capture());
    HstCaomiAcceptReferral actualAcceptReferral = acceptReferralCaptor.getValue();
    Assertions.assertEquals(request.getIdReferral().toString(), actualAcceptReferral.getCaomiId());
    Assertions.assertNotNull(actualAcceptReferral.getDateCreate());
    Assertions.assertEquals(request.getMoOid(), actualAcceptReferral.getRecipientMoOid());
    Assertions.assertEquals(request.getServiceRequest().get(0).getPerformerMoOid(),
        actualAcceptReferral.getPerformerMoOid());
    Assertions.assertEquals(request.getServiceRequest().get(0).getPerformerDeviceId(),
        actualAcceptReferral.getPerformerDeviceId());
    Assertions.assertEquals(0, actualAcceptReferral.getStatusId());
    Assertions.assertEquals(expLabResId, actualAcceptReferral.getLbrLaboratoryResearchId());
    Assertions.assertEquals(expMkabId, actualAcceptReferral.getMkabId());
    Mockito.verify(caomiAcceptReferralRepository, Mockito.times(1))
        .save(Mockito.any(HstCaomiAcceptReferral.class));
  }

  private void assertLbrLaboratoryResearch(AcceptReferralRequest request,
      Integer expMkabId, Integer expRfSmoId, Integer expKlTipOmsId,
      Integer expLpuIdFromPerfmMoOid, Integer expLabResearchTargetId,
      Integer expMkbId, Integer expLpuIdFromMoOid, Integer expKlProfitTypeId){
    ArgumentCaptor<LbrLaboratoryResearch> lbrLaboratoryResearchCaptor = ArgumentCaptor.forClass(LbrLaboratoryResearch.class);
    Mockito.verify(lbrLaboratoryResearchRepository, Mockito.times(1))
        .save(lbrLaboratoryResearchCaptor.capture());
    LbrLaboratoryResearch actualLab = lbrLaboratoryResearchCaptor.getValue();
    Assertions.assertEquals(request.getReferralNumber(), actualLab.getNumber());
    Assertions.assertEquals(expMkabId, actualLab.getRfMkabId());
    Assertions.assertEquals(
        request.getPatient().getFullName().getLastName(),
        actualLab.getPatFamily()
    );
    Assertions.assertEquals(
        request.getPatient().getFullName().getFirstName(),
        actualLab.getPatName()
    );
    Assertions.assertEquals(
        request.getPatient().getFullName().getMiddleName(),
        actualLab.getPatOt()
    );
    Assertions.assertEquals(request.getPatient().getBirthDate().atStartOfDay(),
        actualLab.getPatBirthday());
    Assertions.assertTrue(actualLab.getPatW());
    Assertions.assertEquals(expRfSmoId, actualLab.getRfSmoId());
    Assertions.assertEquals(expKlTipOmsId, actualLab.getRfKlTipOmsId());
    Assertions.assertEquals("policySeries", actualLab.getPatSPol());
    Assertions.assertEquals("policyNumber", actualLab.getPatNPol());
    Assertions.assertTrue(actualLab.getIsReadOnly());
    Assertions.assertEquals(expLpuIdFromPerfmMoOid, actualLab.getRfLpuId());
    Assertions.assertNotNull(actualLab.getDateCreate());
    Assertions.assertEquals(request.getIdReferral().toString(), actualLab.getAccessionNumber());
    Assertions.assertEquals(request.getPatient().getSnils(), actualLab.getPatSs());
    Assertions.assertNotNull(actualLab.getGuid());
    Assertions.assertEquals("Lastnamep Namep Middlenamep", actualLab.getDoctFio());
    Assertions.assertEquals(request.getPractitioner().getMedStaffId(),
        actualLab.getDocPcod());
    Assertions.assertEquals(request.getPractitioner().getSnils(),
        actualLab.getDoctSs());
    Assertions.assertEquals(request.getServiceRequest().get(0).getAuthoredOn().toLocalDateTime(),
        actualLab.getDateDirection());
    Assertions.assertEquals(expLabResearchTargetId, actualLab.getRfLabResearchTargetId());
    Assertions.assertEquals(expMkbId, actualLab.getRfMKBID());
    Assertions.assertEquals(expLpuIdFromMoOid, actualLab.getRfLpuSenderId());
    Assertions.assertEquals(expKlProfitTypeId, actualLab.getRfKlProfitTypeId());
  }

  public void assertLbrResearch(String referralNumber, String labResGuid, String rfResTypeUguid) {
    ArgumentCaptor<LbrResearch> lbrResearchCaptor = ArgumentCaptor.forClass(LbrResearch.class);
    Mockito.verify(lbrResearchRepository, Mockito.times(1))
        .save(lbrResearchCaptor.capture());
    final LbrResearch savedLbrRes = lbrResearchCaptor.getValue();
    Assertions.assertEquals(referralNumber, savedLbrRes.getNumber());
    Assertions.assertEquals(labResGuid, savedLbrRes.getRfLaboratoryResearchGUID());
    Assertions.assertFalse(savedLbrRes.getIsComplete());
    Assertions.assertEquals(0, savedLbrRes.getFlag());
    Assertions.assertEquals(rfResTypeUguid, savedLbrRes.getRfResearchTypeUguid());
  }
}