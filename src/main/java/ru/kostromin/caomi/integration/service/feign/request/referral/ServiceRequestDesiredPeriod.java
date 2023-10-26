package ru.kostromin.caomi.integration.service.feign.request.referral;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServiceRequestDesiredPeriod {
  @JsonProperty("start")
  private OffsetDateTime start;

  @JsonProperty("end")
  private OffsetDateTime end;

}
