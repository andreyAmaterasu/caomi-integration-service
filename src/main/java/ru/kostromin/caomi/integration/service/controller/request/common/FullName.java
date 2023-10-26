package ru.kostromin.caomi.integration.service.controller.request.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(of = {"firstName", "lastName", "middleName"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FullName {

  @NotBlank(message = "firstName")
  @JsonProperty("firstName")
  private String firstName;

  @NotBlank(message = "lastName")
  @JsonProperty("lastName")
  private String lastName;

  @JsonProperty("middleName")
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  private String middleName;
}
