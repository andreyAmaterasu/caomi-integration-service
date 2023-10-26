package ru.kostromin.caomi.integration.service.data.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("hlt_MKAB")
public class HltMkab {

  @Id
  @Column("MKABID")
  private Integer mkabId;

  @Column("FAMILY")
  private String lastName;

  @Column("NAME")
  private String name;

  @Column("OT")
  private String patronymic;

  @Column("SS")
  private String snils;

  @Column("DATE_BD")
  private LocalDateTime birthDate;

  @Column("rf_SMOID")
  private Integer rfSmoId;

  @Column("rf_LPUID")
  private Integer rfLpuId;

  @Column("S_POL")
  private String policySeries;

  @Column("N_POL")
  private String policyNumber;

  @Column("DatePolBegin")
  private LocalDateTime datePolBegin;

  @Column("DatePolEnd")
  private LocalDateTime datePolEnd;

  @Column("rf_kl_SexID")
  private Integer rfKlSexId;

  @Column("rf_kl_TipOMSID")
  private Integer rfKlTipOmsId;

  @Column("UGUID")
  private String uguid;
}
