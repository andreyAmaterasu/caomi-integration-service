package ru.kostromin.caomi.integration.service.data.entity;

import java.time.LocalDateTime;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("hst_caomiReferral")
public class HstCaomiReferral {

  @Id
  @Column("caomiReferralID")
  private Integer caomiReferralId;

  @Column("lbrLaboratoryResearchID")
  private Integer lbrLaboratoryResearchId;

  @Column("DateCreate")
  private LocalDateTime dateCreate;

  @Column("DateChange")
  private LocalDateTime dateChange;

  @Column("caomiID")
  private String caomiId;

  @Column("statusID")
  private Integer statusId;

  @Column("DateStatus")
  private LocalDateTime dateStatus;

  @Column("HttpStatus")
  private Integer httpStatus;

  @Column("errorCode")
  private String errorCode;

  @Column("errorText")
  private String errorText;

  @Column("agreedReferral")
  private Boolean agreedReferral;

  @Column("rejectionReason")
  private String rejectionReason;

  @Column("dateStart")
  private LocalDateTime dateStart;

  @Column("dateEnd")
  private LocalDateTime dateEnd;

}
