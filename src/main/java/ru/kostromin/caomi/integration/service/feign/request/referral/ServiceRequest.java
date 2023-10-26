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
public class ServiceRequest {

  @JsonProperty("serviceId")
  private String serviceId;

  @JsonProperty("serviceStatusCode")
  private Long serviceStatusCode;

  @JsonProperty("serviceIntentCode")
  private Long serviceIntentCode;

  @JsonProperty("servicePriorityCode")
  private Long servicePriorityCode;

  @JsonProperty("serviceCode")
  private String serviceCode;

  @JsonProperty("authoredOn")
  private OffsetDateTime authoredOn;

  @JsonProperty("desiredPeriod")
  private ServiceRequestDesiredPeriod desiredPeriod;

  @JsonProperty("performerTypeCode")
  private Long performerTypeCode;

  @JsonProperty("performerDeviceId")
  private String performerDeviceId;

  @JsonProperty("performerMoOid")
  private String performerMoOid;

  @JsonProperty("reasonCode")
  private String reasonCode;


}
