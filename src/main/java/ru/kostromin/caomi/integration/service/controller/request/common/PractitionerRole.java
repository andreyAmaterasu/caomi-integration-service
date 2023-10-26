package ru.kostromin.caomi.integration.service.controller.request.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import ru.kostromin.caomi.integration.service.controller.request.refresult.PractitionerRoleEmploymentPeriod;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(of = {"medStaffRoleId", "active", "employmentPeriod", "moOid", "positionCode", "specialityCode"})
public class PractitionerRole {

  @NotBlank(message = "medStaffRoleId")
  @JsonProperty("medStaffRoleId")
  private String medStaffRoleId;

  @NotNull(message = "active")
  @JsonProperty("active")
  private Boolean active;

  @JsonProperty("employmentPeriod")
  private PractitionerRoleEmploymentPeriod employmentPeriod;

  @NotBlank(message = "moOid")
  @JsonProperty("moOid")
  private String moOid;

  @NotBlank(message = "positionCode")
  @JsonProperty("positionCode")
  private String positionCode;

  @NotBlank(message = "specialityCode")
  @JsonProperty("specialityCode")
  private String specialityCode;
}
