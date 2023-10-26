package ru.kostromin.caomi.integration.service.data.entity;

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
@Table("oms_kl_TipOMS")
public class OmsKlTipOms {

  @Id
  @Column("kl_TipOMSID")
  private Integer klTipOmsId;

  @Column("IDDOC")
  private Integer idDoc;
}
