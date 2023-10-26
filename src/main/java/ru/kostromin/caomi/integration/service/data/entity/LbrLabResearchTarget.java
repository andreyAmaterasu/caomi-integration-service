package ru.kostromin.caomi.integration.service.data.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("lbr_LabResearchTarget")
public class LbrLabResearchTarget {

  @Column("LabResearchTargetID")
  private Integer labResearchTargetId;

  @Column("Code")
  private String code;

}
