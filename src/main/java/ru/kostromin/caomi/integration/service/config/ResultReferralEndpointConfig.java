package ru.kostromin.caomi.integration.service.config;

import java.util.regex.Pattern;
import javax.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Конфигурация для REST-endpoint - /api-v1/resultReferral
 */
@Component
@Data
@ConfigurationProperties(prefix = "app.endpoints.result-referral-post")
@Validated
public class ResultReferralEndpointConfig {
  /**
   * Паттерн для получения заключения из запроса (заключение лежит внутри тегов Data)
   * (по умолчанию - вся строка '.*')
   */
  @NotNull
  private String conclusionRegexPattern = ".*";

  /**
   * Группа регулярного выражения
   * (по умолчанию 0 - брать все регулярное выражение)
   */
  private Integer group = 0;

  /**
   * Тип контента, который брать для обработки заключения
   */
  private String contentType = "html";

  public Pattern getWrappedConclusionPattern() {
      return Pattern.compile(conclusionRegexPattern);
  }
}
