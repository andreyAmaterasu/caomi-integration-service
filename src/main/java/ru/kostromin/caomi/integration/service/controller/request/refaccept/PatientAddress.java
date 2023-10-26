package ru.kostromin.caomi.integration.service.controller.request.refaccept;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(of = {"typeCode", "text"})
public class PatientAddress {

  @JsonProperty("typeCode")
  private Integer typeCode;

  @JsonProperty("text")
  private String text;
}
