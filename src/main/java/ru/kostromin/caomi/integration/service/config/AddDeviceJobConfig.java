package ru.kostromin.caomi.integration.service.config;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Конфигурация для Job - addDeviceJob
 */
@Component
@Data
@ConfigurationProperties(prefix = "app.add-device-job")
@Validated
public class AddDeviceJobConfig {
  private DataQueries sql;

  @Data
  public static class DataQueries {
    @NotNull
    @NotEmpty
    private String deviceSql;

    private int limit;

    private int offset = 0;
  }
}
