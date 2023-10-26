package ru.kostromin.caomi.integration.service.controller.request.refresult;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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
@EqualsAndHashCode(of = {"serviceId", "serviceCode", "effectiveDateTime", "serviceResultId",
    "imagingStudy", "issued", "moOid", "presentedForm", "protocolStatusCode", "patientId", "practitioner", "practitionerRole"})
public class ServiceResult {

  @JsonProperty("serviceId")
  @NotBlank(message = "serviceId")
  private String serviceId;

  @JsonProperty("serviceCode")
  private String serviceCode;

  @JsonProperty("effectiveDateTime")
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  private OffsetDateTime effectiveDateTime;

  @JsonProperty("serviceResultId")
  private String serviceResultId;

  @JsonProperty("imagingStudy")
  private List<ImagingStudyMis> imagingStudy;

  @JsonProperty("issued")
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  private OffsetDateTime issued;

  @JsonProperty(value = "moOid")
  private String moOid;

  @JsonProperty("presentedForm")
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  private List<ResourcePresentedForm> presentedForm;

  @JsonProperty("protocolStatusCode")
  private Long protocolStatusCode;

  @NotBlank(message = "patientId")
  @JsonProperty("patientId")
  private String patientId;

  @Valid
  @NotNull(message = "practitioner")
  @JsonProperty(value = "practitioner")
  private Practitioner practitioner;

  @Valid
  @NotNull(message = "practitionerRole")
  @JsonProperty(value = "practitionerRole")
  private PractitionerRole practitionerRole;
}
