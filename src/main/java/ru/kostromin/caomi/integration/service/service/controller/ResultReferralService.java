package ru.kostromin.caomi.integration.service.service.controller;

import io.vavr.control.Either;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import io.vavr.control.Try;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.kostromin.caomi.integration.service.config.ResultReferralEndpointConfig;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class ResultReferralService {

  private final static LocalDateTime DEFAULT_DATE_TIME = LocalDateTime.of(1900, 1, 1, 0, 0);
  private final static String DEFAULT_UUID = "00000000-0000-0000-0000-000000000000";
  private final static Integer LBR_RESEARCH_FLAG_VALUE = 64;

  private final HstCaomiReferralRepository caomiReferralRepository;
  private final HltMkabRepository hltMkabRepository;
  private final LbrResearchTypeRepository lbrResearchTypeRepository;
  private final LbrResearchRepository lbrResearchRepository;
  private final LbrLaboratoryResearchRepository lbrLaboratoryResearchRepository;
  private final OmsKlNowServiceRepository omsKlNowServiceRepository;
  private final ResultReferralEndpointConfig referralEndpointConfig;

  /**
   * Обработать запрос ResultReferralRequest
   * @param request - ResultReferralRequest
   * @return Response
   */
  public Response processResultReferralRequest(ResultReferralRequest request){
    // (1) проверить наличие записи в hst_caomiReferral
    List<HstCaomiReferral> foundCaomiReferrals =
        caomiReferralRepository.findByCaomiId(request.getIdReferral().toString());
    if(foundCaomiReferrals.isEmpty()){
      return Response.builder()
          .errorCode(ErrorCode.E006.getCode())
          .errorText(
              String.format("Направление с идентификатором {%s} не найдено",
                  request.getIdReferral().toString())
          )
          .build();
    }
    if(foundCaomiReferrals.size() > 1){
      return Response.builder()
          .errorCode(ErrorCode.E006.getCode())
          .errorText(
              String.format("Для идентификатора {%s} найдено больше 1 направления",
                  request.getIdReferral().toString())
          )
          .build();
    }
    // доп. - проверить lbrLaboratoryResearchID, необходимую для lbr_Research, на заполненность
    final Integer lbrLaboratoryResearchId = foundCaomiReferrals.get(0).getLbrLaboratoryResearchId();
    if (lbrLaboratoryResearchId == null) {
      return Response.builder()
          .errorCode(ErrorCode.E006.getCode())
          .errorText(
              String.format("Для идентификатора {%s} в таблице hst_caomiReferral не заполнен lbrLaboratoryResearchID",
                  request.getIdReferral().toString())
          )
          .build();
    }

    // (2) проверить наличие каждого ServiceResult в таблице hlt_MKAB
    List<ServiceResult> absentByPatientResults = request.getServiceResult().stream().filter(serviceResult -> {
      log.info("Проверка пациента с идентификатором {}", serviceResult.getPatientId());
      return !hltMkabRepository.existsByUguid(serviceResult.getPatientId());
    }).collect(Collectors.toList());
    if (!absentByPatientResults.isEmpty()) {
      return assembleAbsentPatientResponse(absentByPatientResults, request.getIdReferral().toString());
    }

    // (3) проверить наличие каждого ServiceResult в таблице lbr_Research
    List<ServiceResult> absentByServiceResults = request.getServiceResult().stream().filter(serviceResult -> {
      log.info("Проверка услуги с идентификатором {}", serviceResult.getServiceId());
      return Try.of(() -> !lbrResearchRepository.existsByResearchId(Integer.valueOf(serviceResult.getServiceId())))
              .getOrElseGet(e -> {
                log.error("Не удалось привести полученный ServiceResult.ServiceId к типу Integer: {}", e.getMessage());
                return true;
              });
    }).collect(Collectors.toList());
    if (!absentByServiceResults.isEmpty()) {
      return assembleAbsentServiceResponse(absentByServiceResults, request.getIdReferral().toString());
    }

    // (4) найти LbrLaboratoryResearch и обновить AccessionNumber
    final Optional<LbrLaboratoryResearch> laboratoryResearchOpt = lbrLaboratoryResearchRepository.findById(lbrLaboratoryResearchId);
    if (laboratoryResearchOpt.isEmpty()) {
      return Response.builder()
          .errorCode(ErrorCode.E006.getCode())
          .errorText(String.format("Не удалось найти запись lbr_LaboratoryResearch (где LaboratoryResearchID = %s),"
              + "необходимую для последующего поиска обновляемых записей lbr_Research", lbrLaboratoryResearchId))
          .build();
    }
    laboratoryResearchOpt.get().setAccessionNumber(request.getIdReferral().toString());
    lbrLaboratoryResearchRepository.save(laboratoryResearchOpt.get());

    // (5) обновляем запись в таблице lbr_Research для каждого ServiceResult
    return processIncomingServiceResults(request, laboratoryResearchOpt.get());
  }

  /**
   * Создать ответ "не найден пациент"
   */
  private Response assembleAbsentPatientResponse(List<ServiceResult> notExistingByPatientResults, String idReferral){
    String errorText = notExistingByPatientResults.stream().map(
        notExistingResult -> {
          return String.format("У направления с идентификатором {%s} не найден пациент с идентификатором {%s}",
              idReferral,
              notExistingResult.getPatientId());
        }).collect(Collectors.joining(","));

    return Response.builder()
        .errorCode(ErrorCode.E006.getCode())
        .errorText(errorText)
        .build();
  }

  /**
   * Создать ответ "не найдена услуга"
   */
  private Response assembleAbsentServiceResponse(List<ServiceResult> notExistingByServiceResults, String idReferral) {
    String errorText = notExistingByServiceResults.stream().map(
        notExistingResult -> {
          return String.format("У направления с идентификатором {%s} не найдена услуга с идентификатором {%s}",
              idReferral,
              notExistingResult.getServiceId());
        }).collect(Collectors.joining(","));

    return Response.builder()
        .errorCode(ErrorCode.E006.getCode())
        .errorText(errorText)
        .build();
  }

  /**
   * Обработать приходящие ServiceResult
   * @param request - пришедший запрос
   * @param laboratoryResearch - запись lbr_LaboratoryResearch
   * @return - Response
   */
  private Response processIncomingServiceResults(ResultReferralRequest request, LbrLaboratoryResearch laboratoryResearch) {
    String errorMessage = request.getServiceResult().stream().map(serviceResult -> {
      try {
        return findLbrResearchAndUpdateOrReturnErrorMessage(serviceResult, request.getIdReferral(), laboratoryResearch);
      } catch (Exception e){
        log.error(
            "Непредвиденное исключение при попытке обновить запись в таблице lbr_Research "
                + "(связанную с idReferral = {}, serviceId = {}, patientId = {}): ",
            request.getIdReferral(),
            serviceResult.getServiceId(),
            serviceResult.getPatientId(),
            e);
        return createSavingErrorMessageForServiceResult(
            "Непредвиденное исключение при попытке обновить запись в таблице lbr_Research",
            request.getIdReferral(),
            serviceResult);
      }
    }).collect(Collectors.joining());

    if (!errorMessage.isEmpty()){
      return createLbrResearchSavingErrorResponse(errorMessage);
    }

    return Response.builder()
        .errorText(ErrorCode.E000.getMessage())
        .errorCode(ErrorCode.E000.getCode())
        .dateTime(LocalDateTime.now())
        .idReferral(request.getIdReferral())
        .build();
  }

  /**
   * Найти запись lbr_Research для обновления и обновить её,
   * иначе вернуть сообщение об ошибке
   * @param serviceResult - конкретный ServiceResult по которому ищется lbr_Research
   * @param idReferral - idReferral изначального запроса (для сообщения в случае ошибки)
   * @param lbrLaboratoryResearch - запись lbr_LaboratoryResearch (необходима для поиска lbr_Research)
   * @return - сообщение об не найденом lbr_Research/пустая строка при успешном обновлении всех записей
   */
  private String findLbrResearchAndUpdateOrReturnErrorMessage(ServiceResult serviceResult, UUID idReferral, LbrLaboratoryResearch lbrLaboratoryResearch){
    return Optional.of(serviceResult).stream()
        .map(sr -> {
          // (5.1) найти LbrResearch связанную с LbrLaboratoryResearch
          LbrResearch lbrResearchToUpdate;
          final List<LbrResearch> lbrResearches = lbrResearchRepository.findByRfLaboratoryResearchGUID(lbrLaboratoryResearch.getGuid());
          if (lbrResearches.isEmpty()) {
            return Either.left(createSavingErrorMessageForServiceResult("Не удалось найти запись lbr_Research для обновления",
                idReferral, sr));
          } else if (lbrResearches.size() > 1) {
            if (!StringUtils.hasText(sr.getServiceCode())) {
              // если отсутсвует ServiceResult.code - найти подходящюю LbrResearch, среди нескольких, невозможно
              return Either.left(createSavingErrorMessageForServiceResult("Было найдено несколько lbr_Research для обновления, но"
                      + " найти подходящую запись невозможно т.к. ServiceResult.code - null или пустая строка",
                  idReferral, sr));
            }
            // есть ServiceResult.code и найдено несколько LbrResearch для обновления - найти подходящую
            lbrResearchToUpdate =
                findOneLbrResearchToUpdateWhenMultipleFound(lbrResearches, sr.getServiceCode());
            if (lbrResearchToUpdate == null){
              return Either.left(createSavingErrorMessageForServiceResult("Было найдено несколько записей lbr_Research, "
                      + "но среди них отсутсвует подходящая запись для обновления",
                  idReferral, sr));
            }
          } else {
            // если найдена 1 LbrResearch - это нужная запись для обновления
            lbrResearchToUpdate = lbrResearches.get(0);
          }
          return Either.right(HolderLbrResearchToUpdateData.builder()
              .lbrLaboratoryResearchGuid(lbrLaboratoryResearch.getGuid())
              .serviceResult(sr)
              .lbrResearchToUpdate(lbrResearchToUpdate)
              .build());
        })
        .peek(eitherRecord -> {
          if (eitherRecord.isRight()) {
            // (6) обновляем найденную запись lbr_Research
            eitherRecord.peek(record -> updateLbrResearch((HolderLbrResearchToUpdateData) record));
          }
        })
        .filter(Either::isLeft)
        .map(Either::getLeft)
        .map(Object::toString)
        .collect(Collectors.joining());
  }

  /**
   * Создать сообщение об ошибке при обработке конкретного ServiceResult
   */
  private String createSavingErrorMessageForServiceResult(
      String explainingMessage,
      UUID requestIdReferral,
      ServiceResult serviceResult) {
    return explainingMessage
        + String.format(
        "[%s, %s, %s, %s];",
        requestIdReferral,
        serviceResult.getServiceId(),
        serviceResult.getPatientId(),
        serviceResult.getServiceCode());
  }

  /**
   * Найти единственную запись LbrResearch для обновления (если найдено несколько)
   * @param foundEntries - список найденных LbrResearch
   * @param serviceCode - ServiceResult.serviceCode
   * @return - запись для обновления\null
   */
  private LbrResearch findOneLbrResearchToUpdateWhenMultipleFound(List<LbrResearch> foundEntries, String serviceCode) {
    return foundEntries.stream().filter(lbrResearch -> {
      // найти LbrResearchType связанную с данной LbrResearch
      Optional<LbrResearchType> lbrResearchTypeOpt = lbrResearchTypeRepository.findByUguid(lbrResearch.getRfResearchTypeUguid());
      if (lbrResearchTypeOpt.isEmpty()) {
        return false;
      }
      // найти OmsKlNowService связанную с LbrResearchType
      Optional<OmsKlNowService> omsKlNowServiceOpt = omsKlNowServiceRepository.findById(lbrResearchTypeOpt.get().getRfKlNomServiceId());
      if (omsKlNowServiceOpt.isEmpty()) {
        return false;
      }
      // проверить что serviceCode существует в OmsKlNowService
      return omsKlNowServiceRepository.existsByCode(serviceCode);
    }).findFirst().orElse(null);
  }

  /**
   * Обновить запись lbr_Research
   */
  private void updateLbrResearch(HolderLbrResearchToUpdateData holder) {
    final LbrResearch lbrResearchToUpdate = holder.getLbrResearchToUpdate();
    final ServiceResult serviceResult = holder.getServiceResult();
    final String lbrLaboratoryResearchGuid = holder.getLbrLaboratoryResearchGuid();

    final Practitioner practitioner = serviceResult.getPractitioner();
    final String fullName = (practitioner != null && practitioner.getFullName() != null)
        ? practitioner.getFullName().getLastName()
        + " "
        + practitioner.getFullName().getFirstName()
        + " "
        + practitioner.getFullName().getMiddleName()
        : "";
    lbrResearchToUpdate.setLabDoctorFio(fullName);
    lbrResearchToUpdate.setDateComplete(serviceResult.getEffectiveDateTime() != null ?
            serviceResult.getEffectiveDateTime().toLocalDateTime()
            : DEFAULT_DATE_TIME);
    lbrResearchToUpdate.setIsComplete(Boolean.TRUE);
    lbrResearchToUpdate.setFlag(LBR_RESEARCH_FLAG_VALUE);
    lbrResearchToUpdate.setRfLaboratoryResearchGUID(lbrLaboratoryResearchGuid);
    lbrResearchToUpdate.setIsPerformed(Boolean.TRUE);
    lbrResearchToUpdate.setDatePerformed(serviceResult.getEffectiveDateTime() != null ?
            serviceResult.getEffectiveDateTime().toLocalDateTime()
            : DEFAULT_DATE_TIME);
    lbrResearchToUpdate.setConclusion(Optional.ofNullable(serviceResult.getPresentedForm())
            .map(resourcePresentedForms ->
                resourcePresentedForms.stream()
                    .filter(resourcePresentedForm ->
                        // выбираем тип контента
                        resourcePresentedForm.getContentType().contains(referralEndpointConfig.getContentType()))
                    .findFirst()
                    .map(ResourcePresentedForm::getData)
                    .map(this::extractEncodedConclusionFromRequest)
                    .map(this::ridOfHtmlTagsAndSetNewLine)
                    .map(String::trim)
                    .orElse("")
            )
            .orElse(""));
    lbrResearchToUpdate.setIsCancelled(Boolean.FALSE);
    lbrResearchToUpdate.setStudyUid(Optional.ofNullable(serviceResult.getImagingStudy())
            .map(imagingStudyMis -> imagingStudyMis.get(0))
            .map(ImagingStudyMis::getStudyUid)
            .orElse(DEFAULT_UUID));
    lbrResearchToUpdate.setPerformedDocFio(fullName);
    lbrResearchToUpdate.setIsIssued(Boolean.TRUE);
    lbrResearchToUpdate.setDateIssued(serviceResult.getIssued() != null ?
            serviceResult.getIssued().toLocalDateTime()
            : DEFAULT_DATE_TIME);
    lbrResearchToUpdate.setIsRegistered(Boolean.FALSE);
    lbrResearchToUpdate.setIsReceipt(Boolean.FALSE);
    lbrResearchToUpdate.setIsIemkData(Boolean.FALSE);
    lbrResearchToUpdate.setIsMainExpert(Boolean.FALSE);
    lbrResearchToUpdate.setPerformedLpuName("");
    lbrResearchToUpdate.setIsRejected(Boolean.FALSE);
    lbrResearchToUpdate.setIsCompleteEarly(Boolean.FALSE);

    lbrResearchRepository.save(lbrResearchToUpdate);
  }

  /**
   * Извлечь закодированное заключение врача
   * @param encodedConclusion - данные заключения в виде строки base64 обернутой в тег <Data>
   *                               (например: <Data>some base 64</Data> )
   * @return - раскодированное заключение
   */
  private String extractEncodedConclusionFromRequest(String encodedConclusion) {
    final Matcher matcher = referralEndpointConfig.getWrappedConclusionPattern().matcher(encodedConclusion);
    if (matcher.find()) {
      return new String(Base64.decode(matcher.group(referralEndpointConfig.getGroup())));
    } else {
      return "";
    }
  }

  /**
   * Убрать html формат и проставить новую строку (в конце каждого html тега)
   * @param decodedConclusionAsHtmlString - декодированное заключение врача формата html (с тегами)
   * @return - заключение врача (без html тегов, новая строка)
   */
  private String ridOfHtmlTagsAndSetNewLine(String decodedConclusionAsHtmlString) {
    // Конец html тега - новая строка
    return Arrays.stream(decodedConclusionAsHtmlString.split("</"))
        // Избавиться от html тегов (полных и частичных)
        .map(raw -> raw.replaceAll("\\<[^>]*>", "").replaceAll(".+>", ""))
        .filter(StringUtils::hasText)
        // проставить разделить строки "для правильного отображения на фронте"
        .map(item -> item + System.lineSeparator()).collect(Collectors.joining());
  }

  /**
   * Создать Response с ошибками сохранения конкретных lbr_Research
   * @param savingErrorsMessage - ошибки сохранения
   * @return - Response
   */
  private Response createLbrResearchSavingErrorResponse(String savingErrorsMessage) {
    return Response.builder()
        .errorText("Не удалось создать новую запись в таблице lbr_Research для "
            + "следующих ServiceResult [idReferral, serviceId, patientId, serviceCode]: " + savingErrorsMessage)
        .build();
  }

  /**
   * DataHolder:
   *  - LbrResearch для обновления
   *  - данные конкретного ServiceResult (для обновления LbrResearch)
   *  - lbr_LaboratoryResearch.GUID
   */
  @Data
  @Builder
  private static class HolderLbrResearchToUpdateData {
    private LbrResearch lbrResearchToUpdate;
    private ServiceResult serviceResult;
    private String lbrLaboratoryResearchGuid;
  }
}
