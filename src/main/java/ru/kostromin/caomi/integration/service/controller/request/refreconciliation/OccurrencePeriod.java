package ru.kostromin.caomi.integration.service.controller.request.refreconciliation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.OffsetDateTimeSerializer;
import java.time.OffsetDateTime;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OccurrencePeriod {

  @NotNull(message = "occurencePeriod.start")
  @JsonProperty("start")
  @JsonSerialize(using = OffsetDateTimeSerializer.class)
  private OffsetDateTime start;

  @NotNull(message = "occurencePeriod.end")
  @JsonProperty("end")
  @JsonSerialize(using = OffsetDateTimeSerializer.class)
  private OffsetDateTime end;
}
