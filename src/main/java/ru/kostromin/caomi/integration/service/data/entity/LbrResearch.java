package ru.kostromin.caomi.integration.service.data.entity;

import java.time.LocalDateTime;
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
@Table("lbr_Research")
public class LbrResearch {

  @Id
  @Column("ResearchID")
  private Integer researchId;

  @Column("LAB_DOCT_FIO")
  private String labDoctorFio;

  @Column("Date_Complete")
  private LocalDateTime dateComplete;

  @Column("isComplete")
  private Boolean isComplete;

  @Column("Flag")
  private Integer flag;

  @Column("rf_LaboratoryResearchGUID")
  private String rfLaboratoryResearchGUID;

  @Column("IsPerformed")
  private Boolean isPerformed;

  @Column("DatePerformed")
  private LocalDateTime datePerformed;

  @Column("Conclusion")
  private String conclusion;

  @Column("isCanceled")
  private Boolean isCancelled;

  @Column("StudyUID")
  private String studyUid;

  @Column("PerformedDocFio")
  private String performedDocFio;

  @Column("IsIssued")
  private Boolean isIssued;

  @Column("DateIssued")
  private LocalDateTime dateIssued;

  @Column("IsRegistred")
  private Boolean isRegistered;

  @Column("IsReceipt")
  private Boolean isReceipt;

  @Column("IsIemkData")
  private Boolean isIemkData;

  @Column("IsMainExpert")
  private Boolean isMainExpert;

  @Column("PerformedLpuName")
  private String performedLpuName;

  @Column("isRejected")
  private Boolean isRejected;

  @Column("IsCompleteEarly")
  private Boolean isCompleteEarly;

  @Column("rf_ResearchTypeUGUID")
  private String rfResearchTypeUguid;

  @Column("Number")
  private String number;
}
