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
@Table("oms_MKB")
public class OmsMkb {

  @Id
  @Column("MKBID")
  private Integer mkbId;

  @Column("DS")
  private String ds;
}
