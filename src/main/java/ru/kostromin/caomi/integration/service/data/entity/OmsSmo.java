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
@Table("oms_smo")
public class OmsSmo {

  @Id
  @Column("SMOID")
  private Integer smoId;

  @Column("COD")
  private String cod;

}
