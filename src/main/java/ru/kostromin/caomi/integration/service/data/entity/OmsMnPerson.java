package ru.kostromin.caomi.integration.service.data.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("oms_mn_Person")
public class OmsMnPerson {

  @Id
  @Column("mn_PersonID")
  private Integer mnPersonId;

  @Column("PersonGUID")
  private String personGuid;
}
