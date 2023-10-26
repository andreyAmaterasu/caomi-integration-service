package ru.kostromin.caomi.integration.service.feign.response;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CaomiBasicResponse {

  private String errorCode;

  private String errorText;

  private String idReferral;

  private int id;

  private LocalDateTime dateTime;
}
