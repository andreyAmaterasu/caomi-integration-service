package ru.kostromin.caomi.integration.service.feign.request.referral;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CaomiReferralRequest {

  @JsonProperty(value = "moOid")
  private String moOid;

  @JsonProperty(value = "referralNumber")
  private String referralNumber;

  @JsonProperty(value = "practitioner")
  private Long practitioner;

  @JsonProperty(value = "practitionerRole")
  private Long practitionerRole;

  @JsonProperty(value = "patient")
  private Patient patient;

  @JsonProperty(value = "coverage")
  private Coverage coverage;

  @JsonProperty("serviceRequest")
  private List<ServiceRequest> serviceRequest;
}
