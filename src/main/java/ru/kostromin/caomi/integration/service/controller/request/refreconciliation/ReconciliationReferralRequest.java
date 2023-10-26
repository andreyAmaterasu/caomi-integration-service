package ru.kostromin.caomi.integration.service.controller.request.refreconciliation;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.kostromin.caomi.integration.service.validation.contoller.ValidRejectionReasonWithNonAgreedReferral;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@ValidRejectionReasonWithNonAgreedReferral
public class ReconciliationReferralRequest {

  @NotNull(message = "idReferral")
  @JsonProperty("idReferral")
  private UUID idReferral;

  @NotNull(message = "agreedReferral")
  @JsonProperty("agreedReferral")
  private Boolean agreedReferral;

  @JsonProperty("rejectionReason")
  private String rejectionReason;

  @Valid
  @NotNull(message = "occurencePeriod")
  @JsonProperty("occurencePeriod")
  private OccurrencePeriod ocurrencePeriod;
}
