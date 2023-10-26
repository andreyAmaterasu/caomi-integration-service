package ru.kostromin.caomi.integration.service.controller.request.refaccept;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
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
@EqualsAndHashCode(of = {"serviceId", "serviceStatusCode", "serviceIntentCode",
    "servicePriorityCode",
    "serviceCode", "authoredOn", "desiredPeriod", "performerTypeCode", "performerDeviceId",
    "performerMoOid", "reasonCode"})
public class ServiceRequest {

  @NotBlank(message = "serviceId")
  @JsonProperty("serviceId")
  private String serviceId;

  @NotNull(message = "serviceStatusCode")
  @JsonProperty("serviceStatusCode")
  private Long serviceStatusCode;

  @NotNull(message = "serviceIntentCode")
  @JsonProperty("serviceIntentCode")
  private Long serviceIntentCode;

  @JsonProperty("servicePriorityCode")
  private Long servicePriorityCode;

  @NotNull(message = "serviceCode")
  @JsonProperty("serviceCode")
  private String serviceCode;

  @JsonProperty("authoredOn")
  private OffsetDateTime authoredOn;

  @Valid
  @NotNull(message = "desiredPeriod")
  @JsonProperty("desiredPeriod")
  private ServiceRequestDesiredPeriod desiredPeriod;

  @NotNull(message = "performerTypeCode")
  @JsonProperty("performerTypeCode")
  private Long performerTypeCode;

  @JsonProperty("performerDeviceId")
  private String performerDeviceId;

  @NotBlank(message = "performerMoOid")
  @JsonProperty("performerMoOid")
  private String performerMoOid;

  @NotBlank(message = "reasonCode")
  @JsonProperty("reasonCode")
  private String reasonCode;
}
