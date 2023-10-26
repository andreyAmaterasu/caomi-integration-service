package ru.kostromin.caomi.integration.service.data.entity;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Сущность таблицы hst_caomiDevice
 */
@Data
@Table("hst_caomiDevice")
public class HstCaomiDevice {

  @Id
  @Column("caomiDeviceID")
  private Integer caomiDeviceId;

  @Column("hltEquipmentGUID")
  private String hltEquipmentGuid;

  @Column("dateCreate")
  private LocalDateTime dateCreate;

  @Column("dateChange")
  private LocalDateTime dateChange;

  @Column("caomiID")
  private Integer caomiId;

  @Column("statusID")
  private Integer statusId;

  @Column("dateStatus")
  private LocalDateTime dateStatus;

  @Column("httpStatus")
  private Integer httpStatus;

  @Column("errorCode")
  private String errorCode;

  @Column("errorText")
  private String errorText;
}
