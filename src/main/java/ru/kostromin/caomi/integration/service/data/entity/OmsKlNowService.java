package ru.kostromin.caomi.integration.service.data.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("oms_kl_NomService")
public class OmsKlNowService {

  @Id
  @Column("kl_NomServiceID")
  private Long klNomServiceId;

  @Column("CODE")
  private String code;

}
