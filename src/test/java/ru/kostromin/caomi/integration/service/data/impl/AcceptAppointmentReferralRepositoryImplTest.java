package ru.kostromin.caomi.integration.service.data.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import ru.kostromin.caomi.integration.service.data.dto.EquipmentSlotsDto;
import ru.kostromin.caomi.integration.service.data.dto.PlaceDto;

class AcceptAppointmentReferralRepositoryImplTest {

  private AutoCloseable mock;

  @Mock
  private NamedParameterJdbcTemplate jdbcTemplate;

  private AcceptAppointmentReferralRepositoryImpl repositoryImpl;

  @BeforeEach
  void beforeEach() {
    mock = MockitoAnnotations.openMocks(this);
    repositoryImpl = new AcceptAppointmentReferralRepositoryImpl(jdbcTemplate);
  }

  @Test
  @DisplayName("Получить слоты оборудования."
      + "Слоты получены")
  void findEquipmentDataByLaboratoryResearchId() {
    // (1) prepare mocks:
    final EquipmentSlotsDto expectedSlot = new EquipmentSlotsDto();
    Mockito.when(jdbcTemplate.queryForObject(
        Mockito.anyString(),
        Mockito.any(MapSqlParameterSource.class),
        Mockito.any(BeanPropertyRowMapper.class)
    )).thenReturn(expectedSlot);

    // (2) start test:
    final EquipmentSlotsDto result = repositoryImpl.findEquipmentDataByLaboratoryResearchId("", 1);

    // (3) check:
    Assertions.assertEquals(expectedSlot, result);
  }

  @Test
  @DisplayName("Получить слоты оборудования."
      + "Слоты не найдены")
  void findEquipmentDataByLaboratoryResearchId_empty() {
    // (1) prepare mocks:
    Mockito.doThrow(new EmptyResultDataAccessException(1))
        .when(jdbcTemplate).queryForObject(
            Mockito.anyString(),
            Mockito.any(MapSqlParameterSource.class),
            Mockito.any(BeanPropertyRowMapper.class));

    // (2) start test:
    final EquipmentSlotsDto result = repositoryImpl.findEquipmentDataByLaboratoryResearchId("", 1);

    // (3) check:
    Assertions.assertNull(result);
  }

  @Test
  @DisplayName("Получить данные места."
      + "Данные найдены")
  void findIdPlaceByDoctorTimeTableId() {
    // (1) prepare mocks:
    final PlaceDto expectedPlaceDto = PlaceDto.builder()
        .idPlace("idPlace")
        .build();
    Mockito.when(jdbcTemplate.queryForObject(
        Mockito.anyString(),
        Mockito.any(MapSqlParameterSource.class),
        Mockito.any(BeanPropertyRowMapper.class)
    )).thenReturn(expectedPlaceDto);

    // (2) start test:
    final PlaceDto result = repositoryImpl.findIdPlaceByDoctorTimeTableId("",1);

    // (3) check:
    Assertions.assertEquals(expectedPlaceDto, result);
  }

  @Test
  @DisplayName("Получить данные места."
      + "Данные не найдены")
  void findIdPlaceByDoctorTimeTableId_empty() {
    // (1) prepare mocks:
    Mockito.doThrow(new EmptyResultDataAccessException(1))
        .when(jdbcTemplate).queryForObject(
            Mockito.anyString(),
            Mockito.any(MapSqlParameterSource.class),
            Mockito.any(BeanPropertyRowMapper.class));

    // (2) start test:
    final PlaceDto result = repositoryImpl.findIdPlaceByDoctorTimeTableId("", 1);

    // (3) check:
    Assertions.assertNull(result.getIdPlace());
  }
}