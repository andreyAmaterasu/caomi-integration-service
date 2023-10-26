package ru.kostromin.caomi.integration.service.data.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import ru.kostromin.caomi.integration.service.data.dto.EquipmentSlotsDto;
import ru.kostromin.caomi.integration.service.data.dto.PlaceDto;
import ru.kostromin.caomi.integration.service.data.mapper.CustomBeanPropertyRowMapper;
import ru.kostromin.caomi.integration.service.data.repository.AcceptAppointmentReferralRepository;

/**
 * Имплементация AcceptAppointmentReferralRepository
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AcceptAppointmentReferralRepositoryImpl implements
    AcceptAppointmentReferralRepository {

  private final static String LABORATORY_RESEARCH_ID = "laboratory_research_id";
  private final static String DOCTOR_TIME_TABLE_ID = "doctor_time_table_id";

  private final NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * Получить слот оборудования
   * @param query - sql запрос
   * @param labResId - lbr_LaboratoryResearch.LaboratoryResearchID
   * @return - слот оборудования
   */
  @Override
  public EquipmentSlotsDto findEquipmentDataByLaboratoryResearchId(String query, Integer labResId) {
    try {
      return jdbcTemplate.queryForObject(
          query,
          new MapSqlParameterSource()
              .addValue(LABORATORY_RESEARCH_ID, labResId),
          new CustomBeanPropertyRowMapper<>(EquipmentSlotsDto.class));
    } catch (EmptyResultDataAccessException e) {
      return null;
    }
  }

  /**
   * Получить данные места
   * @param query - sql запрос
   * @param doctorTimeTableId - hlt_DoctorTimeTable.DoctorTimeTableID
   * @return - данные места
   */
  @Override
  public PlaceDto findIdPlaceByDoctorTimeTableId(String query, Integer doctorTimeTableId) {
    try {
      return jdbcTemplate.queryForObject(
          query,
          new MapSqlParameterSource()
              .addValue(DOCTOR_TIME_TABLE_ID, doctorTimeTableId),
          new CustomBeanPropertyRowMapper<>(PlaceDto.class));
    } catch (EmptyResultDataAccessException e) {
      return new PlaceDto();
    }
  }
}
