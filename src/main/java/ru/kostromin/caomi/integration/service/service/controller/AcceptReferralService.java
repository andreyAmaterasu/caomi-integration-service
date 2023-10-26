package ru.kostromin.caomi.integration.service.service.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.kostromin.caomi.integration.service.controller.request.common.FullName;
import ru.kostromin.caomi.integration.service.controller.request.common.Practitioner;
import ru.kostromin.caomi.integration.service.controller.request.refaccept.AcceptReferralRequest;
import ru.kostromin.caomi.integration.service.controller.request.refaccept.Coverage;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class AcceptReferralService {

  private final static Pattern extractMainOidPattern = Pattern.compile("(.*)\\.0\\.");
  private final static LocalDateTime DEFAULT_DATE_TIME = LocalDateTime.of(1900, 1, 1, 0, 0);
  private final static String DEFAULT_UGUID = "00000000-0000-0000-0000-000000000000";

  private final HstCaomiReferralRepository caomiReferralRepository;
  private final HstCaomiAcceptReferralRepository caomiAcceptReferralRepository;
  private final LbrLaboratoryResearchRepository lbrLaboratoryResearchRepository;
  private final HltMkabRepository mkabRepository;
  private final OmsSmoRepository omsSmoRepository;
  private final OmsLpuRepository omsLpuRepository;
  private final OmsKlSexRepository omsKlSexRepository;
  private final OmsKlTipOmsRepository omsKlTipOmsRepository;
  private final OmsKlProfitTypeRepository omsKlProfitTypeRepository;
  private final HltPolisMkabRepository polisMkabRepository;
  private final LbrLabResearchTargetRepository labResearchTargetRepository;
  private final OmsMkbRepository omsMkbRepository;
  private final LbrResearchRepository lbrResearchRepository;
  private final LbrResearchTypeRepository lbrResearchTypeRepository;

  /**
   * Обработать запрос AcceptReferralRequest
   * @param request - AcceptReferralRequest
   * @return - Response
   */
  public Response processAcceptReferralRequest(AcceptReferralRequest request){
    FullName patientFullNameCapitalized =
        capitalizeFullNameAndRidOfSpaces(request.getPatient().getFullName());
    request.getPatient().setFullName(patientFullNameCapitalized);
    FullName practitionerFullNameCapitalized =
        capitalizeFullNameAndRidOfSpaces(request.getPractitioner().getFullName());
    request.getPractitioner().setFullName(practitionerFullNameCapitalized);

    // Проверка наличие направления в таблице hst_caomiReferral
    List<HstCaomiReferral> caomiReferrals = caomiReferralRepository.findByCaomiId(request.getIdReferral().toString());
    if (caomiReferrals.size() > 1){
      return Response.builder()
          .errorCode(ErrorCode.E006.getCode())
          .errorText(
              String.format("Для идентификатора {%s} найдено больше 1 направления",
                  request.getIdReferral().toString())
          )
          .build();
    }
    if (caomiReferrals.size() == 1){
      HstCaomiReferral caomiReferral = caomiReferrals.get(0);

      Optional<LbrLaboratoryResearch> laboratoryResearchOpt = lbrLaboratoryResearchRepository.findById(caomiReferral.getLbrLaboratoryResearchId());
      if(laboratoryResearchOpt.isEmpty()){
        return Response.builder()
            .errorCode(ErrorCode.E006.getCode())
            .errorText(
                String.format("Для направления с идентификатором {%s} не найдено lbr_LaboratoryResearch с id = {%s}",
                    request.getIdReferral().toString(), caomiReferral.getLbrLaboratoryResearchId())
            )
            .build();
      }
      // (A) обновть существующую запись lbr_LaboratoryResearch
      final LbrLaboratoryResearch lbrLaboratoryResearch = laboratoryResearchOpt.get();
      lbrLaboratoryResearch.setAccessionNumber(request.getIdReferral().toString());
      lbrLaboratoryResearchRepository.save(lbrLaboratoryResearch);
      // затем сохранить информацию в hst_caomiAcceptReferral
      return saveHstCaomiAcceptReferral(request, caomiReferral.getLbrLaboratoryResearchId(), laboratoryResearchOpt.get().getRfMkabId());
    }
    // (B) обработать запрос при не найденном направлении
    return processReferralNotPresentRequest(request);
  }

  /**
   * Сохранить данные в таблицу hst_caomiAcceptReferral
   * @param request - запрос
   * @param laboratoryResearchId - lbr_LaboratoryResearch.LaboratoryResearchID
   * @param mkabId - hlt_MKAB.MKABID
   * @return Response
   */
  private Response saveHstCaomiAcceptReferral(AcceptReferralRequest request, Integer laboratoryResearchId, Integer mkabId){
    ServiceRequest serviceRequest = request.getServiceRequest().get(0);
    try{
      HstCaomiAcceptReferral hstCaomiAcceptReferral = HstCaomiAcceptReferral.builder()
          .caomiId(request.getIdReferral().toString())
          .dateCreate(LocalDateTime.now())
          .recipientMoOid(request.getMoOid())
          .performerMoOid(serviceRequest.getPerformerMoOid())
          .performerDeviceId(serviceRequest.getPerformerDeviceId() != null ? serviceRequest.getPerformerDeviceId() : "")
          .statusId(0)
          .lbrLaboratoryResearchId(laboratoryResearchId)
          .mkabId(mkabId)
          .build();
      caomiAcceptReferralRepository.save(hstCaomiAcceptReferral);
    } catch (Exception e){
      log.error("Непредвиденное исключение при попытке создать новую запись в hst_caomiAcceptReferral "
              + "(связанную с idReferral = {}, moOid = {}, performerMoOid = {}, performerDeviceId = {})",
          request.getIdReferral(),
          request.getMoOid(),
          serviceRequest.getPerformerMoOid(),
          serviceRequest.getPerformerDeviceId(),
          e
      );
      return Response.builder()
          .errorText("Не удалось создать новую запись в таблице hst_caomiAcceptReferral для "
              + "ServiceRequest [idReferral, moOid, performerMoOid, performerDeviceId]: " + String.format("[%s, %s, %s, %s],",
              request.getIdReferral(),
              request.getMoOid(),
              serviceRequest.getPerformerMoOid(),
              serviceRequest.getPerformerDeviceId()))
          .build();
    }

    return Response.builder()
        .errorCode(ErrorCode.E000.getCode())
        .errorText(ErrorCode.E000.getMessage())
        .dateTime(LocalDateTime.now())
        .idReferral(request.getIdReferral())
        .build();
  }

  /**
   * Обработать запрос при не найденном направлении hst_caomiReferral
   * @param request - запрос
   * @return Response
   */
  private Response processReferralNotPresentRequest(AcceptReferralRequest request){
    // (B.1) Проверить наличие МКАБ в БД:
    // (а) по Полису
    Optional<HltMkab> mkabBySnilsNumberOpt = mkabRepository.findByPolicyNumber(request.getCoverage().getPolicyNumber());
    if (mkabBySnilsNumberOpt.isPresent()){
      return saveNewLabResearchAndHstCaomiAcceptReferral(mkabBySnilsNumberOpt.get().getMkabId(), request);
    }
    // (б) по Полному Имени и Снилс
    Optional<HltMkab> mkabByFullNameAndSnilsOpt = mkabRepository.findBySnilsAndLastNameAndNameAndPatronymic(
        request.getPatient().getSnils(),
        request.getPatient().getFullName().getLastName(),
        request.getPatient().getFullName().getFirstName(),
        request.getPatient().getFullName().getMiddleName() != null
            ? request.getPatient().getFullName().getMiddleName()
            : ""
    );
    if(mkabByFullNameAndSnilsOpt.isPresent()){
      return saveNewLabResearchAndHstCaomiAcceptReferral(mkabByFullNameAndSnilsOpt.get().getMkabId(), request);
    }
    // (в) по Полному Имени и Дате Рождения
    Optional<HltMkab> mkabByFullNameAndBirthDateOpt = mkabRepository.findByLastNameAndNameAndPatronymicAndBirthDate(
        request.getPatient().getFullName().getLastName(),
        request.getPatient().getFullName().getFirstName(),
        request.getPatient().getFullName().getMiddleName() != null
            ? request.getPatient().getFullName().getMiddleName()
            : "",
        request.getPatient().getBirthDate().atStartOfDay()
    );
    if(mkabByFullNameAndBirthDateOpt.isPresent()){
      return saveNewLabResearchAndHstCaomiAcceptReferral(mkabByFullNameAndBirthDateOpt.get().getMkabId(), request);
    }
    // (B.2) не нашли МКАБ а б в - создать необходимые записи самостоятельно
    return saveNewRelatedEntriesAndHstCaomiAcceptReferral(request);
  }

  /**
   * Добавить lbr_LaboratoryResearch, затем lbr_Research, и сохранить информацию в hst_caomiAcceptReferral
   * @param mkabId - МКАБ id
   * @param request - запрос
   * @return Response
   */
  private Response saveNewLabResearchAndHstCaomiAcceptReferral(Integer mkabId, AcceptReferralRequest request){
    final Optional<LbrLaboratoryResearch> laboratoryResearchOpt = createAndSaveNewLaboratoryResearch(mkabId, request);
    if (laboratoryResearchOpt.isEmpty()){
      return Response.builder()
          .errorText("Непредвиденное исключение при попытке сохранить новую запись lbr_LaboratoryResearch")
          .build();
    }
    final Optional<LbrResearch> lbrResearchOpt = createAndSaveNewLbrResearch(request, laboratoryResearchOpt.get().getGuid());
    if (lbrResearchOpt.isEmpty()) {
      return Response.builder()
          .errorText("Непредвиденное исключение при попытке сохранить запись lbr_Research")
          .build();
    }
    return saveHstCaomiAcceptReferral(request,
        laboratoryResearchOpt.get().getLaboratoryResearchId(),
        mkabId);
  }

  /**
   * Создать новые записи hlt_MKAB, hlt_PolisMKAB, lbr_LaboratoryResearch, затем lbr_Research в БД,
   * сохранить данные в hst_caomiAcceptReferral
   * @param request - запрос
   * @return Response
   */
  private Response saveNewRelatedEntriesAndHstCaomiAcceptReferral(AcceptReferralRequest request) {
    // добавить hlt_MKAB
    HltMkab savedMkab = createAndSaveNewHltMkab(request);
    if (savedMkab == null){
      return Response.builder()
          .errorText("Непредвиденное исключение при попытке сохранить новую запись hlt_MKAB")
          .build();
    }
    // добавить hlt_PolisMKAB
    HltPolisMkab hltPolisMkab = createAndSaveNewHltPolisMkab(savedMkab, request);
    if (hltPolisMkab == null){
      return Response.builder()
          .errorText("Непредвиденное исключение при попытке сохранить новую запись hlt_PolisMKAB")
          .build();
    }
    // добавить lbr_LaboratoryResearch
    final Optional<LbrLaboratoryResearch> laboratoryResearchOpt = createAndSaveNewLaboratoryResearch(savedMkab.getMkabId(), request);
    if (laboratoryResearchOpt.isEmpty()){
      return Response.builder()
          .errorText("Непредвиденное исключение при попытке сохранить новую запись lbr_LaboratoryResearch")
          .build();
    }
    // добавить lbr_Research
    return createAndSaveNewLbrResearch(request, laboratoryResearchOpt.get().getGuid())
        // сохранить информацию в hst_caomiAcceptReferral
        .map(lbrResearch -> saveHstCaomiAcceptReferral(request, laboratoryResearchOpt.get().getLaboratoryResearchId(), savedMkab.getMkabId()))
        .orElseGet(() -> Response.builder()
            .errorText("Непредвиденное исключение при попытке сохранить запись lbr_Research")
            .build());
  }

  private HltMkab createAndSaveNewHltMkab(AcceptReferralRequest request) {
    try{
      HltMkab hltMkab = new HltMkab();

      Patient patient = request.getPatient();
      Coverage coverage = request.getCoverage();

      hltMkab.setLastName(patient.getFullName().getLastName());
      hltMkab.setName(patient.getFullName().getFirstName());
      hltMkab.setPatronymic(patient.getFullName().getMiddleName() != null ?
          patient.getFullName().getMiddleName() : "");
      hltMkab.setSnils(patient.getSnils());
      hltMkab.setBirthDate(patient.getBirthDate().atStartOfDay());

      Integer rfSmoId = 0;
      if (coverage.getMedicalInsuranceOrganizationCode() != null){
        rfSmoId = omsSmoRepository.findByCod(coverage.getMedicalInsuranceOrganizationCode())
            .map(OmsSmo::getSmoId)
            .orElse(rfSmoId);
      }
      hltMkab.setRfSmoId(rfSmoId);

      final String lic = extractMainOid(request.getServiceRequest().get(0).getPerformerMoOid());
      hltMkab.setRfLpuId(omsLpuRepository.findTopByLic(lic)
          .map(OmsLpu::getLpuId)
          .orElse(0));
      hltMkab.setPolicySeries(retrievePolicySeries(coverage.getPolicyNumber()));
      hltMkab.setPolicyNumber(retrievePolicyNumber(coverage.getPolicyNumber()));

      LocalDateTime datePolBegin = DEFAULT_DATE_TIME;
      LocalDateTime datePolEnd = DEFAULT_DATE_TIME;
      if (coverage.getValidityPeriod() != null){
        datePolBegin = coverage.getValidityPeriod().getStart() !=null
            ? coverage.getValidityPeriod().getStart().atStartOfDay()
            : datePolBegin;
        datePolEnd = coverage.getValidityPeriod().getEnd() !=null
            ? coverage.getValidityPeriod().getEnd().atStartOfDay()
            : datePolEnd;
      }
      hltMkab.setDatePolBegin(datePolBegin);
      hltMkab.setDatePolEnd(datePolEnd);

      hltMkab.setRfKlSexId(omsKlSexRepository.findByCode(
              String.valueOf(patient.getGender()))
          .map(OmsKlSex::getKlSexId)
          .orElse(0));
      hltMkab.setRfKlTipOmsId(omsKlTipOmsRepository.findByIdDoc(
              coverage.getPolicyTypeCode().intValue())
          .map(OmsKlTipOms::getKlTipOmsId)
          .orElse(0));
      hltMkab.setUguid(UUID.randomUUID().toString());
      return mkabRepository.save(hltMkab);
    } catch (Exception e){
      log.error("Непредвиденное исключение при создании новой записи hlt_MKAB: ", e);
      return null;
    }
  }

  private HltPolisMkab createAndSaveNewHltPolisMkab(HltMkab hltMkab, AcceptReferralRequest request) {
    try{
      HltPolisMkab hltPolisMkab = new HltPolisMkab();
      hltPolisMkab.setRfMkabId(hltMkab.getMkabId());
      hltPolisMkab.setPolicySeries(hltMkab.getPolicySeries());
      hltPolisMkab.setPolicyNumber(hltMkab.getPolicyNumber());
      hltPolisMkab.setRfKlProfitTypeId(omsKlProfitTypeRepository.findByCode("1")
          .map(OmsKlProfitType::getKlProfitTypeId)
          .orElse(0));
      hltPolisMkab.setDatePolBegin(hltMkab.getDatePolBegin());
      hltPolisMkab.setDatePolEnd(hltMkab.getDatePolEnd());
      hltPolisMkab.setRfSmoId(hltMkab.getRfSmoId());
      hltPolisMkab.setIsActive(request.getCoverage().getPolicyTypeCode() == 1
          ? Boolean.TRUE
          : Boolean.FALSE);
      hltPolisMkab.setRfKlTipOmsId(hltMkab.getRfKlTipOmsId());
      hltPolisMkab.setFlags(0);
      hltPolisMkab.setRfDogovorId(0);
      hltPolisMkab.setGuid(UUID.randomUUID().toString());

      return polisMkabRepository.save(hltPolisMkab);
    } catch (Exception e){
      log.error("Непредвиденное исключение при попытке сохранить новую запись hlt_PolisMKAB: ", e);
      return null;
    }
  }

  /**
   * Сохранить в БД новую запись lbr_LaboratoryResearch
   * @param mkabId - идентификатор МКАБ
   * @param request - запрос
   * @return Optional(lbr_LaboratoryResearch)
   */
  private Optional<LbrLaboratoryResearch> createAndSaveNewLaboratoryResearch(Integer mkabId, AcceptReferralRequest request) {
    try{
      ServiceRequest serviceRequest = request.getServiceRequest().get(0);
      Practitioner practitioner = request.getPractitioner();
      FullName fullNamePract = practitioner.getFullName();

      LbrLaboratoryResearch lbrLaboratoryResearch = new LbrLaboratoryResearch();
      lbrLaboratoryResearch.setNumber(request.getReferralNumber());
      lbrLaboratoryResearch.setRfMkabId(mkabId);
      lbrLaboratoryResearch.setPatFamily(request.getPatient().getFullName().getLastName());
      lbrLaboratoryResearch.setPatName(request.getPatient().getFullName().getFirstName());
      lbrLaboratoryResearch.setPatOt(request.getPatient().getFullName().getMiddleName() != null
          ? request.getPatient().getFullName().getMiddleName()
          : "");
      String practitionerFullName =
          fullNamePract.getLastName() + " " + fullNamePract.getFirstName() + " "
              + (StringUtils.hasText(fullNamePract.getMiddleName()) ? fullNamePract.getMiddleName() : "");
      lbrLaboratoryResearch.setDoctFio(practitionerFullName);
      lbrLaboratoryResearch.setDocPcod(practitioner.getMedStaffId());
      LocalDateTime dateDirection = serviceRequest.getAuthoredOn() != null
          ? serviceRequest.getAuthoredOn().toLocalDateTime()
          : DEFAULT_DATE_TIME;
      lbrLaboratoryResearch.setDateDirection(dateDirection);
      lbrLaboratoryResearch.setRfMKBID(
          omsMkbRepository.findByDs(serviceRequest.getReasonCode())
              .map(OmsMkb::getMkbId)
              .orElse(0)
      );
      lbrLaboratoryResearch.setPatBirthday(request.getPatient().getBirthDate().atStartOfDay());
      lbrLaboratoryResearch.setPatW(request.getPatient().getGender() == 1 ? Boolean.TRUE : Boolean.FALSE);

      Integer rfSmoId = 0;
      if (request.getCoverage().getMedicalInsuranceOrganizationCode() != null){
        rfSmoId = omsSmoRepository.findByCod(request.getCoverage().getMedicalInsuranceOrganizationCode())
            .map(OmsSmo::getSmoId)
            .orElse(rfSmoId);
      }
      lbrLaboratoryResearch.setRfSmoId(rfSmoId);

      lbrLaboratoryResearch.setRfKlTipOmsId(
          omsKlTipOmsRepository.findByIdDoc(
              request.getCoverage().getPolicyTypeCode().intValue())
          .map(OmsKlTipOms::getKlTipOmsId)
          .orElse(0));
      lbrLaboratoryResearch.setPatSPol(retrievePolicySeries(request.getCoverage().getPolicyNumber()));
      lbrLaboratoryResearch.setPatNPol(retrievePolicyNumber(request.getCoverage().getPolicyNumber()));
      lbrLaboratoryResearch.setRfLpuSenderId(
          omsLpuRepository.findTopByLic(request.getMoOid()).map(OmsLpu::getLpuId).orElse(0)
      );
      lbrLaboratoryResearch.setRfKlProfitTypeId(
          omsKlProfitTypeRepository.findByCode(request.getCoverage().getPolicyTypeCode().toString())
              .map(OmsKlProfitType::getKlProfitTypeId)
              .orElse(0)
      );
      lbrLaboratoryResearch.setIsReadOnly(Boolean.TRUE);
      lbrLaboratoryResearch.setRfLpuId(
          omsLpuRepository.findTopByLic(request.getServiceRequest().get(0).getPerformerMoOid())
          .map(OmsLpu::getLpuId)
          .orElse(0));
      lbrLaboratoryResearch.setRfLabResearchTargetId(
          labResearchTargetRepository.findByCode(serviceRequest.getServiceIntentCode().toString())
              .map(LbrLabResearchTarget::getLabResearchTargetId)
              .orElse(0)
      );
      lbrLaboratoryResearch.setDateCreate(LocalDateTime.now());
      lbrLaboratoryResearch.setAccessionNumber(request.getIdReferral().toString());
      lbrLaboratoryResearch.setPatSs(request.getPatient().getSnils());
      lbrLaboratoryResearch.setDoctSs(practitioner.getSnils() != null
          ? practitioner.getSnils()
          : "");
      lbrLaboratoryResearch.setGuid(UUID.randomUUID().toString());

      return Optional.of(lbrLaboratoryResearchRepository.save(lbrLaboratoryResearch));
    } catch (Exception e){
      log.error("Непредвиденное исключение при попытке сохранить новую запись lbr_LaboratoryResearch: ", e);
      return Optional.empty();
    }
  }

  /**
   * Сохранить в БД новую запись lbr_Research
   * @param request - запрос
   * @param lbrLabResGuid - значение GUID таблицы lbr_LaboratoryResearch
   * @return Optional(lbr_Research)
   */
  private Optional<LbrResearch> createAndSaveNewLbrResearch(AcceptReferralRequest request, String lbrLabResGuid) {
    try {
      final LbrResearch lbrResearch = LbrResearch.builder()
          .number(request.getReferralNumber())
          .rfLaboratoryResearchGUID(lbrLabResGuid)
          .isComplete(false)
          .flag(0)
          .rfResearchTypeUguid(lbrResearchTypeRepository.findUGUIDByServiceRequestCode(
              request.getServiceRequest().get(0).getServiceCode()).orElse(DEFAULT_UGUID)
          )
          // оставшиеся дефолтные значения
          .labDoctorFio("")
          .dateComplete(DEFAULT_DATE_TIME)
          .isPerformed(false)
          .datePerformed(DEFAULT_DATE_TIME)
          .conclusion("")
          .isCancelled(false)
          .studyUid("")
          .performedDocFio("")
          .isIssued(false)
          .dateIssued(DEFAULT_DATE_TIME)
          .isRegistered(false)
          .isReceipt(false)
          .isIemkData(false)
          .isMainExpert(false)
          .performedLpuName("")
          .isRejected(false)
          .isCompleteEarly(false)
          .build();
      return Optional.of(lbrResearchRepository.save(lbrResearch));
    } catch (Exception e){
      log.error("Непредвиденное исключение при попытке сохранить новую запись lbr_Research: ", e);
      return Optional.empty();
    }
  }

  /**
   * Извлечь серию полиса (ТОЛЬКО до двоеточия)
   * @param policy - цельный полис
   * @return серия полиса
   */
  private String retrievePolicySeries(String policy){
    String policySeries = "";
    if(StringUtils.hasText(policy)){
      Pattern patternSeries = Pattern.compile(".*(?=:)");
      Matcher matcher = patternSeries.matcher(policy);
      if(matcher.find()){
        policySeries = matcher.group();
      }
    }
    return policySeries;
  }

  /**
   * Извлечь номер полиса (после двоеточия или цельный)
   * @param policy - цельный полис
   * @return номер полиса
   */
  private String retrievePolicyNumber(String policy){
    String policyNumber = "";
    if (StringUtils.hasText(policy)){
      Pattern patternNumber = Pattern.compile("(?<=:).*");
      Matcher matcher = patternNumber.matcher(policy);
      if(matcher.find()){
        policyNumber = matcher.group();
      } else {
        policyNumber = policy;
      }
    }
    return policyNumber;
  }

  /**
   * Капитализировать Полное Имя и избавится от пробелов
   * @param fullName - Полное Имя
   */
  private FullName capitalizeFullNameAndRidOfSpaces(FullName fullName){
    fullName.setLastName(StringUtils.capitalize(fullName.getLastName().toLowerCase(Locale.ROOT).replaceAll("\\s+", "")));
    fullName.setFirstName(StringUtils.capitalize(fullName.getFirstName().toLowerCase(Locale.ROOT).replaceAll("\\s+", "")));
    if (fullName.getMiddleName() != null){
      fullName.setMiddleName(StringUtils.capitalize(fullName.getMiddleName().toLowerCase(Locale.ROOT).replaceAll("\\s+", "")));
    }
    return fullName;
  }

  /**
   * Метод для извлечения головного OID.
   * Головным считается oid до символа '.0.' если символа нет, считаем
   * переданный oid головным
   * @param oid - oбщий id
   * @return - головной oid
   */
  private String extractMainOid(String oid) {
    if (!oid.contains(".0.")) {
      return oid;
    }
    final Matcher matcher = extractMainOidPattern.matcher(oid);
    if (matcher.find()) {
      return matcher.group(1);
    } else {
      return oid;
    }
  }
}
