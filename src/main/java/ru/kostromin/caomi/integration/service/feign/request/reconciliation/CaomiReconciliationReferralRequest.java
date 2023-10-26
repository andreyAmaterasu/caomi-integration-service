package ru.kostromin.caomi.integration.service.feign.request.reconciliation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Модель данных о результате согласования
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CaomiReconciliationReferralRequest {

  /**
   * Идентификатор направления в Шине
   */
  @JsonProperty("idReferral")
  private String idReferral;

  /**
   * Направление согласовано (да/нет)
   */
  @JsonProperty("agreedReferral")
  private Boolean agreedReferral;

  /**
   * Причина отказа (для несогласованных направлений)
   */
  @JsonProperty("rejectionReason")
  private String rejectionReason;

  @JsonProperty("occurencePeriod")
  private OccurrencePeriod ocurrencePeriod;

  @JsonProperty("device")
  private Device device;
}
