package ru.kostromin.caomi.integration.service.data.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DeviceDto {

  private Integer caomiDeviceId;

  private String guid;

  private String deviceMisId;

  private Boolean isActiveDevice;

  private String deviceName;

  private String owner;

  private String serviceCode;

  private String serviceName;

  private Boolean isActiveService;
}
