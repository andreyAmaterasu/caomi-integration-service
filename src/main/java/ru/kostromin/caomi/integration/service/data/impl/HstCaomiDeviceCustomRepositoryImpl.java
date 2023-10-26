package ru.kostromin.caomi.integration.service.data.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import ru.kostromin.caomi.integration.service.data.dto.DeviceDto;
import ru.kostromin.caomi.integration.service.data.repository.HstCaomiDeviceCustomRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class HstCaomiDeviceCustomRepositoryImpl implements HstCaomiDeviceCustomRepository {

  private final NamedParameterJdbcTemplate jdbcTemplate;

  @Override
  public List<DeviceDto> getDeviceDataByLimitAndOffset(String deviceQuery, int offset, int limit) {
    limit = limit != 0 ? limit : Integer.MAX_VALUE;
    return jdbcTemplate.query(
        deviceQuery,
        new MapSqlParameterSource()
            .addValue("offset", offset)
            .addValue("limit", limit),
        new BeanPropertyRowMapper<>(DeviceDto.class));
  }
}
