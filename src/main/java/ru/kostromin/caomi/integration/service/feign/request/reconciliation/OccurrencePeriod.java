package ru.kostromin.caomi.integration.service.feign.request.reconciliation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.OffsetDateTimeSerializer;
import java.time.OffsetDateTime;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Дата и время, когда должна произойти запрошенная услуга
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OccurrencePeriod {

  /**
   * Дата начала
   */
  @JsonProperty("start")
  @JsonSerialize(using = OffsetDateTimeSerializer.class)
  private OffsetDateTime start;

  /**
   * Дата окончания
   */
  @JsonProperty("end")
  @JsonSerialize(using = OffsetDateTimeSerializer.class)
  private OffsetDateTime end;


}
