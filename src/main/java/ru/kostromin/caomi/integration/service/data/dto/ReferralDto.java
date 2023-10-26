package ru.kostromin.caomi.integration.service.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class ReferralDto {

  private String moOid;

  private String referralNumber;

  private String practitioner;

  private Long practitionerRole;

}
