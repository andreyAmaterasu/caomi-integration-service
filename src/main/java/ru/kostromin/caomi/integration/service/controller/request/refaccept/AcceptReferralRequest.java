package ru.kostromin.caomi.integration.service.controller.request.refaccept;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.UUID;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import ru.kostromin.caomi.integration.service.controller.request.common.Practitioner;
import ru.kostromin.caomi.integration.service.controller.request.common.PractitionerRole;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(of = {"moOid", "referralNumber", "practitioner",
    "practitionerRole",
    "patient", "coverage", "serviceRequest"})
public class AcceptReferralRequest {

  @NotBlank(message = "moOid")
  @JsonProperty(value = "moOid")
  private String moOid;

  @NotBlank(message = "referralNumber")
  @JsonProperty(value = "referralNumber")
  private String referralNumber;

  @NotNull(message = "idReferral")
  @JsonProperty("idReferral")
  private UUID idReferral;

  @Valid
  @JsonProperty(value = "practitioner")
  private Practitioner practitioner;

  @Valid
  @JsonProperty(value = "practitionerRole")
  private PractitionerRole practitionerRole;

  @Valid
  @JsonProperty(value = "patient")
  private Patient patient;

  @Valid
  @JsonProperty(value = "coverage")
  private Coverage coverage;

  @Valid
  @Size(min = 1, message = "список serviceRequest должен иметь минимум 1 элемент")
  @JsonProperty("serviceRequest")
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  private List<ServiceRequest> serviceRequest;
}
