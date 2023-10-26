package ru.kostromin.caomi.integration.service.feign.request.device;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class CaomiDeviceRequest {

  private String deviceMisId;

  private Boolean isActive;

  private String deviceName;

  private String owner;

  private List<Service> service;

  @Data
  @JsonInclude(Include.NON_NULL)
  public static class Service{
    private String serviceCode;

    private String serviceName;

    private Boolean isActive;
  }
}
