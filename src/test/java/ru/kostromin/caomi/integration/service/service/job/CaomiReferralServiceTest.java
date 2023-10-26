package ru.kostromin.caomi.integration.service.service.job;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.kostromin.caomi.integration.service.config.AcceptReferralJobConfig;
import ru.kostromin.caomi.integration.service.config.AcceptReferralJobConfig.DataQueries;
import ru.kostromin.caomi.integration.service.data.dto.PatientDto;
import ru.kostromin.caomi.integration.service.data.dto.ReferralDto;
import ru.kostromin.caomi.integration.service.data.dto.ServiceRequestDto;
import ru.kostromin.caomi.integration.service.data.entity.HstCaomiReferral;
import ru.kostromin.caomi.integration.service.data.repository.HstCaomiReferralCustomRepository;
import ru.kostromin.caomi.integration.service.data.repository.HstCaomiReferralRepository;
import ru.kostromin.caomi.integration.service.feign.CaomiFeignClient;
import ru.kostromin.caomi.integration.service.feign.request.referral.CaomiReferralRequest;
import ru.kostromin.caomi.integration.service.feign.request.referral.ServiceRequest;
import ru.kostromin.caomi.integration.service.feign.response.CaomiBasicResponse;

@ExtendWith(SpringExtension.class)
class CaomiReferralServiceTest {

  @Mock
  private AcceptReferralJobConfig config;
  @Mock
  private HstCaomiReferralRepository referralRepository;
  @Mock
  private HstCaomiReferralCustomRepository referralCustomRepository;
  @Mock
  private CaomiFeignClient feignClient;

  private CaomiReferralService service;

  @BeforeEach
  void initCaomiReferralService(){
    service = new CaomiReferralService(config, referralRepository,
        referralCustomRepository, feignClient);
  }

  @Test
  @DisplayName("Поиск и отправка данных о создании направления на инструментальное исследование."
      + "SQL запросы найдены в конфиге,"
      + "hst_caomiReferral не найдены для обработки."
      + "Ничего не отправлено.")
  void findAndSendReferralsSqlsFoundReferralsNotFound_nothingSent(){
    // (1) prepare mocks:
    AcceptReferralJobConfig.DataQueries dataQueries = new DataQueries();
    dataQueries.setReferralSql("sqlReferral");
    dataQueries.setPatientSql("patientSql");
    dataQueries.setServiceRequestSql("requestSql");
    Mockito.when(config.getSql()).thenReturn(dataQueries);

    Mockito.when(referralRepository.getReferralsToProcessByLimitAndOffset(Mockito.anyInt(), Mockito.anyInt()))
        .thenReturn(new ArrayList<>());

    // (2) start test:
    service.findAndSendReferrals();

    // (3) check:
    Mockito.verify(config, Mockito.times(2))
        .getSql();

    Mockito.verify(referralRepository, Mockito.times(1))
        .getReferralsToProcessByLimitAndOffset(Mockito.anyInt(), Mockito.anyInt());

    Mockito.verify(referralCustomRepository, Mockito.times(0))
        .getReferralDataByLaboratoryResearchId(Mockito.anyString(), Mockito.anyInt());

    Mockito.verify(referralCustomRepository, Mockito.times(0))
        .getPatientDataByLaboratoryResearchId(Mockito.anyString(), Mockito.anyInt());

    Mockito.verify(referralCustomRepository, Mockito.times(0))
        .getServiceRequestDataByLaboratoryResearchId(Mockito.anyString(), Mockito.anyInt());

    Mockito.verify(feignClient, Mockito.times(0))
        .acceptReferral(Mockito.any(CaomiReferralRequest.class));

    Mockito.verify(referralRepository, Mockito.times(0))
        .findById(Mockito.anyInt());

    Mockito.verify(referralRepository, Mockito.times(0))
        .save(Mockito.any(HstCaomiReferral.class));
  }

  @Test
  @DisplayName("Поиск и отправка данных о создании направления на инструментальное исследование."
      + "SQL запросы найдены в конфиге,"
      + "выброшено исключение при поиске hst_caomiReferral для обработки."
      + "Ничего не отправлено.")
  void findAndSendReferralsSqlsFoundReferralsThrewException_nothingSent(){
    // (1) prepare mocks:
    AcceptReferralJobConfig.DataQueries dataQueries = new DataQueries();
    dataQueries.setReferralSql("sqlReferral");
    dataQueries.setPatientSql("patientSql");
    dataQueries.setServiceRequestSql("requestSql");
    Mockito.when(config.getSql()).thenReturn(dataQueries);

    Mockito.doThrow(new RuntimeException())
        .when(referralRepository)
        .getReferralsToProcessByLimitAndOffset(Mockito.anyInt(), Mockito.anyInt());

    // (2) start test:
    service.findAndSendReferrals();

    // (3) check:
    Mockito.verify(config, Mockito.times(2))
        .getSql();

    Mockito.verify(referralRepository, Mockito.times(1))
        .getReferralsToProcessByLimitAndOffset(Mockito.anyInt(), Mockito.anyInt());

    Mockito.verify(referralCustomRepository, Mockito.times(0))
        .getReferralDataByLaboratoryResearchId(Mockito.anyString(), Mockito.anyInt());

    Mockito.verify(referralCustomRepository, Mockito.times(0))
        .getPatientDataByLaboratoryResearchId(Mockito.anyString(), Mockito.anyInt());

    Mockito.verify(referralCustomRepository, Mockito.times(0))
        .getServiceRequestDataByLaboratoryResearchId(Mockito.anyString(), Mockito.anyInt());

    Mockito.verify(feignClient, Mockito.times(0))
        .acceptReferral(Mockito.any(CaomiReferralRequest.class));

    Mockito.verify(referralRepository, Mockito.times(0))
        .findById(Mockito.anyInt());

    Mockito.verify(referralRepository, Mockito.times(0))
        .save(Mockito.any(HstCaomiReferral.class));
  }

  @Test
  @DisplayName(
      "Поиск и отправка данных о создании направления на инструментальное исследование."
          + "SQL запросы найдены в конфиге,"
          + "hst_caomiReferral найдены для обработки."
          + "выброшено исключение при поиске referral данных."
          + "Ничего не отправлено.")
  void findAndSendReferralsSqlsFoundReferralsFoundReferralDataThrewException_nothingSent() {
    // (1) prepare mocks:
    AcceptReferralJobConfig.DataQueries dataQueries = new DataQueries();
    dataQueries.setReferralSql("sqlReferral");
    dataQueries.setPatientSql("patientSql");
    dataQueries.setServiceRequestSql("requestSql");
    Mockito.when(config.getSql()).thenReturn(dataQueries);

    List<HstCaomiReferral> foundReferrals = new ArrayList<>();
    HstCaomiReferral foundReferral = new HstCaomiReferral();
    foundReferral.setCaomiReferralId(1);
    foundReferral.setLbrLaboratoryResearchId(2);
    foundReferrals.add(foundReferral);
    Mockito.when(referralRepository.getReferralsToProcessByLimitAndOffset(Mockito.anyInt(), Mockito.anyInt()))
        .thenReturn(foundReferrals);

    Mockito.doThrow(new RuntimeException())
        .when(referralCustomRepository)
        .getReferralDataByLaboratoryResearchId(Mockito.anyString(), Mockito.anyInt());

    // (2) start test:
    service.findAndSendReferrals();

    // (3) check:
    Mockito.verify(config, Mockito.times(3))
        .getSql();

    Mockito.verify(referralRepository, Mockito.times(1))
        .getReferralsToProcessByLimitAndOffset(Mockito.anyInt(), Mockito.anyInt());

    Mockito.verify(referralCustomRepository, Mockito.times(1))
        .getReferralDataByLaboratoryResearchId(Mockito.anyString(), Mockito.anyInt());

    Mockito.verify(referralCustomRepository, Mockito.times(0))
        .getPatientDataByLaboratoryResearchId(Mockito.anyString(), Mockito.anyInt());

    Mockito.verify(referralCustomRepository, Mockito.times(0))
        .getServiceRequestDataByLaboratoryResearchId(Mockito.anyString(), Mockito.anyInt());

    Mockito.verify(feignClient, Mockito.times(0))
        .acceptReferral(Mockito.any(CaomiReferralRequest.class));

    Mockito.verify(referralRepository, Mockito.times(0))
        .findById(Mockito.anyInt());

    Mockito.verify(referralRepository, Mockito.times(0))
        .save(Mockito.any(HstCaomiReferral.class));
  }

  @Test
  @DisplayName(
      "Поиск и отправка данных о создании направления на инструментальное исследование."
          + "SQL запросы найдены в конфиге,"
          + "hst_caomiReferral найдены для обработки."
          + "referral данные найдены,"
          + "выброшено исключение при поиске patient данных."
          + "Ничего не отправлено.")
  void findAndSendReferralsSqlsFoundReferralDataFoundPatientDataThrewException_nothingSent() {
    // (1) prepare mocks:
    AcceptReferralJobConfig.DataQueries dataQueries = new DataQueries();
    dataQueries.setReferralSql("sqlReferral");
    dataQueries.setPatientSql("patientSql");
    dataQueries.setServiceRequestSql("requestSql");
    Mockito.when(config.getSql()).thenReturn(dataQueries);

    List<HstCaomiReferral> foundReferrals = new ArrayList<>();
    HstCaomiReferral foundReferral = new HstCaomiReferral();
    foundReferral.setCaomiReferralId(1);
    foundReferral.setLbrLaboratoryResearchId(2);
    foundReferrals.add(foundReferral);
    Mockito.when(referralRepository.getReferralsToProcessByLimitAndOffset(Mockito.anyInt(), Mockito.anyInt()))
        .thenReturn(foundReferrals);

    Mockito.when(referralCustomRepository
        .getReferralDataByLaboratoryResearchId(Mockito.anyString(), Mockito.anyInt()))
        .thenReturn(new ReferralDto());

    Mockito.doThrow(new RuntimeException())
        .when(referralCustomRepository)
        .getPatientDataByLaboratoryResearchId(Mockito.anyString(), Mockito.anyInt());

    // (2) start test:
    service.findAndSendReferrals();

    // (3) check:
    Mockito.verify(config, Mockito.times(4))
        .getSql();

    Mockito.verify(referralRepository, Mockito.times(1))
        .getReferralsToProcessByLimitAndOffset(Mockito.anyInt(), Mockito.anyInt());

    Mockito.verify(referralCustomRepository, Mockito.times(1))
        .getReferralDataByLaboratoryResearchId(Mockito.anyString(), Mockito.anyInt());

    Mockito.verify(referralCustomRepository, Mockito.times(1))
        .getPatientDataByLaboratoryResearchId(Mockito.anyString(), Mockito.anyInt());

    Mockito.verify(referralCustomRepository, Mockito.times(0))
        .getServiceRequestDataByLaboratoryResearchId(Mockito.anyString(), Mockito.anyInt());

    Mockito.verify(feignClient, Mockito.times(0))
        .acceptReferral(Mockito.any(CaomiReferralRequest.class));

    Mockito.verify(referralRepository, Mockito.times(0))
        .findById(Mockito.anyInt());

    Mockito.verify(referralRepository, Mockito.times(0))
        .save(Mockito.any(HstCaomiReferral.class));
  }

  @Test
  @DisplayName(
      "Поиск и отправка данных о создании направления на инструментальное исследование."
          + "SQL запросы найдены в конфиге,"
          + "hst_caomiReferral найдены для обработки."
          + "referral данные найдены,"
          + "patient данные найдены,"
          + "выброшено исключение при поиске serviceRequest данных."
          + "Ничего не отправлено.")
  void findAndSendReferralsSqlsFoundReferralDataFoundPatientDataFoundServiceRequestDataThrewException_nothingSent() {
    // (1) prepare mocks:
    AcceptReferralJobConfig.DataQueries dataQueries = new DataQueries();
    dataQueries.setReferralSql("sqlReferral");
    dataQueries.setPatientSql("patientSql");
    dataQueries.setServiceRequestSql("requestSql");
    Mockito.when(config.getSql()).thenReturn(dataQueries);

    List<HstCaomiReferral> foundReferrals = new ArrayList<>();
    HstCaomiReferral foundReferral = new HstCaomiReferral();
    foundReferral.setCaomiReferralId(1);
    foundReferral.setLbrLaboratoryResearchId(2);
    foundReferrals.add(foundReferral);
    Mockito.when(referralRepository.getReferralsToProcessByLimitAndOffset(Mockito.anyInt(), Mockito.anyInt()))
        .thenReturn(foundReferrals);

    Mockito.when(referralCustomRepository
            .getReferralDataByLaboratoryResearchId(Mockito.anyString(), Mockito.anyInt()))
        .thenReturn(new ReferralDto());

    Mockito.when(referralCustomRepository
            .getPatientDataByLaboratoryResearchId(Mockito.anyString(), Mockito.anyInt()))
        .thenReturn(new PatientDto());

    Mockito.doThrow(new RuntimeException())
        .when(referralCustomRepository)
            .getServiceRequestDataByLaboratoryResearchId(Mockito.anyString(), Mockito.anyInt());

    // (2) start test:
    service.findAndSendReferrals();

    // (3) check:
    Mockito.verify(config, Mockito.times(5))
        .getSql();

    Mockito.verify(referralRepository, Mockito.times(1))
        .getReferralsToProcessByLimitAndOffset(Mockito.anyInt(), Mockito.anyInt());

    Mockito.verify(referralCustomRepository, Mockito.times(1))
        .getReferralDataByLaboratoryResearchId(Mockito.anyString(), Mockito.anyInt());

    Mockito.verify(referralCustomRepository, Mockito.times(1))
        .getPatientDataByLaboratoryResearchId(Mockito.anyString(), Mockito.anyInt());

    Mockito.verify(referralCustomRepository, Mockito.times(1))
        .getServiceRequestDataByLaboratoryResearchId(Mockito.anyString(), Mockito.anyInt());

    Mockito.verify(feignClient, Mockito.times(0))
        .acceptReferral(Mockito.any(CaomiReferralRequest.class));

    Mockito.verify(referralRepository, Mockito.times(0))
        .findById(Mockito.anyInt());

    Mockito.verify(referralRepository, Mockito.times(0))
        .save(Mockito.any(HstCaomiReferral.class));
  }

  @Test
  @DisplayName(
      "Поиск и отправка данных о создании направления на инструментальное исследование."
          + "SQL запросы найдены в конфиге,"
          + "hst_caomiReferral найдены для обработки."
          + "referral данные найдены,"
          + "patient данные найдены,"
          + "serviceRequest данные найдены."
          + "при формировании запроса в ЦАМИ выброшено исключение."
          + "Ничего не отправлено.")
  void findAndSendReferralsSqlsFoundAllDataFoundWhileMakingRequestExceptionIsThrown_nothingSent() {
    // (1) prepare mocks:
    AcceptReferralJobConfig.DataQueries dataQueries = new DataQueries();
    dataQueries.setReferralSql("sqlReferral");
    dataQueries.setPatientSql("patientSql");
    dataQueries.setServiceRequestSql("requestSql");
    Mockito.when(config.getSql()).thenReturn(dataQueries);

    List<HstCaomiReferral> foundReferrals = new ArrayList<>();
    HstCaomiReferral foundReferral = new HstCaomiReferral();
    foundReferral.setCaomiReferralId(1);
    foundReferral.setLbrLaboratoryResearchId(2);
    foundReferrals.add(foundReferral);
    Mockito.when(referralRepository.getReferralsToProcessByLimitAndOffset(Mockito.anyInt(), Mockito.anyInt()))
        .thenReturn(foundReferrals);

    Mockito.when(referralCustomRepository
            .getReferralDataByLaboratoryResearchId(Mockito.anyString(), Mockito.anyInt()))
        .thenReturn(new ReferralDto());

    PatientDto corruptedPatientData = new PatientDto();
    corruptedPatientData.setBirthDate("not a stgring-date for sure");
    Mockito.when(referralCustomRepository
            .getPatientDataByLaboratoryResearchId(Mockito.anyString(), Mockito.anyInt()))
        .thenReturn(corruptedPatientData);

    Mockito.when(referralCustomRepository
        .getServiceRequestDataByLaboratoryResearchId(Mockito.anyString(), Mockito.anyInt()))
        .thenReturn(new ServiceRequestDto());

    // (2) start test:
    service.findAndSendReferrals();

    // (3) check:
    Mockito.verify(config, Mockito.times(5))
        .getSql();

    Mockito.verify(referralRepository, Mockito.times(1))
        .getReferralsToProcessByLimitAndOffset(Mockito.anyInt(), Mockito.anyInt());

    Mockito.verify(referralCustomRepository, Mockito.times(1))
        .getReferralDataByLaboratoryResearchId(Mockito.anyString(), Mockito.anyInt());

    Mockito.verify(referralCustomRepository, Mockito.times(1))
        .getPatientDataByLaboratoryResearchId(Mockito.anyString(), Mockito.anyInt());

    Mockito.verify(referralCustomRepository, Mockito.times(1))
        .getServiceRequestDataByLaboratoryResearchId(Mockito.anyString(), Mockito.anyInt());

    Mockito.verify(feignClient, Mockito.times(0))
        .acceptReferral(Mockito.any(CaomiReferralRequest.class));

    Mockito.verify(referralRepository, Mockito.times(0))
        .findById(Mockito.anyInt());

    Mockito.verify(referralRepository, Mockito.times(0))
        .save(Mockito.any(HstCaomiReferral.class));
  }

  @Test
  @DisplayName(
      "Поиск и отправка данных о создании направления на инструментальное исследование."
          + "SQL запросы найдены в конфиге,"
          + "hst_caomiReferral найдены для обработки."
          + "referral данные найдены,"
          + "patient данные найдены,"
          + "serviceRequest данные найдены."
          + "запрос сформирован,"
          + "ответ от ЦАМИ не имеет тела."
          + "Ничего не сохранено.")
  void findAndSendReferralsSqlsFoundAllDataFoundRequestCreatedCaomiReturnedEmptyBody_nothingSaved() {
    // (1) prepare mocks:
    AcceptReferralJobConfig.DataQueries dataQueries = new DataQueries();
    dataQueries.setReferralSql("sqlReferral");
    dataQueries.setPatientSql("patientSql");
    dataQueries.setServiceRequestSql("requestSql");
    Mockito.when(config.getSql()).thenReturn(dataQueries);

    List<HstCaomiReferral> foundReferrals = new ArrayList<>();
    HstCaomiReferral foundReferral = new HstCaomiReferral();
    foundReferral.setCaomiReferralId(1);
    foundReferral.setLbrLaboratoryResearchId(2);
    foundReferrals.add(foundReferral);
    Mockito.when(referralRepository.getReferralsToProcessByLimitAndOffset(Mockito.anyInt(), Mockito.anyInt()))
        .thenReturn(foundReferrals);

    ReferralDto referralData = new ReferralDto();
    Mockito.when(referralCustomRepository
            .getReferralDataByLaboratoryResearchId(Mockito.anyString(), Mockito.anyInt()))
        .thenReturn(referralData);

    PatientDto patientData = new PatientDto();
    Mockito.when(referralCustomRepository
            .getPatientDataByLaboratoryResearchId(Mockito.anyString(), Mockito.anyInt()))
        .thenReturn(patientData);

    ServiceRequestDto serviceRequestData = new ServiceRequestDto();
    Mockito.when(referralCustomRepository
            .getServiceRequestDataByLaboratoryResearchId(Mockito.anyString(), Mockito.anyInt()))
        .thenReturn(serviceRequestData);

    Mockito.when(feignClient.acceptReferral(Mockito.any(CaomiReferralRequest.class)))
        .thenReturn(ResponseEntity.of(Optional.empty()));

    // (2) start test:
    service.findAndSendReferrals();

    // (3) check:
    Mockito.verify(config, Mockito.times(5))
        .getSql();

    Mockito.verify(referralRepository, Mockito.times(1))
        .getReferralsToProcessByLimitAndOffset(Mockito.anyInt(), Mockito.anyInt());

    Mockito.verify(referralCustomRepository, Mockito.times(1))
        .getReferralDataByLaboratoryResearchId(Mockito.anyString(), Mockito.anyInt());

    Mockito.verify(referralCustomRepository, Mockito.times(1))
        .getPatientDataByLaboratoryResearchId(Mockito.anyString(), Mockito.anyInt());

    Mockito.verify(referralCustomRepository, Mockito.times(1))
        .getServiceRequestDataByLaboratoryResearchId(Mockito.anyString(), Mockito.anyInt());

    Mockito.verify(feignClient, Mockito.times(1))
        .acceptReferral(Mockito.any(CaomiReferralRequest.class));

    Mockito.verify(referralRepository, Mockito.times(0))
        .findById(Mockito.anyInt());

    Mockito.verify(referralRepository, Mockito.times(0))
        .save(Mockito.any(HstCaomiReferral.class));
  }

  @Test
  @DisplayName(
      "Поиск и отправка данных о создании направления на инструментальное исследование."
          + "SQL запросы найдены в конфиге,"
          + "hst_caomiReferral найдены для обработки."
          + "referral данные найдены,"
          + "patient данные найдены,"
          + "serviceRequest данные найдены."
          + "запрос сформирован,"
          + "во время отправки/получения ответа от ЦАМИ выброшено исключение,"
          + "запись hst_caomiReferral найдена."
          + "hst_caomiReferral обновлена.")
  void findAndSendReferralsSqlsFoundAllDataFoundRequestCreatedCaomiThrewException_caomiReferralUpdated() {
    // (1) prepare mocks:
    AcceptReferralJobConfig.DataQueries dataQueries = new DataQueries();
    dataQueries.setReferralSql("sqlReferral");
    dataQueries.setPatientSql("patientSql");
    dataQueries.setServiceRequestSql("requestSql");
    Mockito.when(config.getSql()).thenReturn(dataQueries);

    List<HstCaomiReferral> foundReferrals = new ArrayList<>();
    HstCaomiReferral foundReferral = new HstCaomiReferral();
    foundReferral.setCaomiReferralId(1);
    foundReferral.setLbrLaboratoryResearchId(2);
    foundReferrals.add(foundReferral);
    Mockito.when(referralRepository.getReferralsToProcessByLimitAndOffset(Mockito.anyInt(), Mockito.anyInt()))
        .thenReturn(foundReferrals);

    ReferralDto referralData = new ReferralDto();
    Mockito.when(referralCustomRepository
            .getReferralDataByLaboratoryResearchId(Mockito.anyString(), Mockito.anyInt()))
        .thenReturn(referralData);

    PatientDto patientData = new PatientDto();
    Mockito.when(referralCustomRepository
            .getPatientDataByLaboratoryResearchId(Mockito.anyString(), Mockito.anyInt()))
        .thenReturn(patientData);

    ServiceRequestDto serviceRequestData = new ServiceRequestDto();
    Mockito.when(referralCustomRepository
            .getServiceRequestDataByLaboratoryResearchId(Mockito.anyString(), Mockito.anyInt()))
        .thenReturn(serviceRequestData);

    RuntimeException caomiException = new RuntimeException("message");
    Mockito.doThrow(caomiException)
        .when(feignClient)
        .acceptReferral(Mockito.any(CaomiReferralRequest.class));

    HstCaomiReferral foundCaomiReferral = new HstCaomiReferral();
    Mockito.when(referralRepository.findById(Mockito.anyInt()))
        .thenReturn(Optional.of(foundCaomiReferral));

    // (2) start test:
    service.findAndSendReferrals();

    // (3) check:
    Mockito.verify(config, Mockito.times(5))
        .getSql();

    Mockito.verify(referralRepository, Mockito.times(1))
        .getReferralsToProcessByLimitAndOffset(Mockito.anyInt(), Mockito.anyInt());

    Mockito.verify(referralCustomRepository, Mockito.times(1))
        .getReferralDataByLaboratoryResearchId(Mockito.anyString(), Mockito.anyInt());

    Mockito.verify(referralCustomRepository, Mockito.times(1))
        .getPatientDataByLaboratoryResearchId(Mockito.anyString(), Mockito.anyInt());

    Mockito.verify(referralCustomRepository, Mockito.times(1))
        .getServiceRequestDataByLaboratoryResearchId(Mockito.anyString(), Mockito.anyInt());

    Mockito.verify(feignClient, Mockito.times(1))
        .acceptReferral(Mockito.any(CaomiReferralRequest.class));

    Mockito.verify(referralRepository, Mockito.times(1))
        .findById(Mockito.anyInt());

    ArgumentCaptor<HstCaomiReferral> caomiReferralCaptor = ArgumentCaptor.forClass(HstCaomiReferral.class);
    Mockito.verify(referralRepository, Mockito.times(1))
        .save(caomiReferralCaptor.capture());
    HstCaomiReferral actualSaved = caomiReferralCaptor.getValue();
    Assertions.assertEquals(0, actualSaved.getHttpStatus());
    Assertions.assertEquals("", actualSaved.getErrorCode());
    Assertions.assertEquals(caomiException.toString(), actualSaved.getErrorText());
    Assertions.assertNotNull(actualSaved.getDateStatus());
  }

  @Test
  @DisplayName(
      "Поиск и отправка данных о создании направления на инструментальное исследование."
          + "SQL запросы найдены в конфиге,"
          + "hst_caomiReferral найдены для обработки."
          + "referral данные найдены,"
          + "patient данные найдены,"
          + "serviceRequest данные найдены."
          + "запрос сформирован,"
          + "цами вернуло ответ,"
          + "запись hst_caomiReferral найдена."
          + "hst_caomiReferral обновлена.")
  void findAndSendReferralsSqlsFoundAllDataFoundRequestCreatedCaomiReturnedResponse_caomiReferralUpdated() {
    // (1) prepare mocks:
    AcceptReferralJobConfig.DataQueries dataQueries = new DataQueries();
    dataQueries.setReferralSql("sqlReferral");
    dataQueries.setPatientSql("patientSql");
    dataQueries.setServiceRequestSql("requestSql");
    Mockito.when(config.getSql()).thenReturn(dataQueries);

    List<HstCaomiReferral> foundReferrals = new ArrayList<>();
    HstCaomiReferral foundReferral = new HstCaomiReferral();
    foundReferral.setCaomiReferralId(1);
    foundReferral.setLbrLaboratoryResearchId(2);
    foundReferrals.add(foundReferral);
    Mockito.when(referralRepository.getReferralsToProcessByLimitAndOffset(Mockito.anyInt(), Mockito.anyInt()))
        .thenReturn(foundReferrals);

    ReferralDto referralData = getFilledReferralDto();
    Mockito.when(referralCustomRepository
            .getReferralDataByLaboratoryResearchId(Mockito.anyString(), Mockito.anyInt()))
        .thenReturn(referralData);

    PatientDto patientData = getFilledPatientDto();
    Mockito.when(referralCustomRepository
            .getPatientDataByLaboratoryResearchId(Mockito.anyString(), Mockito.anyInt()))
        .thenReturn(patientData);

    ServiceRequestDto serviceRequestData = getFilledServiceRequestDto();
    Mockito.when(referralCustomRepository
            .getServiceRequestDataByLaboratoryResearchId(Mockito.anyString(), Mockito.anyInt()))
        .thenReturn(serviceRequestData);

    CaomiBasicResponse caomiResponse = new CaomiBasicResponse();
    caomiResponse.setIdReferral("idReferral");
    caomiResponse.setErrorCode("errorCode");
    caomiResponse.setErrorText("errorText");
    ResponseEntity<CaomiBasicResponse> respEnt = ResponseEntity.of(Optional.of(caomiResponse));
    Mockito.when(feignClient.acceptReferral(Mockito.any(CaomiReferralRequest.class)))
        .thenReturn(respEnt);

    HstCaomiReferral foundCaomiReferral = new HstCaomiReferral();
    Mockito.when(referralRepository.findById(Mockito.anyInt()))
        .thenReturn(Optional.of(foundCaomiReferral));

    // (2) start test:
    service.findAndSendReferrals();

    // (3) check:
    Mockito.verify(config, Mockito.times(5))
        .getSql();

    Mockito.verify(referralRepository, Mockito.times(1))
        .getReferralsToProcessByLimitAndOffset(Mockito.anyInt(), Mockito.anyInt());

    Mockito.verify(referralCustomRepository, Mockito.times(1))
        .getReferralDataByLaboratoryResearchId(Mockito.anyString(), Mockito.anyInt());

    Mockito.verify(referralCustomRepository, Mockito.times(1))
        .getPatientDataByLaboratoryResearchId(Mockito.anyString(), Mockito.anyInt());

    Mockito.verify(referralCustomRepository, Mockito.times(1))
        .getServiceRequestDataByLaboratoryResearchId(Mockito.anyString(), Mockito.anyInt());

    ArgumentCaptor<CaomiReferralRequest> requestCaptor = ArgumentCaptor.forClass(CaomiReferralRequest.class);
    Mockito.verify(feignClient, Mockito.times(1))
        .acceptReferral(requestCaptor.capture());
    CaomiReferralRequest actualRequest = requestCaptor.getValue();
    Assertions.assertEquals(referralData.getMoOid(), actualRequest.getMoOid());
    Assertions.assertEquals(referralData.getReferralNumber(), actualRequest.getReferralNumber());
    Assertions.assertEquals(Long.valueOf(referralData.getPractitioner()), actualRequest.getPractitioner());
    Assertions.assertEquals(referralData.getPractitionerRole(), actualRequest.getPractitionerRole());
    Assertions.assertEquals(patientData.getId(), actualRequest.getPatient().getPatientId());
    Assertions.assertEquals(Long.valueOf(patientData.getGender()), actualRequest.getPatient().getGender());
    Assertions.assertEquals(LocalDate.parse(patientData.getBirthDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd")),
        actualRequest.getPatient().getBirthDate());
    Assertions.assertEquals(patientData.getGeneralPractitionerMoOid(),
        actualRequest.getPatient().getGeneralPractitionerMoOid());
    Assertions.assertEquals(patientData.getPolicyTypeCode(), actualRequest.getCoverage().getPolicyTypeCode());
    Assertions.assertEquals(patientData.getPolicyNumber(), actualRequest.getCoverage().getPolicyNumber());
    Assertions.assertEquals(patientData.getPolicyStatus() ? 1L : 0L, actualRequest.getCoverage().getPolicyStatus());
    Assertions.assertEquals(patientData.getValidityPeriodStart().toLocalDate(),
        actualRequest.getCoverage().getValidityPeriod().getStart());
    Assertions.assertEquals(patientData.getValidityPeriodEnd().toLocalDate(),
        actualRequest.getCoverage().getValidityPeriod().getEnd());
    Assertions.assertEquals(patientData.getMedInsuranceOrgCode(),
        actualRequest.getCoverage().getMedicalInsuranceOrganizationCode());
    ServiceRequest serviceRequest = actualRequest.getServiceRequest().get(0);
    Assertions.assertEquals(serviceRequestData.getId(), serviceRequest.getServiceId());
    Assertions.assertEquals(serviceRequestData.getStatusCode(), serviceRequest.getServiceStatusCode());
    Assertions.assertEquals(Long.valueOf(serviceRequestData.getIntentCode()),
        serviceRequest.getServiceIntentCode());
    Assertions.assertEquals(serviceRequestData.getPriorityCode(), serviceRequest.getServicePriorityCode());
    Assertions.assertEquals(serviceRequestData.getCode(), serviceRequest.getServiceCode());
    Assertions.assertEquals(serviceRequestData.getAuthOnDate().atZone(ZoneId.systemDefault()).toOffsetDateTime(),
        serviceRequest.getAuthoredOn());
    Assertions.assertEquals(serviceRequestData.getDesiredPeriodStart().atZone(ZoneId.systemDefault()).toOffsetDateTime(),
        serviceRequest.getDesiredPeriod().getStart());
    Assertions.assertEquals(serviceRequestData.getDesiredPeriodEnd().atZone(ZoneId.systemDefault()).toOffsetDateTime(),
        serviceRequest.getDesiredPeriod().getEnd());
    Assertions.assertEquals(Long.valueOf(serviceRequestData.getPerformerTypeCode()),
        serviceRequest.getPerformerTypeCode());
    Assertions.assertEquals(serviceRequestData.getPerformerDeviceId(),
        serviceRequest.getPerformerDeviceId());
    Assertions.assertEquals(serviceRequestData.getPerformerMoOid(),
        serviceRequest.getPerformerMoOid());
    Assertions.assertEquals(serviceRequestData.getReasonCode(),
        serviceRequest.getReasonCode());

    Mockito.verify(referralRepository, Mockito.times(1))
        .findById(Mockito.anyInt());

    ArgumentCaptor<HstCaomiReferral> caomiReferralCaptor = ArgumentCaptor.forClass(HstCaomiReferral.class);
    Mockito.verify(referralRepository, Mockito.times(1))
        .save(caomiReferralCaptor.capture());
    HstCaomiReferral actualSaved = caomiReferralCaptor.getValue();
    Assertions.assertEquals(caomiResponse.getIdReferral(), actualSaved.getCaomiId());
    Assertions.assertEquals(caomiResponse.getErrorCode(), actualSaved.getErrorCode());
    Assertions.assertEquals(caomiResponse.getErrorText(), actualSaved.getErrorText());
    Assertions.assertNotNull(actualSaved.getDateStatus());
    Assertions.assertEquals(respEnt.getStatusCodeValue(), actualSaved.getHttpStatus());
    Assertions.assertEquals(1, actualSaved.getStatusId());
  }

  private ReferralDto getFilledReferralDto(){
    return ReferralDto.builder()
        .moOid("moOid")
        .referralNumber("refNum")
        .practitioner("1")
        .practitionerRole(2L)
        .build();
  }

  private PatientDto getFilledPatientDto(){
    return PatientDto.builder()
        .id("id")
        .gender("1")
        .birthDate("2012-01-01")
        .generalPractitionerMoOid("genPrMoOid")
        .policyTypeCode(2L)
        .policyNumber("polNum")
        .policyStatus(Boolean.TRUE)
        .validityPeriodStart(LocalDateTime.of(LocalDate.parse("1991-12-31"), LocalTime.MIDNIGHT))
        .validityPeriodEnd(LocalDateTime.of(LocalDate.parse("2022-02-24"), LocalTime.MIN))
        .medInsuranceOrgCode("medInsOrgCode")
        .build();
  }

  private ServiceRequestDto getFilledServiceRequestDto(){
    return ServiceRequestDto.builder()
        .id("id")
        .statusCode(1L)
        .intentCode("2")
        .priorityCode(3L)
        .code("code")
        .authOnDate(LocalDateTime.now())
        .desiredPeriodStart(LocalDateTime.of(LocalDate.parse("2011-01-01"), LocalTime.MIDNIGHT))
        .desiredPeriodEnd(LocalDateTime.of(LocalDate.parse("2012-01-01"), LocalTime.MIDNIGHT))
        .performerTypeCode("4")
        .performerDeviceId("devId")
        .performerMoOid("moId")
        .reasonCode("reasonCode")
        .build();
  }
}