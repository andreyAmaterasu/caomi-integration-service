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
public class PatientDto {

  @Column("patient_patientId")
  private String id;

  @Column("patient_gender")
  private String gender;

  @Column("patient_birthDate")
  private String birthDate;

  @Column("patient_generalPractitionerMoOid")
  private String generalPractitionerMoOid;

  @Column("coverage_policyTypeCode")
  private Long policyTypeCode;

  @Column("coverage_policyNumber")
  private String policyNumber;

  @Column("coverage_policyStatus")
  private Boolean policyStatus;

  @Column("coverage_validityPeriod_start")
  private LocalDateTime validityPeriodStart;

  @Column("coverage_validityPeriod_end")
  private LocalDateTime validityPeriodEnd;

  @Column("coverage_medicalInsuranceOrganizationCode")
  private String medInsuranceOrgCode;

}
