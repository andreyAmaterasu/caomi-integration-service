package ru.kostromin.caomi.integration.service.controller.request.refresult;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(of = {"data", "contentType"})
public class ResourcePresentedForm {

  @JsonProperty("contentType")
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  private String contentType;

  @JsonProperty("data")
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  private String data;
}
