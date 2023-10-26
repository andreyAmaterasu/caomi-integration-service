package ru.kostromin.caomi.integration.service.service.job;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import ru.kostromin.caomi.integration.service.config.AcceptAppointmentReferralJobConfig;
import ru.kostromin.caomi.integration.service.config.AcceptAppointmentReferralJobConfig.DataQueries;
import ru.kostromin.caomi.integration.service.data.dto.EquipmentSlotsDto;
import ru.kostromin.caomi.integration.service.data.dto.PlaceDto;
import ru.kostromin.caomi.integration.service.data.entity.HltMkab;
import ru.kostromin.caomi.integration.service.data.entity.HstCaomiAcceptReferral;
import ru.kostromin.caomi.integration.service.data.repository.AcceptAppointmentReferralRepository;
import ru.kostromin.caomi.integration.service.data.repository.HltMkabRepository;
import ru.kostromin.caomi.integration.service.data.repository.HstCaomiAcceptReferralRepository;
import ru.kostromin.caomi.integration.service.feign.AppointmentFeignClient;
import ru.kostromin.caomi.integration.service.feign.request.appointment.AppointmentRequest;
import ru.kostromin.caomi.integration.service.feign.response.AppointmentResponse;

class AppointmentAcceptReferralServiceTest {

  private AutoCloseable mocks;

  @Mock
  private AcceptAppointmentReferralJobConfig config;
  @Mock
  private HstCaomiAcceptReferralRepository hstCaomiAcceptReferralRepository;
  @Mock
  private AcceptAppointmentReferralRepository appointmentReferralRepository;
  @Mock
  private HltMkabRepository hltMkabRepository;
  @Mock
  private AppointmentFeignClient appointmentFeignClient;

  private AppointmentAcceptReferralService service;

  @BeforeEach
  void beforeEach() {
    mocks = MockitoAnnotations.openMocks(this);

    config = new AcceptAppointmentReferralJobConfig();
    final DataQueries dataQueries = new DataQueries();
    dataQueries.setEquipmentSlotsSql("some sql");
    dataQueries.setIdPlaceSql("some other sql");
    config.setSql(dataQueries);

    service = new AppointmentAcceptReferralService(
        config, hstCaomiAcceptReferralRepository, appointmentReferralRepository,
        hltMkabRepository, appointmentFeignClient);
  }

  @Test
  @DisplayName("Рефералы с performerDeviceID = null, не были найдены."
      + "Запрос в Внешнюю Систему не отправлен")
  void whenReferralsWithEmptyPerformerDeviceIdNotFound_requestNotSent() {
    // (1) prepare mocks:
    Mockito.when(hstCaomiAcceptReferralRepository.findEntriesWithStatusIdOffsetAndLimit(
        Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt()
    )).thenReturn(List.of());

    // (2) start test:
    service.findAcceptReferralsAndSendToAppointment();

    // (3) check:
    Mockito.verify(hstCaomiAcceptReferralRepository, Mockito.times(1))
        .findEntriesWithStatusIdOffsetAndLimit(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt());

    Mockito.verify(appointmentReferralRepository, Mockito.times(0)).findEquipmentDataByLaboratoryResearchId(
        Mockito.anyString(), Mockito.anyInt()
    );
    Mockito.verify(hltMkabRepository, Mockito.times(0))
        .findById(Mockito.anyInt());
    Mockito.verify(hstCaomiAcceptReferralRepository, Mockito.times(0))
        .save(Mockito.any(HstCaomiAcceptReferral.class));
  }

  @Test
  @DisplayName("Рефералы с performerDeviceID = null, были найдены,"
      + "доступные слоты оборудования не были найдены."
      + "statusId = 2 у рефералов")
  void whenReferralsWithEmptyPerformerDeviceIdFoundButSlotsNotFound_referralStatusIdTwoUpdated() {
    // (1) prepare mocks:
    final HstCaomiAcceptReferral foundReferral = HstCaomiAcceptReferral.builder().build();
    Mockito.when(hstCaomiAcceptReferralRepository.findEntriesWithStatusIdOffsetAndLimit(
        Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt()
    )).thenReturn(List.of(foundReferral));

    Mockito.when(appointmentReferralRepository
            .findEquipmentDataByLaboratoryResearchId(Mockito.anyString(), Mockito.anyInt()))
        .thenReturn(null);

    // (2) start test:
    service.findAcceptReferralsAndSendToAppointment();

    // (3) check:
    Mockito.verify(hstCaomiAcceptReferralRepository, Mockito.times(1))
        .findEntriesWithStatusIdOffsetAndLimit(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt());
    final ArgumentCaptor<HstCaomiAcceptReferral> saveCaptor = ArgumentCaptor.forClass(HstCaomiAcceptReferral.class);
    Mockito.verify(hstCaomiAcceptReferralRepository, Mockito.times(1))
        .save(saveCaptor.capture());
    Assertions.assertEquals(foundReferral, saveCaptor.getValue());
    Assertions.assertEquals(2, saveCaptor.getValue().getStatusId());

    Mockito.verify(appointmentReferralRepository, Mockito.times(0))
        .findEquipmentDataByLaboratoryResearchId(
            Mockito.anyString(), Mockito.anyInt());
    Mockito.verify(hltMkabRepository, Mockito.times(0))
        .findById(Mockito.anyInt());
    Mockito.verify(appointmentReferralRepository, Mockito.times(0))
        .findIdPlaceByDoctorTimeTableId(Mockito.anyString(), Mockito.anyInt());
  }

  @Test
  @DisplayName("Рефералы с performerDeviceID = null, были найдены,"
      + "доступные слоты оборудования были найдены,"
      + "при отправке запроса в шину было выброшено исключение."
      + "Рефералы не обновлены.")
  void whenReferralsWithEmptyPerformerDeviceIdFoundAndSlotsNotFoundButFeignThrewUnexpectedException_referralNotUpdated() {
    // (1) prepare mocks:
    final HstCaomiAcceptReferral foundReferral = HstCaomiAcceptReferral.builder()
        .id(1)
        .lbrLaboratoryResearchId(100)
        .build();
    Mockito.when(hstCaomiAcceptReferralRepository.findEntriesWithStatusIdOffsetAndLimit(
        Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt()
    )).thenReturn(List.of(foundReferral));

    final EquipmentSlotsDto foundSlot = EquipmentSlotsDto.builder()
        .date(LocalDateTime.MAX)
        .beginTime(LocalDateTime.MIN)
        .externalScheduleId(1)
        .number("number")
        .uguid("uguid")
        .rfMkabID(2)
        .equipmentGuid(UUID.randomUUID().toString())
        .doctorTimeTableId(4)
        .build();
    Mockito.when(appointmentReferralRepository
            .findEquipmentDataByLaboratoryResearchId(Mockito.anyString(), Mockito.anyInt()))
        .thenReturn(foundSlot);

    final HltMkab foundMkab = HltMkab.builder()
        .birthDate(LocalDateTime.now())
        .rfKlSexId(1) // male
        .policyNumber("policyNumber")
        .name("name")
        .lastName("lastName")
        .patronymic("patronymic")
        .build();

    Mockito.when(hltMkabRepository.findById(Mockito.anyInt()))
        .thenReturn(Optional.of(foundMkab));

    final PlaceDto foundPlace = PlaceDto.builder()
        .idPlace("idPlace")
        .build();
    Mockito.when(appointmentReferralRepository.findIdPlaceByDoctorTimeTableId(
        Mockito.anyString(), Mockito.anyInt()
    )).thenReturn(foundPlace);

    Mockito.doThrow(new RuntimeException())
        .when(appointmentFeignClient).postAppointment(Mockito.any(AppointmentRequest.class));

    // (2) start test:
    service.findAcceptReferralsAndSendToAppointment();

    // (3) check:
    Mockito.verify(hstCaomiAcceptReferralRepository, Mockito.times(1))
        .findEntriesWithStatusIdOffsetAndLimit(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt());
    Mockito.verify(appointmentReferralRepository, Mockito.times(1)).findEquipmentDataByLaboratoryResearchId(
        Mockito.anyString(), Mockito.anyInt()
    );
    Mockito.verify(hltMkabRepository, Mockito.times(1))
        .findById(Mockito.anyInt());
    Mockito.verify(appointmentReferralRepository, Mockito.times(1))
            .findIdPlaceByDoctorTimeTableId(Mockito.anyString(), Mockito.anyInt());

    final ArgumentCaptor<AppointmentRequest> requestCaptor = ArgumentCaptor.forClass(AppointmentRequest.class);
    Mockito.verify(appointmentFeignClient, Mockito.times(1))
        .postAppointment(requestCaptor.capture());
    assertCreatedRequest(
        requestCaptor.getValue(),
        foundSlot,
        foundMkab,
        foundPlace.getIdPlace(),
        foundReferral);

    Mockito.verify(hstCaomiAcceptReferralRepository, Mockito.times(0))
        .save(Mockito.any(HstCaomiAcceptReferral.class));
  }

  @Test
  @DisplayName("Рефералы с performerDeviceID = null, были найдены,"
      + "доступные слоты оборудования были найдены,"
      + "запрос в шину отправлен, но тело ответа отсутсвует."
      + "Рефералы не обновлены.")
  void whenReferralsWithEmptyPerformerDeviceIdFoundAndSlotsNotFoundButResponseBodyEmpty_referralNotUpdated() {
    // (1) prepare mocks:
    final HstCaomiAcceptReferral foundReferral = HstCaomiAcceptReferral.builder()
        .id(1)
        .lbrLaboratoryResearchId(100)
        .build();
    Mockito.when(hstCaomiAcceptReferralRepository.findEntriesWithStatusIdOffsetAndLimit(
        Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt()
    )).thenReturn(List.of(foundReferral));

    final EquipmentSlotsDto foundSlot = EquipmentSlotsDto.builder()
        .date(LocalDateTime.MAX)
        .beginTime(LocalDateTime.MIN)
        .externalScheduleId(1)
        .number("number")
        .uguid("uguid")
        .rfMkabID(2)
        .equipmentGuid(UUID.randomUUID().toString())
        .doctorTimeTableId(4)
        .build();
    Mockito.when(appointmentReferralRepository
            .findEquipmentDataByLaboratoryResearchId(Mockito.anyString(), Mockito.anyInt()))
        .thenReturn(foundSlot);

    final HltMkab foundMkab = HltMkab.builder()
        .birthDate(LocalDateTime.now())
        .rfKlSexId(2) // female
        .policyNumber("policyNumber")
        .name("name")
        .lastName("lastName")
        .patronymic("patronymic")
        .build();
    Mockito.when(hltMkabRepository.findById(Mockito.anyInt()))
        .thenReturn(Optional.of(foundMkab));

    final PlaceDto foundPlace = PlaceDto.builder()
        .idPlace("idPlace")
        .build();
    Mockito.when(appointmentReferralRepository.findIdPlaceByDoctorTimeTableId(
        Mockito.anyString(), Mockito.anyInt()
    )).thenReturn(foundPlace);

    Mockito.when(appointmentFeignClient.postAppointment(Mockito.any(AppointmentRequest.class)))
        .thenReturn(ResponseEntity.of(Optional.empty()));

    // (2) start test:
    service.findAcceptReferralsAndSendToAppointment();

    // (3) check:
    Mockito.verify(hstCaomiAcceptReferralRepository, Mockito.times(1))
        .findEntriesWithStatusIdOffsetAndLimit(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt());
    Mockito.verify(appointmentReferralRepository, Mockito.times(1)).findEquipmentDataByLaboratoryResearchId(
        Mockito.anyString(), Mockito.anyInt()
    );
    Mockito.verify(hltMkabRepository, Mockito.times(1))
        .findById(Mockito.anyInt());
    Mockito.verify(appointmentReferralRepository, Mockito.times(1))
        .findIdPlaceByDoctorTimeTableId(Mockito.anyString(), Mockito.anyInt());

    final ArgumentCaptor<AppointmentRequest> requestCaptor = ArgumentCaptor.forClass(AppointmentRequest.class);
    Mockito.verify(appointmentFeignClient, Mockito.times(1))
        .postAppointment(requestCaptor.capture());
    assertCreatedRequest(
        requestCaptor.getValue(),
        foundSlot,
        foundMkab,
        foundPlace.getIdPlace(),
        foundReferral);

    Mockito.verify(hstCaomiAcceptReferralRepository, Mockito.times(0))
        .save(Mockito.any(HstCaomiAcceptReferral.class));
  }

  @Test
  @DisplayName("Рефералы с performerDeviceID = null, были найдены,"
      + "доступные слоты оборудования были найдены,"
      + "запрос в шину отправлен, ответ возвращен."
      + "Рефералы обновлены.")
  void whenReferralsWithEmptyPerformerDeviceIdFoundAndSlotsNotFoundAndResponseReturned_referralsStatusOneUpdated() {
    // (1) prepare mocks:
    final HstCaomiAcceptReferral foundReferral = HstCaomiAcceptReferral.builder()
        .id(1)
        .lbrLaboratoryResearchId(100)
        .build();
    Mockito.when(hstCaomiAcceptReferralRepository.findEntriesWithStatusIdOffsetAndLimit(
        Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt()
    )).thenReturn(List.of(foundReferral));

    final EquipmentSlotsDto foundSlot = EquipmentSlotsDto.builder()
        .date(LocalDateTime.MAX)
        .beginTime(LocalDateTime.MIN)
        .externalScheduleId(1)
        .number("number")
        .uguid("uguid")
        .rfMkabID(2)
        .equipmentGuid(UUID.randomUUID().toString())
        .doctorTimeTableId(4)
        .build();
    Mockito.when(appointmentReferralRepository
            .findEquipmentDataByLaboratoryResearchId(Mockito.anyString(), Mockito.anyInt()))
        .thenReturn(foundSlot);

    final HltMkab foundMkab = HltMkab.builder()
        .birthDate(LocalDateTime.now())
        .rfKlSexId(1) // male
        .policyNumber("policyNumber")
        .name("name")
        .lastName("lastName")
        .patronymic("patronymic")
        .build();
    Mockito.when(hltMkabRepository.findById(Mockito.anyInt()))
        .thenReturn(Optional.of(foundMkab));

    final PlaceDto foundPlace = PlaceDto.builder()
        .idPlace("idPlace")
        .build();
    Mockito.when(appointmentReferralRepository.findIdPlaceByDoctorTimeTableId(
        Mockito.anyString(), Mockito.anyInt()
    )).thenReturn(foundPlace);

    final AppointmentResponse expectedResponse = new AppointmentResponse();
    Mockito.when(appointmentFeignClient.postAppointment(Mockito.any(AppointmentRequest.class)))
        .thenReturn(ResponseEntity.of(Optional.of(expectedResponse)));

    // (2) start test:
    service.findAcceptReferralsAndSendToAppointment();

    // (3) check:
    Mockito.verify(hstCaomiAcceptReferralRepository, Mockito.times(1))
        .findEntriesWithStatusIdOffsetAndLimit(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt());
    Mockito.verify(appointmentReferralRepository, Mockito.times(1)).findEquipmentDataByLaboratoryResearchId(
        Mockito.anyString(), Mockito.anyInt()
    );
    Mockito.verify(hltMkabRepository, Mockito.times(1))
        .findById(Mockito.anyInt());
    Mockito.verify(appointmentReferralRepository, Mockito.times(1))
        .findIdPlaceByDoctorTimeTableId(Mockito.anyString(), Mockito.anyInt());

    final ArgumentCaptor<AppointmentRequest> requestCaptor = ArgumentCaptor.forClass(AppointmentRequest.class);
    Mockito.verify(appointmentFeignClient, Mockito.times(1))
        .postAppointment(requestCaptor.capture());
    assertCreatedRequest(
        requestCaptor.getValue(),
        foundSlot,
        foundMkab,
        foundPlace.getIdPlace(),
        foundReferral);

    final ArgumentCaptor<HstCaomiAcceptReferral> saveCaptor = ArgumentCaptor.forClass(HstCaomiAcceptReferral.class);
    Mockito.verify(hstCaomiAcceptReferralRepository, Mockito.times(1))
        .save(saveCaptor.capture());
    Assertions.assertEquals(foundReferral, saveCaptor.getValue());
    Assertions.assertEquals(1, saveCaptor.getValue().getStatusId());
    Assertions.assertEquals(foundSlot.getEquipmentGuid(), saveCaptor.getValue().getPerformerDeviceId());
  }

  private void assertCreatedRequest(
      AppointmentRequest createdRequest,
      EquipmentSlotsDto equipmentSlotsDto,
      HltMkab hltMkab,
      String idPlace,
      HstCaomiAcceptReferral caomiAcceptReferral) {
    Assertions.assertEquals(hltMkab.getPolicyNumber(),
        createdRequest.getPatient().getOms());
    Assertions.assertEquals(hltMkab.getBirthDate().toLocalDate(),
        createdRequest.getPatient().getBirthDate());
    final String gender = hltMkab.getRfKlSexId() == 1
        ? "male"
        : hltMkab.getRfKlSexId() == 2
            ? "female"
            : null;
    Assertions.assertEquals(gender,
        createdRequest.getPatient().getGender());
    Assertions.assertEquals(hltMkab.getName(),
        createdRequest.getPatient().getName());
    Assertions.assertEquals(hltMkab.getLastName(),
        createdRequest.getPatient().getSurname());
    Assertions.assertEquals(hltMkab.getPatronymic(),
        createdRequest.getPatient().getPatronymic());

    Assertions.assertEquals(equipmentSlotsDto.getDate().toLocalDate(),
        createdRequest.getDateSlot());
    Assertions.assertEquals(equipmentSlotsDto.getBeginTime(),
        createdRequest.getTimeBegin());
    Assertions.assertEquals(equipmentSlotsDto.getExternalScheduleId().toString(),
        createdRequest.getIdSchedule());
    Assertions.assertEquals(equipmentSlotsDto.getUguid(),
        createdRequest.getIdSlot());
    Assertions.assertEquals(equipmentSlotsDto.getNumber(),
        createdRequest.getReferralNum());

    Assertions.assertEquals(idPlace, createdRequest.getIdPlace());

    Assertions.assertEquals(caomiAcceptReferral.getCaomiId(),
        createdRequest.getId());
    Assertions.assertEquals(caomiAcceptReferral.getServiceCode(),
        createdRequest.getMedicalServiceId());
  }
}