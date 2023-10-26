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
@Table("hst_caomiAcceptReferral")
public class HstCaomiAcceptReferral {

  @Id
  @Column("caomiAcceptReferralID")
  private Integer id;

  @Column("dateCreate")
  private LocalDateTime dateCreate;

  @Column("caomiID")
  private String caomiId;

  @Column("recipientMoOID")
  private String recipientMoOid;

  @Column("performerMoOID")
  private String performerMoOid;

  @Column("performerDeviceID")
  private String performerDeviceId;

  @Column("statusID")
  private Integer statusId;

  @Column("lbrLaboratoryResearchID")
  private Integer lbrLaboratoryResearchId;

  @Column("MKABID")
  private Integer mkabId;

  @Column("errorCode")
  private String errorCode;

  @Column("errorText")
  private String errorText;

  @Column("serviceCode")
  private String serviceCode;
}
