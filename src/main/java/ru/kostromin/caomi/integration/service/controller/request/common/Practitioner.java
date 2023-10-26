package ru.kostromin.caomi.integration.service.controller.request.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
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
@EqualsAndHashCode(of = {"medStaffId", "snils", "fullName", "gender", "birthDate"})
public class Practitioner {

  @NotBlank(message = "medStaffId")
  @JsonProperty("medStaffId")
  private String medStaffId;

  @JsonProperty("snils")
  private String snils;

  @Valid
  @NotNull(message = "fullName")
  @JsonProperty("fullName")
  private FullName fullName;

  @JsonProperty("gender")
  private Long gender;

  @NotNull(message = "birthDate")
  @JsonProperty("birthDate")
  private LocalDate birthDate;
}
