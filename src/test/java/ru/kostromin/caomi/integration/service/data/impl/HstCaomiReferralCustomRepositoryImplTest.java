package ru.kostromin.caomi.integration.service.data.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.kostromin.caomi.integration.service.data.dto.PatientDto;
import ru.kostromin.caomi.integration.service.data.dto.ReferralDto;
import ru.kostromin.caomi.integration.service.data.dto.ServiceRequestDto;

@ExtendWith(SpringExtension.class)
class HstCaomiReferralCustomRepositoryImplTest {

  @Mock
  private NamedParameterJdbcTemplate jdbcTemplate;

  private HstCaomiReferralCustomRepositoryImpl referralCustomRepositoryImpl;

  @BeforeEach
  void initReferralCustomRepositoryImpl(){
    referralCustomRepositoryImpl = new HstCaomiReferralCustomRepositoryImpl(jdbcTemplate);
  }

  @Test
  @DisplayName("Получение данных referral."
      + "Данные получены."
      + "ReferralDto возвращен.")
  void getReferralDataByLaboratoryResearchIdSuccess_referralDtoReturned(){
    // (1) prepare mocks:
    ReferralDto expectedData = ReferralDto.builder()
        .moOid("moId")
        .build();
    Mockito.when(jdbcTemplate.queryForObject(
            Mockito.anyString(),
            Mockito.any(MapSqlParameterSource.class),
            Mockito.any(BeanPropertyRowMapper.class)))
        .thenReturn(expectedData);

    // (2) start test:
    ReferralDto actualData = referralCustomRepositoryImpl.getReferralDataByLaboratoryResearchId("", 1);

    // (3) check:
    Assertions.assertEquals(expectedData, actualData);
  }

  @Test
  @DisplayName("Получение данных referral."
      + "Данные не найдены."
      + "Пустой ReferralDto возвращен.")
  void getReferralDataByLaboratoryResearchIdNoDataFound_emptyReferralDtoReturned(){
    // (1) prepare mocks:
    Mockito.doThrow(new EmptyResultDataAccessException(1))
        .when(jdbcTemplate).queryForObject(
            Mockito.anyString(),
            Mockito.any(MapSqlParameterSource.class),
            Mockito.any(BeanPropertyRowMapper.class));

    // (2) start test:
    ReferralDto actualData = referralCustomRepositoryImpl.getReferralDataByLaboratoryResearchId("", 1);

    // (3) check:
    Assertions.assertEquals(new ReferralDto(), actualData);
  }

  @Test
  @DisplayName("Получение данных patient."
      + "Данные получены."
      + "PatientDto возвращен.")
  void getPatientDataByLaboratoryResearchIdSuccess_patientDtoReturned(){
    // (1) prepare mocks:
    PatientDto expectedData = PatientDto.builder()
        .birthDate("2012-01-01")
        .build();
    Mockito.when(jdbcTemplate.queryForObject(
            Mockito.anyString(),
            Mockito.any(MapSqlParameterSource.class),
            Mockito.any(BeanPropertyRowMapper.class)))
        .thenReturn(expectedData);

    // (2) start test:
    PatientDto actualData = referralCustomRepositoryImpl.getPatientDataByLaboratoryResearchId("", 1);

    // (3) check:
    Assertions.assertEquals(expectedData, actualData);
  }

  @Test
  @DisplayName("Получение данных patient."
      + "Данные не найдены."
      + "Пустой PatientDto возвращен.")
  void getPatientDataByLaboratoryResearchIdNoDataFound_emptyPatientDtoReturned(){
    // (1) prepare mocks:
    Mockito.doThrow(new EmptyResultDataAccessException(1))
        .when(jdbcTemplate).queryForObject(
            Mockito.anyString(),
            Mockito.any(MapSqlParameterSource.class),
            Mockito.any(BeanPropertyRowMapper.class));

    // (2) start test:
    PatientDto actualData = referralCustomRepositoryImpl.getPatientDataByLaboratoryResearchId("", 1);

    // (3) check:
    Assertions.assertEquals(new PatientDto(), actualData);
  }

  @Test
  @DisplayName("Получение данных serviceRequest."
      + "Данные получены."
      + "ServiceRequest возвращен.")
  void getServiceRequestDataByLaboratoryResearchIdSuccess_serviceRequestDtoReturned(){
    // (1) prepare mocks:
    ServiceRequestDto expectedData = ServiceRequestDto.builder()
        .reasonCode("lox")
        .build();
    Mockito.when(jdbcTemplate.queryForObject(
            Mockito.anyString(),
            Mockito.any(MapSqlParameterSource.class),
            Mockito.any(BeanPropertyRowMapper.class)))
        .thenReturn(expectedData);

    // (2) start test:
    ServiceRequestDto actualData = referralCustomRepositoryImpl.getServiceRequestDataByLaboratoryResearchId("", 1);

    // (3) check:
    Assertions.assertEquals(expectedData, actualData);
  }

  @Test
  @DisplayName("Получение данных serviceRequest."
      + "Данные не найдены."
      + "Пустой ServiceRequestDto возвращен.")
  void getServiceRequestDataByLaboratoryResearchIdNoDataFound_emptyServiceRequestDtoReturned(){
    // (1) prepare mocks:
    Mockito.doThrow(new EmptyResultDataAccessException(1))
        .when(jdbcTemplate).queryForObject(
            Mockito.anyString(),
            Mockito.any(MapSqlParameterSource.class),
            Mockito.any(BeanPropertyRowMapper.class));

    // (2) start test:
    ServiceRequestDto actualData = referralCustomRepositoryImpl.getServiceRequestDataByLaboratoryResearchId("", 1);

    // (3) check:
    Assertions.assertEquals(new ServiceRequestDto(), actualData);
  }

}