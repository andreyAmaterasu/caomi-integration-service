package ru.kostromin.caomi.integration.service.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Column;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class ReconciliationDto {

  @Column("idReferral")
  private String idReferral;

  @Column("agreedReferral")
  private Boolean agreedReferral;

  @Column("rejectionReason")
  private String rejectionReason;

  @Column("occurencePeriodStart")
  private String occurrencePeriodStart;

  @Column("occurencePeriodEnd")
  private String occurrencePeriodEnd;

  @Column("deviceMisId")
  private String deviceMisId;

  @Column("owner")
  private String owner;

  @Column("deviceName")
  private String deviceName;
}
