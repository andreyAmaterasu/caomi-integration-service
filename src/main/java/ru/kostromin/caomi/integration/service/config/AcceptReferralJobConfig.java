package ru.kostromin.caomi.integration.service.config;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Конфигурация для Job - acceptReferralJob
 */
@Component
@Data
@ConfigurationProperties(prefix = "app.accept-referral-job")
@Validated
public class AcceptReferralJobConfig {

  private DataQueries sql;

  @Data
  public static class DataQueries {

    private int limit;

    private int offset = 0;

    @NotNull
    @NotEmpty
    private String referralSql;

    @NotNull
    @NotEmpty
    private String patientSql;

    @NotNull
    @NotEmpty
    private String serviceRequestSql;
  }

}
