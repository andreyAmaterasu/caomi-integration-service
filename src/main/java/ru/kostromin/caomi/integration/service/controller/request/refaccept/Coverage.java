package ru.kostromin.caomi.integration.service.controller.request.refaccept;

import com.fasterxml.jackson.annotation.JsonProperty;
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
@EqualsAndHashCode(of = {"policyTypeCode", "policyNumber", "policyStatus", "validityPeriod"})
public class Coverage {

  @NotNull(message = "policyTypeCode")
  @JsonProperty("policyTypeCode")
  private Long policyTypeCode;

  @NotBlank(message = "policyNumber")
  @JsonProperty("policyNumber")
  private String policyNumber;

  @NotNull(message = "policyStatus")
  @JsonProperty("policyStatus")
  private Long policyStatus;

  @JsonProperty("validityPeriod")
  private CoverageValidityPeriod validityPeriod;

  @JsonProperty("medicalInsuranceOrganizationCode")
  private String medicalInsuranceOrganizationCode;
}
