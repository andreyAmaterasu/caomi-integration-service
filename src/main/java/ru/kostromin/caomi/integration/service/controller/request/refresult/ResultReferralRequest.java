package ru.kostromin.caomi.integration.service.controller.request.refresult;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.UUID;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(of = {"idReferral", "referralNumber", "serviceResult"})
public class ResultReferralRequest {

  @NotNull(message = "idReferral")
  @JsonProperty("idReferral")
  private UUID idReferral;

  @JsonProperty(value = "referralNumber")
  private String referralNumber;

  @Valid
  @NotNull(message = "serviceResult")
  @Size(min = 1, message = "serviceResult должен иметь минимум 1 элемент списка")
  @JsonProperty(value = "serviceResult")
  private List<ServiceResult> serviceResult;
}
