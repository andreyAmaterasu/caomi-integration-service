package ru.kostromin.caomi.integration.service.data.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("hlt_PolisMKAB")
public class HltPolisMkab {

  @Id
  @Column("PolisMKABID")
  private Integer polisMkabId;

  @Column("rf_MKABID")
  private Integer rfMkabId;

  @Column("S_POL")
  private String policySeries;

  @Column("N_POL")
  private String policyNumber;

  @Column("rf_kl_ProfitTypeID")
  private Integer rfKlProfitTypeId;

  @Column("DatePolBegin")
  private LocalDateTime datePolBegin;

  @Column("DatePolEnd")
  private LocalDateTime datePolEnd;

  @Column("rf_SMOID")
  private Integer rfSmoId;

  @Column("isActive")
  private Boolean isActive;

  @Column("GUID")
  private String guid;

  @Column("Flags")
  private Integer flags;

  @Column("rf_kl_TipOMSID")
  private Integer rfKlTipOmsId;

  @Column("rf_DOGOVORID")
  private Integer rfDogovorId;
}
