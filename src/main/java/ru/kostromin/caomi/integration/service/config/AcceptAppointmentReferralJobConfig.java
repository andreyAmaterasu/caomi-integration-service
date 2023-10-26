package ru.kostromin.caomi.integration.service.config;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Конфигурация для Job - acceptAppointmentReferralJob
 */
@Component
@Data
@ConfigurationProperties(prefix = "app.accept-appointment-referral-job")
@Validated
public class AcceptAppointmentReferralJobConfig {

  private DataQueries sql;

  @Data
  public static class DataQueries {

    private int limit;

    private int offset = 0;

    /**
     * SQL для поиска доступных слотов оборудования
     */
    @NotNull
    @NotEmpty
    private String equipmentSlotsSql;

    /**
     * SQL для поиска идентификатора места
     */
    @NotNull
    @NotEmpty
    private String idPlaceSql;
  }
}
