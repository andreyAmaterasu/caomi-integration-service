package ru.kostromin.caomi.integration.service.feign.request.referral;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coverage {

  @JsonProperty("policyTypeCode")
  private Long policyTypeCode;

  @JsonProperty("policyNumber")
  private String policyNumber;

  @JsonProperty("policyStatus")
  private Long policyStatus;

  @JsonProperty("validityPeriod")
  private CoverageValidityPeriod validityPeriod;

  @JsonProperty("medicalInsuranceOrganizationCode")
  private String medicalInsuranceOrganizationCode;

}
