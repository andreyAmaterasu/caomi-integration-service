package ru.kostromin.caomi.integration.service.controller.request.refaccept;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(of = {"start", "end"})
public class ServiceRequestDesiredPeriod {

  @JsonProperty("start")
  @NotNull(message = "start")
  private OffsetDateTime start;

  @JsonProperty("end")
  private OffsetDateTime end;
}
