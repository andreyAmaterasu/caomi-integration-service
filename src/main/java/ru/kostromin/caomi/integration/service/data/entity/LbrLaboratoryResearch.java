package ru.kostromin.caomi.integration.service.data.entity;

import java.time.LocalDateTime;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("lbr_LaboratoryResearch")
public class LbrLaboratoryResearch {

  @Id
  @Column("LaboratoryResearchID")
  private Integer laboratoryResearchId;

  @Column("GUID")
  private String guid;

  @Column("Number")
  private String number;

  @Column("rf_MKABID")
  private Integer rfMkabId;

  @Column("Pat_Family")
  private String patFamily;

  @Column("Pat_Name")
  private String patName;

  @Column("Pat_Ot")
  private String patOt;

  @Column("DOCT_FIO")
  private String doctFio;

  @Column("DOCT_PCOD")
  private String docPcod;

  @Column("Date_Direction")
  private LocalDateTime dateDirection;

  @Column("rf_MKBID")
  private Integer rfMKBID;

  @Column("Pat_Birthday")
  private LocalDateTime patBirthday;

  @Column("Pat_W")
  private Boolean patW;

  @Column("rf_SMOID")
  private Integer rfSmoId;

  @Column("rf_kl_TipOMSID")
  private Integer rfKlTipOmsId;

  @Column("Pat_S_POL")
  private String patSPol;

  @Column("Pat_N_POL")
  private String patNPol;

  @Column("rf_LPUSenderID")
  private Integer rfLpuSenderId;

  @Column("rf_kl_ProfitTypeID")
  private Integer rfKlProfitTypeId;

  @Column("isReadOnly")
  private Boolean isReadOnly;

  @Column("rf_LPUID")
  private Integer rfLpuId;

  @Column("rf_LabResearchTargetID")
  private Integer rfLabResearchTargetId;

  @Column("DateCreate")
  private LocalDateTime dateCreate;

  @Column("AccessionNumber")
  private String accessionNumber;

  @Column("Pat_SS")
  private String patSs;

  @Column("DOCT_SS")
  private String doctSs;
}
