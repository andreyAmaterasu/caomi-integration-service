package ru.kostromin.caomi.integration.service.feign.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentResponse {

  @JsonProperty("statusCode")
  private String statusCode;

  @JsonProperty("comment")
  private String comment;

}
