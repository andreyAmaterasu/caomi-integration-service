package ru.kostromin.caomi.integration.service.data.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Column;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class ServiceRequestDto {

  @Column("serviceRequest_serviceId")
  private String id;

  @Column("serviceRequest_serviceStatusCode")
  private Long statusCode;

  @Column("serviceRequest_serviceIntentCode")
  private String intentCode;

  @Column("serviceRequest_servicePriorityCode")
  private Long priorityCode;

  @Column("serviceRequest_serviceCode")
  private String code;

  @Column("serviceRequest_authoredOn")
  private LocalDateTime authOnDate;

  @Column("serviceRequest_desiredPeriod_start")
  private LocalDateTime desiredPeriodStart;

  @Column("serviceRequest_desiredPeriod_end")
  private LocalDateTime desiredPeriodEnd;

  @Column("serviceRequest_performerTypeCode")
  private String performerTypeCode;

  @Column("serviceRequest_performerDeviceId")
  private String performerDeviceId;

  @Column("serviceRequest_performerMoOid")
  private String performerMoOid;

  @Column("serviceRequest_reasonCode")
  private String reasonCode;
}
