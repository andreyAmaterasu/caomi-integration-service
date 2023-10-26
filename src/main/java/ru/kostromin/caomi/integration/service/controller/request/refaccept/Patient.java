package ru.kostromin.caomi.integration.service.controller.request.refaccept;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import java.time.LocalDate;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import ru.kostromin.caomi.integration.service.controller.request.common.FullName;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(of = {"patientId", "gender", "birthDate",
    "generalPractitionerMoOid"})
public class Patient {

  @NotBlank(message = "patientId")
  @JsonProperty("patientId")
  private String patientId;

  @NotBlank(message = "snils")
  @JsonProperty("snils")
  private String snils;

  @Valid
  @NotNull(message = "fullName")
  @JsonProperty("fullName")
  private FullName fullName;

  @NotNull(message = "gender")
  @JsonProperty("gender")
  private Long gender;

  @NotNull(message = "birthDate")
  @JsonProperty("birthDate")
  @JsonSerialize(using = LocalDateSerializer.class)
  @JsonDeserialize(using = LocalDateDeserializer.class)
  private LocalDate birthDate;

  @JsonProperty("address")
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  private List<PatientAddress> address;

  @NotBlank(message = "generalPractitionerMoOid")
  @JsonProperty("generalPractitionerMoOid")
  private String generalPractitionerMoOid;
}
