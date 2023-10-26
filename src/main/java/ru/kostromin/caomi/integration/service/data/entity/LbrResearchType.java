package ru.kostromin.caomi.integration.service.data.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("lbr_ResearchType")
public class LbrResearchType {

  @Id
  @Column("ResearchTypeID")
  private Integer researchTypeId;

  @Column("Code")
  private String code;

  @Column("UGUID")
  private String uguid;

  @Column("rf_kl_NomServiceID")
  private Long rfKlNomServiceId;
}
