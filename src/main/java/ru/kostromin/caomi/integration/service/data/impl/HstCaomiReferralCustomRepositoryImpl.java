package ru.kostromin.caomi.integration.service.data.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import ru.kostromin.caomi.integration.service.data.dto.PatientDto;
import ru.kostromin.caomi.integration.service.data.dto.ReferralDto;
import ru.kostromin.caomi.integration.service.data.dto.ServiceRequestDto;
import ru.kostromin.caomi.integration.service.data.mapper.CustomBeanPropertyRowMapper;
import ru.kostromin.caomi.integration.service.data.repository.HstCaomiReferralCustomRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class HstCaomiReferralCustomRepositoryImpl implements HstCaomiReferralCustomRepository {

  private static final String LAB_RESEARCH_ID_NAME = "labResearchId";

  private final NamedParameterJdbcTemplate jdbcTemplate;

  @Override
  public ReferralDto getReferralDataByLaboratoryResearchId(String referralQuery, Integer labResId) {
    try{
      return jdbcTemplate.queryForObject(
          referralQuery,
          new MapSqlParameterSource()
              .addValue(LAB_RESEARCH_ID_NAME, labResId),
          new BeanPropertyRowMapper<>(ReferralDto.class)
      );
    } catch (EmptyResultDataAccessException e){
      return new ReferralDto();
    }
  }

  @Override
  public PatientDto getPatientDataByLaboratoryResearchId(String patientQuery, Integer labResId) {
    try{
      return jdbcTemplate.queryForObject(
          patientQuery,
          new MapSqlParameterSource()
              .addValue(LAB_RESEARCH_ID_NAME, labResId),
          new CustomBeanPropertyRowMapper<>(PatientDto.class)
      );
    } catch (EmptyResultDataAccessException e){
      return new PatientDto();
    }
  }

  @Override
  public ServiceRequestDto getServiceRequestDataByLaboratoryResearchId(String referralRequestQuery,
      Integer labResId) {
    try{
      return jdbcTemplate.queryForObject(
          referralRequestQuery,
          new MapSqlParameterSource()
              .addValue(LAB_RESEARCH_ID_NAME, labResId),
          new CustomBeanPropertyRowMapper<>(ServiceRequestDto.class)
      );
    } catch (EmptyResultDataAccessException e){
      return new ServiceRequestDto();
    }
  }
}
