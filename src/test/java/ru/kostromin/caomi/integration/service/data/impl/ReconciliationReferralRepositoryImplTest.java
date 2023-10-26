package ru.kostromin.caomi.integration.service.data.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import ru.kostromin.caomi.integration.service.data.mapper.CustomBeanPropertyRowMapper;

class ReconciliationReferralRepositoryImplTest {

  @Mock
  private NamedParameterJdbcTemplate jdbcTemplate;

  private AutoCloseable mocks;

  private ReconciliationReferralRepositoryImpl referralRepositoryImpl;

  @BeforeEach
  void beforeEach(){
    mocks = MockitoAnnotations.openMocks(this);

    referralRepositoryImpl = new ReconciliationReferralRepositoryImpl(jdbcTemplate);
  }

  @Test
  void getReconciliationReferralsByLabResIdAndLimitAndOffset_success() {
    referralRepositoryImpl.getReconciliationReferralByLabResId("", 1);

    Mockito.verify(jdbcTemplate, Mockito.times(1))
        .queryForObject(Mockito.anyString(), Mockito.any(MapSqlParameterSource.class), Mockito.any(
            CustomBeanPropertyRowMapper.class));
  }

  @Test
  void getReconciliationReferralsByLabResIdAndLimitAndOffset_failure() {
    Mockito.doThrow(new EmptyResultDataAccessException(1))
        .when(jdbcTemplate)
            .queryForObject(Mockito.anyString(), Mockito.any(MapSqlParameterSource.class), Mockito.any(
            CustomBeanPropertyRowMapper.class));

    referralRepositoryImpl.getReconciliationReferralByLabResId("", 1);

    Mockito.verify(jdbcTemplate, Mockito.times(1))
        .queryForObject(Mockito.anyString(), Mockito.any(MapSqlParameterSource.class), Mockito.any(
            CustomBeanPropertyRowMapper.class));
  }
}