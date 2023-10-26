package ru.kostromin.caomi.integration.service.data.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Column;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class EquipmentSlotsDto {

  @Column("date")
  private LocalDateTime date;

  @Column("beginTime")
  private LocalDateTime beginTime;

  @Column("externalScheduleId")
  private Integer externalScheduleId;

  @Column("uguid")
  private String uguid;

  @Column("doctorTimeTableId")
  private Integer doctorTimeTableId;

  @Column("rfMkabId")
  private Integer rfMkabID;

  @Column("equipmentGuid")
  private String equipmentGuid;

  @Column("number")
  private String number;

}
