package ru.kostromin.caomi.integration.service.feign.request.reconciliation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Модель диагностического оборудования
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Device {

  /**
   * Идентификатор диагностического оборудования в МИС
   */
  @JsonProperty("deviceMisId")
  private String deviceMisId;

  /**
   * Название аппарата
   */
  @JsonProperty("deviceName")
  private String deviceName;

  /**
   * Oid Медицинской организации, из справочника 1.2.643.5.1.13.13.11.1461, где установлено  диагностическое оборудовани
   */
  @JsonProperty("owner")
  private String owner;


}
