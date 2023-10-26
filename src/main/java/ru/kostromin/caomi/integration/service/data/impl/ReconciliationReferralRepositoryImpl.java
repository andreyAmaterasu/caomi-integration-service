package ru.kostromin.caomi.integration.service.data.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import ru.kostromin.caomi.integration.service.data.dto.ReconciliationDto;
import ru.kostromin.caomi.integration.service.data.mapper.CustomBeanPropertyRowMapper;
import ru.kostromin.caomi.integration.service.data.repository.ReconciliationReferralRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReconciliationReferralRepositoryImpl implements ReconciliationReferralRepository {

  private final static String LABORATORY_RESEARCH_ID = "laboratory_research_id";

  private final NamedParameterJdbcTemplate jdbcTemplate;

  @Override
  public ReconciliationDto getReconciliationReferralByLabResId(String query, Integer labResId) {
    try {
      return jdbcTemplate.queryForObject(
          query,
          new MapSqlParameterSource()
              .addValue(LABORATORY_RESEARCH_ID, labResId),
          new CustomBeanPropertyRowMapper<>(ReconciliationDto.class));
    } catch (EmptyResultDataAccessException e) {
      return null;
    }
  }
}
