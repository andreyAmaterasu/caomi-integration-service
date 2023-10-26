package ru.kostromin.caomi.integration.service.controller.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response {

  @JsonProperty("errorCode")
  private String errorCode;

  @JsonProperty("errorText")
  private String errorText;

  @JsonProperty("id")
  private Long id;

  @JsonProperty("dateTime")
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  private LocalDateTime dateTime;

  @JsonProperty("idReferral")
  private UUID idReferral;

  @Getter
  public static enum ErrorCode {
      E000("0", "success"),
      E001("e_001:req_fields_null", null),
      E006("e_006:data_not_found", null);

      private final String code;
      private final String message;

      ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
      }
    }
  }
